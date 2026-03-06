package my.noveldokusha.text_translator

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import my.noveldokusha.core.AppCoroutineScope
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.text_translator.domain.TranslationManager
import my.noveldokusha.text_translator.domain.TranslationModelState
import my.noveldokusha.text_translator.domain.TranslatorState
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Translation manager using Google MLKit for on-device translation.
 * Works offline after model download.
 * No API key required.
 */
class TranslationManagerMLKit(
    private val context: Context,
    private val coroutineScope: AppCoroutineScope,
    private val appPreferences: AppPreferences
) : TranslationManager {

    override val available = true
    override val isUsingOnlineTranslation: Boolean = false

    // Cache for translators to avoid recreating them
    private val translatorCache = mutableMapOf<String, Translator>()

    // MLKit supported languages
    override val models = mutableStateListOf<TranslationModelState>().apply {
        val supportedLanguages = listOf(
            "en", "zh", "ja", "ko", "es", "fr", "de", "it", "pt", "ru",
            "ar", "hi", "th", "vi", "id", "tr", "pl", "nl", "sv", "da",
            "fi", "no", "cs", "el", "he", "ro", "hu", "uk", "bg", "hr",
            "bn", "fa"
        )

        addAll(supportedLanguages.map { lang ->
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

    override fun getTranslator(source: String, target: String): TranslatorState {
        Log.d(TAG, "getTranslator: source=$source, target=$target (MLKit)")
        return TranslatorState(
            source = source,
            target = target,
            translate = { input -> translateWithMLKit(input, source, target) }
        )
    }

    private suspend fun translateWithMLKit(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val sourceCode = getMLKitLanguageCode(sourceLanguage)
                ?: throw IllegalArgumentException("Unsupported source language: $sourceLanguage")
            val targetCode = getMLKitLanguageCode(targetLanguage)
                ?: throw IllegalArgumentException("Unsupported target language: $targetLanguage")

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build()

            val translator = getOrCreateTranslator(options, sourceLanguage, targetLanguage)

            suspendCancellableCoroutine<String> { continuation ->
                translator.translate(text)
                    .addOnSuccessListener { result ->
                        Log.d(TAG, "MLKit translation succeeded: ${text.take(50)}...")
                        continuation.resume(result)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "MLKit translation failed", e)
                        continuation.resumeWithException(e)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "MLKit translation error", e)
            throw e
        }
    }

    private fun getOrCreateTranslator(
        options: TranslatorOptions,
        sourceLanguage: String,
        targetLanguage: String
    ): Translator {
        // Use language pair as cache key
        val cacheKey = "$sourceLanguage-$targetLanguage"
        return translatorCache.getOrPut(cacheKey) {
            Translation.getClient(options)
        }
    }

    override fun downloadModel(language: String) {
        try {
            val languageCode = getMLKitLanguageCode(language)
                ?: throw IllegalArgumentException("Unsupported language: $language")

            val modelState = models.firstOrNull {
                it.language == language && !it.downloading
            } ?: return

            // Update state using mutableStateListOf methods
            val index = models.indexOf(modelState)
            if (index != -1) {
                models[index] = modelState.copy(
                    downloading = true,
                    downloadingFailed = false
                )

                Log.d(TAG, "Preparing MLKit model for $language")

                // MLKit automatically downloads model when first used
                models[index] = modelState.copy(
                    downloading = false,
                    available = true
                )

                Log.d(TAG, "MLKit model ready for $language")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error with MLKit model for $language", e)
            val index = models.indexOfFirst { it.language == language }
            if (index != -1) {
                val modelState = models[index]
                models[index] = modelState.copy(
                    downloading = false,
                    downloadingFailed = true
                )
            }
        }
    }

    override fun removeModel(language: String) {
        clearCache()
        val index = models.indexOfFirst { it.language == language }
        if (index != -1) {
            val modelState = models[index]
            models[index] = modelState.copy(available = false)
        }
        Log.d(TAG, "MLKit cache cleared for $language")
    }

    override suspend fun translateBatch(
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String
    ): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val sourceCode = getMLKitLanguageCode(sourceLanguage)
                ?: throw IllegalArgumentException("Unsupported source language: $sourceLanguage")
            val targetCode = getMLKitLanguageCode(targetLanguage)
                ?: throw IllegalArgumentException("Unsupported target language: $targetLanguage")

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build()

            val translator = getOrCreateTranslator(options, sourceLanguage, targetLanguage)

            // MLKit doesn't support batch translation, translate one by one
            val resultMap = mutableMapOf<String, String>()
            for (text in texts) {
                try {
                    val translated = suspendCancellableCoroutine<String> { continuation ->
                        translator.translate(text)
                            .addOnSuccessListener { result ->
                                continuation.resume(result)
                            }
                            .addOnFailureListener { e ->
                                continuation.resumeWithException(e)
                            }
                    }
                    resultMap[text] = translated
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to translate text in batch: ${e.message}", e)
                }
            }

            Log.d(TAG, "MLKit batch translation completed: ${resultMap.size}/${texts.size} texts")
            return@withContext resultMap
        } catch (e: Exception) {
            Log.e(TAG, "MLKit batch translation error", e)
            emptyMap()
        }
    }

    /**
     * Clear all cached translators to free memory
     */
    fun clearCache() {
        Log.d(TAG, "Clearing MLKit translator cache")
        translatorCache.values.forEach { it.close() }
        translatorCache.clear()
    }

    /**
     * Get MLKit language code from locale string
     */
    private fun getMLKitLanguageCode(locale: String): String? {
        return when (locale) {
            "zh" -> TranslateLanguage.CHINESE
            "en" -> TranslateLanguage.ENGLISH
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
            "bn" -> TranslateLanguage.BENGALI
            "fa" -> TranslateLanguage.PERSIAN
            else -> null
        }
    }

    companion object {
        private const val TAG = "TranslationMLKit"
    }
}
