plugins {
    alias(libs.plugins.noveldokusha.android.library)
    alias(libs.plugins.noveldokusha.android.compose)
}

android {
    namespace = "my.noveldokusha.text_translator"
}

dependencies {
    implementation(projects.core)
    implementation(projects.features.textTranslator.domain)

    // Needed to have the Task -> await extension.
    implementation(libs.kotlinx.coroutines.playServices)
    // Android ML Translation Kit
    implementation(libs.translate)
}