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
 * Composite translation manager that supports three backends:
 * 1. Gemini (online, requires API key)
 * 2. Google Translate Free (online, no API key)
 * 3. MLKit (offline, requires model download)
 */
class TranslationManagerComposite(
    private val coroutineScope: AppCoroutineScope,
    private val geminiManager: TranslationManagerGemini,
    private val googleFreeManager: TranslationManagerGoogleFree,
    private val mlKitManager: TranslationManagerMlKit,
    private val appPreferences: AppPreferences
) : TranslationManager {

    override val available: Boolean = true

    override val isUsingOnlineTranslation: Boolean
        get() = getActiveMode() != TranslationMode.OFFLINE

    enum class TranslationMode {
        GEMINI,          // Online, API key required
        GOOGLE_FREE,     // Online, no API key
        OFFLINE          // MLKit offline
    }

    override val models = mutableStateListOf<TranslationModelState>()

    init {
        // Merge languages from all providers
        val allLanguages = mutableSetOf<String>()
        allLanguages.addAll(mlKitManager.models.map { it.language })
        allLanguages.addAll(googleFreeManager.models.map { it.language })
        allLanguages.addAll(geminiManager.models.map { it.language })

        // For MLKit languages, use actual download status from mlKitManager.
        // For online-only languages (Gemini/Google Free), available is always true.
        val mlKitDownloadStatus = mlKitManager.models.associate { it.language to it.available }
        Log.d(TAG, "init: mlKitDownloadStatus=$mlKitDownloadStatus")

        models.addAll(allLanguages.map { lang ->
            val isMlKitLang = lang in mlKitDownloadStatus
            TranslationModelState(
                language = lang,
                available = if (isMlKitLang) mlKitDownloadStatus[lang] == true else true,
                downloading = false,
                downloadingFailed = false
            )
        })
    }

    // Sync composite models list with MLKit manager's actual download status
    init {
        mlKitManager.onModelStatusChanged = { language, available, downloading, failed ->
            Log.d(TAG, "onModelStatusChanged: language=$language, available=$available, downloading=$downloading, failed=$failed")
            updateModelStatus(language, available, downloading, failed)
        }
    }

    private fun updateModelStatus(language: String, available: Boolean, downloading: Boolean, failed: Boolean) {
        val index = models.indexOfFirst { it.language == language }
        if (index >= 0) {
            models[index] = models[index].copy(
                available = available,
                downloading = downloading,
                downloadingFailed = failed
            )
        }
    }

    override suspend fun hasModelDownloaded(language: String): TranslationModelState? {
        return models.firstOrNull { it.language == language }
    }

    private fun hasGeminiApiKey(): Boolean {
        val apiKey = appPreferences.TRANSLATION_GEMINI_API_KEY.value
        return apiKey.isNotBlank()
    }

    private fun getActiveMode(): TranslationMode {
        val hasApiKey = hasGeminiApiKey()
        val preferOnline = appPreferences.TRANSLATION_PREFER_ONLINE.value
        val preferOffline = appPreferences.TRANSLATION_PREFER_OFFLINE.value

        return when {
            preferOffline -> TranslationMode.OFFLINE
            hasApiKey && preferOnline -> TranslationMode.GEMINI
            else -> TranslationMode.GOOGLE_FREE
        }
    }

    override fun getTranslator(source: String, target: String): TranslatorState {
        val mode = getActiveMode()
        Log.d(TAG, "getTranslator: source=$source, target=$target, mode=$mode")

        return when (mode) {
            TranslationMode.OFFLINE -> {
                Log.d(TAG, "getTranslator: using MLKit offline translation")
                mlKitManager.getTranslator(source, target)
            }
            TranslationMode.GEMINI -> {
                Log.d(TAG, "getTranslator: using Gemini with Google Free fallback")
                val geminiTranslator = geminiManager.getTranslator(source, target)
                val googleFreeTranslator = googleFreeManager.getTranslator(source, target)

                TranslatorState(
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
            TranslationMode.GOOGLE_FREE -> {
                Log.d(TAG, "getTranslator: using Google Translate Free")
                googleFreeManager.getTranslator(source, target)
            }
        }
    }

    override fun downloadModel(language: String) {
        Log.d(TAG, "downloadModel: composite requested for language=$language")
        // Update composite's model status to show downloading state
        updateModelStatus(language, available = false, downloading = true, failed = false)

        mlKitManager.downloadModel(language)
    }

    override fun removeModel(language: String) {
        mlKitManager.removeModel(language)

        // Update composite's model status
        updateModelStatus(language, available = false, downloading = false, failed = false)
    }

    fun invalidateCacheFor(sourceLanguage: String, targetLanguage: String, text: String? = null) {
        Log.d(TAG, "invalidateCacheFor: delegating to all managers")
        geminiManager.invalidateCacheFor(sourceLanguage, targetLanguage, text)
        googleFreeManager.invalidateCacheFor(sourceLanguage, targetLanguage, text)
        mlKitManager.invalidateCacheFor(sourceLanguage, targetLanguage, text)
    }

    fun getActiveTranslatorName(): String {
        return when (getActiveMode()) {
            TranslationMode.GEMINI -> "Google Gemini API"
            TranslationMode.GOOGLE_FREE -> "Google Translate (Free)"
            TranslationMode.OFFLINE -> "MLKit (Offline)"
        }
    }

    override suspend fun translateBatch(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val mode = getActiveMode()

        return@withContext when (mode) {
            TranslationMode.OFFLINE -> {
                Log.d(TAG, "translateBatch: using MLKit")
                mlKitManager.translateBatch(texts, sourceLanguage, targetLanguage)
            }
            TranslationMode.GEMINI -> {
                Log.d(TAG, "translateBatch: using Gemini")
                try {
                    geminiManager.translateBatch(texts, sourceLanguage, targetLanguage)
                } catch (e: Exception) {
                    Log.e(TAG, "translateBatch: Gemini failed, falling back to Google Free", e)
                    googleFreeManager.translateBatch(texts, sourceLanguage, targetLanguage)
                }
            }
            TranslationMode.GOOGLE_FREE -> {
                Log.d(TAG, "translateBatch: using Google Translate Free")
                googleFreeManager.translateBatch(texts, sourceLanguage, targetLanguage)
            }
        }
    }

    companion object {
        private const val TAG = "TranslationComposite"
    }
}
