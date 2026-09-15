package com.srapp.gemini

import kotlinx.serialization.Serializable

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseModalities: List<String>? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiApiError? = null
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

@Serializable
data class GeminiApiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

enum class GeminiModelTier(
    val modelId: String,
    val displayName: String,
    val subtitle: String,
    val badge: String
) {
    FLASH_LITE(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Flash Lite",
        subtitle = "Ultra-low latency impulse deflection & panic coaching",
        badge = "⚡ Fast"
    ),
    FLASH_BALANCED(
        modelId = "gemini-3.5-flash",
        displayName = "Flash 3.5",
        subtitle = "Balanced daily accountability, routine & habit psychology",
        badge = "🧠 Balanced"
    ),
    PRO_DEEP(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Pro 3.1",
        subtitle = "Deep cognitive behavioral reframing & root cause analysis",
        badge = "🔬 Deep CBT"
    )
}

enum class LyriaModelType(
    val modelId: String,
    val displayName: String,
    val description: String,
    val durationLabel: String
) {
    CLIP(
        modelId = "lyria-3-clip-preview",
        displayName = "Lyria 3 Clip",
        description = "Short dopamine-reset ambient audio loop (15-30s)",
        durationLabel = "15-30 sec"
    ),
    PRO(
        modelId = "lyria-3-pro-preview",
        displayName = "Lyria 3 Pro",
        description = "Full-length binaural flow state soundscape",
        durationLabel = "Full Track"
    )
}
