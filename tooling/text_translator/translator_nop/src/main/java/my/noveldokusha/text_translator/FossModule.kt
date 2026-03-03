package my.noveldokusha.text_translator

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.noveldokusha.core.AppCoroutineScope
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.text_translator.domain.TranslationManager
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FossModule {

    @Provides
    @Singleton
    fun provideTranslationManager(
        context: Context,
        appCoroutineScope: AppCoroutineScope,
        appPreferences: AppPreferences
    ): TranslationManager {
        // Create all three managers
        val geminiManager = TranslationManagerGemini(appCoroutineScope, appPreferences)
        val googleFreeManager = TranslationManagerGoogleFree(appCoroutineScope)
        val mlKitManager = TranslationManagerMLKit(context, appCoroutineScope, appPreferences)

        // Use composite to manage all three translation methods
        return TranslationManagerComposite(
            appCoroutineScope,
            geminiManager,
            googleFreeManager,
            mlKitManager,
            appPreferences
        )
    }
}