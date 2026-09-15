package com.srapp.music

import android.app.Application
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srapp.audio.SoundscapeEngine
import com.srapp.gemini.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class SoundscapePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val prompt: String,
    val baseFreq: Float,
    val beatDelta: Float,
    val iconName: String
)

sealed class MusicPlaybackState {
    data object Idle : MusicPlaybackState()
    data class Generating(val modelName: String, val prompt: String) : MusicPlaybackState()
    data class Playing(val trackTitle: String, val isProcedural: Boolean) : MusicPlaybackState()
    data class Paused(val trackTitle: String) : MusicPlaybackState()
    data class Error(val message: String) : MusicPlaybackState()
}

class LyriaMusicViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "LyriaMusicViewModel"
    private val soundscape = SoundscapeEngine.get(application)
    private var mediaPlayer: MediaPlayer? = null

    val presets = listOf(
        SoundscapePreset(
            id = "theta_reset",
            title = "Deep Theta Wave",
            subtitle = "4-8Hz wave entrainment for acute dopamine craving reset",
            prompt = "Generate a soothing 432Hz deep meditative ambient soundscape with gentle theta binaural drones and organic warm pads.",
            baseFreq = 216f,
            beatDelta = 5.5f,
            iconName = "wave"
        ),
        SoundscapePreset(
            id = "solfeggio_528",
            title = "Solfeggio 528Hz Reset",
            subtitle = "Miracle transformation tone with harmonic singing bowls",
            prompt = "Synthesize an ethereal ambient tone centered on 528Hz Solfeggio frequency with acoustic Tibetan singing bowl resonances.",
            baseFreq = 528f,
            beatDelta = 4.0f,
            iconName = "chime"
        ),
        SoundscapePreset(
            id = "cyber_rain",
            title = "Cyberpunk Rain Lofi",
            subtitle = "Atmospheric gentle rainfall with soft analog synth chords",
            prompt = "A 60-second chill relaxing cyberpunk ambient lofi track with soothing midnight rain texture, slow Rhodes electric piano, and warm bass.",
            baseFreq = 180f,
            beatDelta = 7.83f,
            iconName = "rain"
        ),
        SoundscapePreset(
            id = "alpha_flow",
            title = "Alpha Focus Flow",
            subtitle = "10Hz binaural beats for deep coding, reading & study",
            prompt = "Binaural alpha wave focus audio track at 10Hz difference with smooth ambient cosmic textures for unbroken deep work.",
            baseFreq = 300f,
            beatDelta = 10.0f,
            iconName = "bolt"
        ),
        SoundscapePreset(
            id = "void_drone",
            title = "Void Starlight Drone",
            subtitle = "Zero-distraction sub-bass cosmic white noise",
            prompt = "Hypnotic deep space ambient drone with smooth warm sub-bass frequencies, zero percussion, ultra-calming.",
            baseFreq = 144f,
            beatDelta = 4.5f,
            iconName = "space"
        )
    )

    private val _selectedModel = MutableStateFlow(LyriaModelType.CLIP)
    val selectedModel: StateFlow<LyriaModelType> = _selectedModel.asStateFlow()

    private val _playbackState = MutableStateFlow<MusicPlaybackState>(MusicPlaybackState.Idle)
    val playbackState: StateFlow<MusicPlaybackState> = _playbackState.asStateFlow()

    private val _activePreset = MutableStateFlow(presets.first())
    val activePreset: StateFlow<SoundscapePreset> = _activePreset.asStateFlow()

    private val _customPrompt = MutableStateFlow("")
    val customPrompt: StateFlow<String> = _customPrompt.asStateFlow()

    private val _isLooping = MutableStateFlow(true)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    // Simulated waveform amplitudes for Compose visualizer (0f..1f)
    private val _waveAmplitudes = MutableStateFlow(List(24) { 0.15f })
    val waveAmplitudes: StateFlow<List<Float>> = _waveAmplitudes.asStateFlow()

    init {
        startWaveformAnimation()
    }

    private fun startWaveformAnimation() {
        viewModelScope.launch {
            var step = 0
            while (isActive) {
                val isPlaying = _playbackState.value is MusicPlaybackState.Playing
                if (isPlaying) {
                    val updated = (0 until 24).map { i ->
                        val phase = (step * 0.25f + i * 0.4f)
                        val v = (kotlin.math.sin(phase) * 0.45f + 0.5f).coerceIn(0.12f, 0.95f)
                        v
                    }
                    _waveAmplitudes.value = updated
                    step++
                } else {
                    _waveAmplitudes.value = List(24) { 0.1f }
                }
                delay(60)
            }
        }
    }

    fun selectPreset(preset: SoundscapePreset) {
        _activePreset.value = preset
        soundscape.playClick()
    }

    fun selectModel(model: LyriaModelType) {
        _selectedModel.value = model
        soundscape.playClick()
    }

    fun updateCustomPrompt(text: String) {
        _customPrompt.value = text
    }

    fun toggleLoop() {
        _isLooping.value = !_isLooping.value
        mediaPlayer?.isLooping = _isLooping.value
        soundscape.playClick()
    }

    /**
     * Triggers generation with Lyria-3 model or seamlessly activates procedural
     * high-fidelity binaural engine if offline or API key is absent.
     */
    fun startSoundscape() {
        val preset = _activePreset.value
        val model = _selectedModel.value
        val prompt = if (_customPrompt.value.isNotBlank()) _customPrompt.value else preset.prompt

        soundscape.playClick()
        _playbackState.value = MusicPlaybackState.Generating(model.displayName, prompt)

        viewModelScope.launch {
            val apiKey = GeminiClient.getApiKey()

            if (apiKey.isBlank()) {
                // Instantly fall back to procedural acoustic engine
                playProceduralBinaural(preset)
                return@launch
            }

            try {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = prompt))
                        )
                    ),
                    generationConfig = GenerationConfig(
                        responseModalities = listOf("AUDIO")
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiClient.service.generateContent(
                        model = model.modelId,
                        apiKey = apiKey,
                        request = request
                    )
                }

                val inlineAudio = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.inlineData
                if (inlineAudio != null && inlineAudio.data.isNotBlank()) {
                    playBase64Audio(inlineAudio.data, inlineAudio.mimeType, preset.title)
                } else {
                    // Lyria model returned text or empty audio; start real-time synthesized procedural binaural tone
                    playProceduralBinaural(preset)
                }
            } catch (e: Exception) {
                Log.w(tag, "Lyria API exception: ${e.message}, switching to procedural ambient generator")
                playProceduralBinaural(preset)
            }
        }
    }

    private fun playProceduralBinaural(preset: SoundscapePreset) {
        soundscape.startBinauralSoundscape(preset.baseFreq, preset.beatDelta)
        _playbackState.value = MusicPlaybackState.Playing(preset.title, isProcedural = true)
        soundscape.playChime()
    }

    private suspend fun playBase64Audio(base64Data: String, mimeType: String, title: String) = withContext(Dispatchers.IO) {
        try {
            val decoded = Base64.decode(base64Data, Base64.DEFAULT)
            val tempFile = File(getApplication<Application>().cacheDir, "lyria_sound_${System.currentTimeMillis()}.mp3")
            FileOutputStream(tempFile).use { it.write(decoded) }

            withContext(Dispatchers.Main) {
                stopPlayback()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    isLooping = _isLooping.value
                    prepare()
                    start()
                    setOnCompletionListener {
                        if (!_isLooping.value) {
                            _playbackState.value = MusicPlaybackState.Idle
                        }
                    }
                }
                _playbackState.value = MusicPlaybackState.Playing(title, isProcedural = false)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to decode/play Lyria audio: ${e.message}")
            withContext(Dispatchers.Main) {
                playProceduralBinaural(_activePreset.value)
            }
        }
    }

    fun pausePlayback() {
        soundscape.playClick()
        val current = _playbackState.value
        if (current is MusicPlaybackState.Playing) {
            if (current.isProcedural) {
                soundscape.stopAmbient()
            } else {
                mediaPlayer?.pause()
            }
            _playbackState.value = MusicPlaybackState.Paused(current.trackTitle)
        }
    }

    fun resumePlayback() {
        soundscape.playClick()
        val current = _playbackState.value
        if (current is MusicPlaybackState.Paused) {
            mediaPlayer?.start() ?: soundscape.startBinauralSoundscape(_activePreset.value.baseFreq, _activePreset.value.beatDelta)
            _playbackState.value = MusicPlaybackState.Playing(current.trackTitle, isProcedural = mediaPlayer == null)
        }
    }

    fun stopPlayback() {
        soundscape.playClick()
        soundscape.stopAmbient()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        _playbackState.value = MusicPlaybackState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}
