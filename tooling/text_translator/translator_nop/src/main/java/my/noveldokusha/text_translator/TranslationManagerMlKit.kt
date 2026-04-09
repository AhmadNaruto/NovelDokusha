package my.noveldokusha.text_translator

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import my.noveldokusha.core.AppCoroutineScope
import my.noveldokusha.text_translator.domain.TranslationManager
import my.noveldokusha.text_translator.domain.TranslationModelState
import my.noveldokusha.text_translator.domain.TranslatorState
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Translation manager using Google MLKit on-device translation.
 * Requires downloading language models, works offline after download.
 */
@Singleton
class TranslationManagerMlKit @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineScope: AppCoroutineScope
) : TranslationManager {

    override val available = true
    override val isUsingOnlineTranslation: Boolean
        get() = false

    private val modelManager = RemoteModelManager.getInstance()

    // Callback for when model status changes (used by composite to sync)
    var onModelStatusChanged: ((String, Boolean, Boolean, Boolean) -> Unit)? = null

    // Cache for downloaded languages (tracked manually since MLKit has no sync query API)
    private val downloadedLanguages = mutableSetOf<String>()

    // Track downloading state
    private val downloadingLanguages = mutableSetOf<String>()
    private val failedLanguages = mutableSetOf<String>()

    // Map of BCP-47 language codes to MLKit TranslateLanguage constants
    private fun toMlKitLanguage(languageCode: String): String? {
        return when (languageCode) {
            "en" -> TranslateLanguage.ENGLISH
            "zh" -> TranslateLanguage.CHINESE
            "ja" -> TranslateLanguage.JAPANESE
            "ko" -> TranslateLanguage.KOREAN
            "es" -> TranslateLanguage.SPANISH
            "fr" -> TranslateLanguage.FRENCH
            "de" -> TranslateLanguage.GERMAN
            "it" -> TranslateLanguage.ITALIAN
            "pt" -> TranslateLanguage.PORTUGUESE
            "ru" -> TranslateLanguage.RUSSIAN
            "ar" -> TranslateLanguage.ARABIC
            "hi" -> TranslateLanguage.HINDI
            "th" -> TranslateLanguage.THAI
            "vi" -> TranslateLanguage.VIETNAMESE
            "id" -> TranslateLanguage.INDONESIAN
            "tr" -> TranslateLanguage.TURKISH
            "pl" -> TranslateLanguage.POLISH
            "nl" -> TranslateLanguage.DUTCH
            "sv" -> TranslateLanguage.SWEDISH
            "da" -> TranslateLanguage.DANISH
            "fi" -> TranslateLanguage.FINNISH
            "no" -> TranslateLanguage.NORWEGIAN
            "cs" -> TranslateLanguage.CZECH
            "el" -> TranslateLanguage.GREEK
            "he" -> TranslateLanguage.HEBREW
            "ro" -> TranslateLanguage.ROMANIAN
            "hu" -> TranslateLanguage.HUNGARIAN
            "uk" -> TranslateLanguage.UKRAINIAN
            "bg" -> TranslateLanguage.BULGARIAN
            "hr" -> TranslateLanguage.CROATIAN
            "ms" -> TranslateLanguage.MALAY
            "tl" -> TranslateLanguage.TAGALOG
            "bn" -> TranslateLanguage.BENGALI
            "fa" -> TranslateLanguage.PERSIAN
            "ta" -> TranslateLanguage.TAMIL
            "te" -> TranslateLanguage.TELUGU
            "ur" -> TranslateLanguage.URDU
            else -> null
        }
    }

    // Supported languages (MLKit supports ~59 languages)
    private val supportedLanguages = listOf(
        "en", "zh", "ja", "ko", "es", "fr", "de", "it", "pt", "ru",
        "ar", "hi", "th", "vi", "id", "tr", "pl", "nl", "sv", "da",
        "fi", "no", "cs", "el", "he", "ro", "hu", "uk", "bg", "hr",
        "ms", "tl", "bn", "fa", "ta", "te", "ur"
    )

    override val models = mutableStateListOf<TranslationModelState>().apply {
        addAll(supportedLanguages.map { lang ->
            val isDownloaded = lang in downloadedLanguages
            TranslationModelState(
                language = lang,
                available = isDownloaded,
                downloading = false,
                downloadingFailed = false
            )
        })
        Log.d(TAG, "models: initialized with ${size} languages, downloaded: ${downloadedLanguages}")
    }

    override suspend fun hasModelDownloaded(language: String): TranslationModelState? {
        return models.firstOrNull { it.language == language }
    }

    override fun getTranslator(source: String, target: String): TranslatorState {
        val mlSource = toMlKitLanguage(source) ?: source
        val mlTarget = toMlKitLanguage(target) ?: target

        return TranslatorState(
            source = source,
            target = target,
            translate = { input -> translateWithMlKit(input, mlSource, mlTarget) }
        )
    }

    private suspend fun translateWithMlKit(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()

            val translator = Translation.getClient(options)

            val result = translator.translate(text).await()
            translator.close()
            Log.d(TAG, "MLKit translation succeeded, result length=${result.length}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "MLKit translation failed: ${e.message}", e)
            "[Translation error: ${e.message?.take(50) ?: "unknown"}]"
        }
    }

    override fun downloadModel(language: String) {
        Log.d(TAG, "downloadModel: requested language=$language")
        if (language in downloadingLanguages) {
            Log.w(TAG, "downloadModel: already downloading $language, skipping")
            return
        }

        downloadingLanguages.add(language)
        failedLanguages.remove(language)

        // Update UI state
        val index = models.indexOfFirst { it.language == language }
        Log.d(TAG, "downloadModel: found model index=$index for language=$language")
        if (index >= 0) {
            models[index] = models[index].copy(downloading = true, downloadingFailed = false)
        }

        coroutineScope.launch {
            try {
                Log.d(TAG, "downloadModel: starting download for $language")
                val conditions = DownloadConditions.Builder().build()

                // Download the MLKit translate model for this language
                val mlSource = toMlKitLanguage(language) ?: language
                Log.d(TAG, "downloadModel: mlSource=$mlSource for language=$language")

                val model = TranslateRemoteModel.Builder(mlSource).build()
                modelManager.download(model, conditions).await()
                Log.d(TAG, "downloadModel: download completed for $language")

                // Mark as downloaded
                downloadedLanguages.add(language)
                downloadingLanguages.remove(language)

                val idx = models.indexOfFirst { it.language == language }
                if (idx >= 0) {
                    models[idx] = models[idx].copy(
                        available = true,
                        downloading = false,
                        downloadingFailed = false
                    )
                }
                onModelStatusChanged?.invoke(language, true, false, false)
                Log.d(TAG, "MLKit model downloaded for language: $language")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download MLKit model for $language: ${e.message}", e)
                downloadingLanguages.remove(language)
                failedLanguages.add(language)
                val idx = models.indexOfFirst { it.language == language }
                if (idx >= 0) {
                    models[idx] = models[idx].copy(
                        available = false,
                        downloading = false,
                        downloadingFailed = true
                    )
                }
                onModelStatusChanged?.invoke(language, false, false, true)
            }
        }
    }

    override fun removeModel(language: String) {
        coroutineScope.launch {
            try {
                // Remove translator instances for this language
                val keysToRemove = translatorCache.keys.filter { key ->
                    key.startsWith("$language-") || key.endsWith("-$language")
                }
                keysToRemove.forEach { translatorCache[it]?.close() }
                translatorCache.keys.removeAll(keysToRemove.toSet())

                downloadedLanguages.remove(language)

                Log.d(TAG, "MLKit models removed for language: $language")

                val idx = models.indexOfFirst { it.language == language }
                if (idx >= 0) {
                    models[idx] = models[idx].copy(available = false)
                }
                onModelStatusChanged?.invoke(language, false, false, false)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove MLKit model for $language: ${e.message}", e)
            }
        }
    }

    override suspend fun translateBatch(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String
    ): Map<String, String> = withContext(Dispatchers.IO) {
        if (texts.isEmpty()) return@withContext emptyMap()

        val mlSource = toMlKitLanguage(sourceLanguage) ?: sourceLanguage
        val mlTarget = toMlKitLanguage(targetLanguage) ?: targetLanguage

        val translations = mutableMapOf<String, String>()

        // Translate each text individually (MLKit doesn't support batch)
        texts.forEach { text ->
            try {
                val result = translateWithMlKit(text, mlSource, mlTarget)
                translations[text] = result
            } catch (e: Exception) {
                Log.e(TAG, "Batch translation failed for text: ${e.message}")
                translations[text] = text
            }
        }

        return@withContext translations
    }

    /**
     * MLKit doesn't support batch translation natively
     * Returns null to indicate no native batch support
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun translateBatchNative(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String
    ): Map<String, String>? = null

    @Suppress("UNUSED_PARAMETER")
    fun invalidateCacheFor(sourceLanguage: String, targetLanguage: String, text: String? = null) {
        // MLKit doesn't use a text cache, translators are created on demand
        val key = "$sourceLanguage-$targetLanguage"
        translatorCache[key]?.close()
        translatorCache.remove(key)
    }

    companion object {
        private const val TAG = "TranslationMLKit"
    }

    // Cache for translators (not text translations, but Translator instances)
    private val translatorCache: ConcurrentHashMap<String, com.google.mlkit.nl.translate.Translator> = ConcurrentHashMap()
}
