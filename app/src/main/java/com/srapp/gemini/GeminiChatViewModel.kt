package com.srapp.gemini

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srapp.audio.SoundscapeEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelTier: GeminiModelTier? = null,
    val isError: Boolean = false
)

sealed class ChatUiState {
    data object Idle : ChatUiState()
    data class Generating(val modelTier: GeminiModelTier) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class GeminiChatViewModel(application: Application) : AndroidViewModel(application) {

    private val soundscape = SoundscapeEngine.get(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "model",
                text = "Welcome to your Mental Fortress. I am your cognitive resilience companion, powered by Gemini AI.\n\nWhether you are experiencing an acute dopamine urge, need to deconstruct a trigger, or want to reinforce daily discipline, choose your model tier above and speak freely. You are in complete control.",
                modelTier = GeminiModelTier.FLASH_BALANCED
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _selectedTier = MutableStateFlow(GeminiModelTier.FLASH_BALANCED)
    val selectedTier: StateFlow<GeminiModelTier> = _selectedTier.asStateFlow()

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun selectTier(tier: GeminiModelTier) {
        _selectedTier.value = tier
        soundscape.playClick()
    }

    fun clearChat() {
        soundscape.playClick()
        _messages.value = listOf(
            ChatMessage(
                role = "model",
                text = "Mental Fortress reset. Ready for your next session.",
                modelTier = _selectedTier.value
            )
        )
    }

    fun sendMessage(userText: String) {
        val prompt = userText.trim()
        if (prompt.isEmpty() || _uiState.value is ChatUiState.Generating) return

        soundscape.playClick()

        val userMessage = ChatMessage(
            role = "user",
            text = prompt,
            modelTier = _selectedTier.value
        )
        _messages.value = _messages.value + userMessage
        _uiState.value = ChatUiState.Generating(_selectedTier.value)

        viewModelScope.launch {
            val tier = _selectedTier.value
            val responseText = executeGeminiTurn(prompt, tier)

            soundscape.playChime()
            _messages.value = _messages.value + ChatMessage(
                role = "model",
                text = responseText,
                modelTier = tier
            )
            _uiState.value = ChatUiState.Idle
        }
    }

    private suspend fun executeGeminiTurn(newPrompt: String, tier: GeminiModelTier): String = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext "⚠️ Gemini API key is not configured in Secrets.\n\n" +
                    "To enable live multi-turn AI responses, please add your GEMINI_API_KEY in the AI Studio Secrets panel.\n\n" +
                    "In the meantime, remember the 5-Minute Rule: Neurochemical cravings peak within 3–5 minutes. Shift your physical environment, drink cold water, and engage in diaphragmatic 4-7-8 breathing."
        }

        val systemPrompt = "You are the Mental Fortress Companion in SR App, an elite cognitive behavioral recovery, self-mastery, and digital wellness guide. " +
                "You combine neuroscience (dopamine receptor healing, prefrontal cortex strengthening, vagus nerve stimulation) with compassionate, tactical stoicism. " +
                "When the user is in distress, give immediate actionable physical instructions (ice, somatic grounding, 4-7-8 breathing). " +
                "When analyzing triggers, guide them with CBT reframing (Situation -> Automatic Thought -> Physical Urge -> Rational Alternative). " +
                "Keep responses punchy, beautifully structured with bullet points, and encouraging."

        // Build multi-turn context (last 6 messages)
        val historyContents = mutableListOf<Content>()
        val recentMessages = _messages.value.takeLast(6)
        for (msg in recentMessages) {
            val role = if (msg.role == "user") "user" else "model"
            historyContents.add(
                Content(
                    role = role,
                    parts = listOf(Part(text = msg.text))
                )
            )
        }
        // Add current prompt
        historyContents.add(
            Content(
                role = "user",
                parts = listOf(Part(text = newPrompt))
            )
        )

        val request = GenerateContentRequest(
            contents = historyContents,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(
                temperature = when (tier) {
                    GeminiModelTier.FLASH_LITE -> 0.4f
                    GeminiModelTier.FLASH_BALANCED -> 0.7f
                    GeminiModelTier.PRO_DEEP -> 0.8f
                },
                topP = 0.95f
            )
        )

        try {
            val response = GeminiClient.service.generateContent(
                model = tier.modelId,
                apiKey = apiKey,
                request = request
            )

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                text
            } else if (response.error != null) {
                "API Note (${response.error.code}): ${response.error.message ?: "Unable to generate response"}"
            } else {
                "No response was returned. Take a deep breath and center your focus."
            }
        } catch (e: Exception) {
            "Connection issue: ${e.message ?: "Network error"}. Remember: You have overcome 100% of your hardest days so far. Stay grounded."
        }
    }
}
