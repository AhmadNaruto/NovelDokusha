package my.noveldokusha.text_translator

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.AppCoroutineScope
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.text_translator.domain.TranslationManager
import my.noveldokusha.text_translator.domain.TranslationModelState
import my.noveldokusha.text_translator.domain.TranslatorState

/**
 * Composite translation manager that supports three translation methods:
 * 1. Google Gemini API (online, requires API key)
 * 2. Google Translate Free (online, no API key)
 * 3. MLKit On-Device (offline, requires model download)
 *
 * Priority based on settings:
 * - If API key configured + prefer online → Gemini (with Google Free fallback)
 * - If no API key + prefer online → Google Translate Free
 * - If prefer offline → MLKit (must download models first)
 */
class TranslationManagerComposite(
    private val appCoroutineScope: AppCoroutineScope,
    private val geminiManager: TranslationManagerGemini,
    private val googleFreeManager: TranslationManagerGoogleFree,
    private val mlKitManager: TranslationManagerMLKit,
    private val appPreferences: AppPreferences
) : TranslationManager {

    override val available: Boolean = true

    override val isUsingOnlineTranslation: Boolean
        get() = appPreferences.TRANSLATION_PREFER_ONLINE.value

    override val models = mutableStateListOf<TranslationModelState>()

    init {
        // Merge models from all three providers
        val allLanguages = mutableSetOf<String>()
        allLanguages.addAll(geminiManager.models.map { it.language })
        allLanguages.addAll(googleFreeManager.models.map { it.language })
        allLanguages.addAll(mlKitManager.models.map { it.language })

        models.addAll(allLanguages.map { lang ->
            TranslationModelState(
                language = lang,
                available = true,
                downloading = false,
                downloadingFailed = false
            )
        })
    }

    override suspend fun hasModelDownloaded(language: String): TranslationModelState? {
        return models.firstOrNull { it.language == language }
    }

    private fun getTranslationMode(): TranslationMode {
        val hasApiKey = hasGeminiApiKey()
        val preferOnline = appPreferences.TRANSLATION_PREFER_ONLINE.value

        return when {
            // Offline mode - use MLKit
            !preferOnline -> TranslationMode.MLKIT
            // Online mode with API key - use Gemini
            hasApiKey && preferOnline -> TranslationMode.GEMINI
            // Online mode without API key - use Google Free
            else -> TranslationMode.GOOGLE_FREE
        }
    }

    override fun getTranslator(source: String, target: String): TranslatorState {
        val mode = getTranslationMode()
        Log.d(TAG, "getTranslator: source=$source, target=$target, mode=$mode")

        return when (mode) {
            TranslationMode.MLKIT -> {
                Log.d(TAG, "Using MLKit on-device translation")
                mlKitManager.getTranslator(source, target)
            }
            TranslationMode.GEMINI -> {
                Log.d(TAG, "Using Gemini API with Google Free fallback")
                getGeminiWithFallbackTranslator(source, target)
            }
            TranslationMode.GOOGLE_FREE -> {
                Log.d(TAG, "Using Google Translate Free")
                googleFreeManager.getTranslator(source, target)
            }
        }
    }

    private fun getGeminiWithFallbackTranslator(
        source: String,
        target: String
    ): TranslatorState {
        val geminiTranslator = geminiManager.getTranslator(source, target)
        val googleFreeTranslator = googleFreeManager.getTranslator(source, target)

        return TranslatorState(
            source = source,
            target = target,
            translate = { input ->
                var lastException: Exception? = null
                // Try Gemini first with 2 retries
                repeat(2) { attempt ->
                    try {
                        Log.d(TAG, "Gemini attempt ${attempt + 1}/2")
                        val result = geminiTranslator.translate(input)

                        // Check if result is an error message
                        if (!result.startsWith("[Translation") && !result.startsWith("[API")) {
                            Log.d(TAG, "Gemini translation succeeded")
                            return@TranslatorState result
                        } else {
                            Log.w(TAG, "Gemini returned error: $result")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Gemini attempt ${attempt + 1} failed: ${e.message}", e)
                        lastException = e
                        if (attempt < 1) {
                            kotlinx.coroutines.delay(1000L)
                        }
                    }
                }

                // Fallback to Google Free
                Log.w(TAG, "Gemini failed, falling back to Google Translate Free")
                try {
                    val result = googleFreeTranslator.translate(input)
                    Log.d(TAG, "Google Free fallback succeeded")
                    result
                } catch (e: Exception) {
                    Log.e(TAG, "Google Free fallback also failed: ${e.message}", e)
                    throw lastException ?: e
                }
            }
        )
    }

    override fun downloadModel(language: String) {
        val mode = getTranslationMode()
        Log.d(TAG, "downloadModel: language=$language, mode=$mode")

        when (mode) {
            TranslationMode.MLKIT -> {
                Log.d(TAG, "Downloading MLKit model for $language")
                mlKitManager.downloadModel(language)
            }
            TranslationMode.GEMINI,
            TranslationMode.GOOGLE_FREE -> {
                // No-op for online translation
                Log.d(TAG, "No model download needed for online translation")
            }
        }
    }

    override fun removeModel(language: String) {
        val mode = getTranslationMode()
        Log.d(TAG, "removeModel: language=$language, mode=$mode")

        when (mode) {
            TranslationMode.MLKIT -> {
                Log.d(TAG, "Removing MLKit model for $language")
                mlKitManager.removeModel(language)
            }
            TranslationMode.GEMINI,
            TranslationMode.GOOGLE_FREE -> {
                // No-op for online translation
                Log.d(TAG, "No model to remove for online translation")
            }
        }
    }

    /**
     * Get current translation mode for UI display
     */
    fun getCurrentTranslationMode(): TranslationMode {
        return getTranslationMode()
    }

    /**
     * Get human-readable translation method name
     */
    fun getTranslationMethodName(): String {
        return when (getTranslationMode()) {
            TranslationMode.MLKIT -> "On-Device (MLKit)"
            TranslationMode.GEMINI -> "Google Gemini API"
            TranslationMode.GOOGLE_FREE -> "Google Translate (Free)"
        }
    }

    /**
     * Get storage size used by MLKit models
     */
    suspend fun getMLKitStorageSize(): Long {
        return mlKitManager.getStorageSize()
    }

    /**
     * Clear MLKit translator cache
     */
    fun clearMLKitCache() {
        mlKitManager.clearCache()
    }

    /**
     * Batch translation - delegates to active manager
     */
    override suspend fun translateBatch(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val mode = getTranslationMode()
        Log.d(TAG, "translateBatch: mode=$mode, texts=${texts.size}")

        when (mode) {
            TranslationMode.MLKIT -> {
                try {
                    return@withContext mlKitManager.translateBatch(texts, sourceLanguage, targetLanguage)
                } catch (e: Exception) {
                    Log.e(TAG, "MLKit batch translation failed", e)
                    return@withContext emptyMap()
                }
            }
            TranslationMode.GEMINI -> {
                try {
                    return@withContext geminiManager.translateBatch(texts, sourceLanguage, targetLanguage)
                } catch (e: Exception) {
                    Log.e(TAG, "Gemini batch translation failed, falling back to Google Free", e)
                    return@withContext googleFreeManager.translateBatch(texts, sourceLanguage, targetLanguage)
                }
            }
            TranslationMode.GOOGLE_FREE -> {
                return@withContext googleFreeManager.translateBatch(texts, sourceLanguage, targetLanguage)
            }
        }
    }

    private fun hasGeminiApiKey(): Boolean {
        val apiKey = appPreferences.TRANSLATION_GEMINI_API_KEY.value
        return apiKey.isNotBlank()
    }

    enum class TranslationMode {
        MLKIT,
        GEMINI,
        GOOGLE_FREE
    }

    companion object {
        private const val TAG = "TranslationComposite"
    }
}
