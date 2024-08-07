package my.noveldokusha.text_translator

import androidx.compose.runtime.mutableStateListOf
import my.noveldokusha.text_translator.domain.TranslationManager
import my.noveldokusha.text_translator.domain.TranslationModelState
import my.noveldokusha.text_translator.domain.TranslatorState

class TranslationManagerEmpty : TranslationManager {

    override val available = false
    override val models = mutableStateListOf<TranslationModelState>()
    override suspend fun hasModelDownloaded(language: String) = null
    override fun getTranslator(
        source: String,
        target: String
    ): TranslatorState = TranslatorState(
        source = source,
        target = target,
        translate = { _ -> "" },
    )

    override fun downloadModel(language: String) = Unit
    override fun removeModel(language: String) = Unit
}