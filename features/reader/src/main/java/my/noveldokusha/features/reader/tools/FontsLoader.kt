package my.noveldokusha.features.reader.tools

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily

internal class FontsLoader {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    companion object {
        // System fonts
        val systemFonts = listOf(
            "casual",
            "cursive",
            "monospace",
            "sans-serif",
            "sans-serif-black",
            "sans-serif-condensed",
            "sans-serif-condensed-light",
            "sans-serif-light",
            "sans-serif-medium",
            "sans-serif-smallcaps",
            "sans-serif-thin",
            "serif",
            "serif-monospace"
        )

        // Custom fonts from assets/fonts/
        val customFonts = listOf(
            "apollo",
            "aventa",
            "calling_code",
            "cloud_3",
            "comfortaa",
            "coolvetica",
            "creato_display",
            "dejavu-fonts",
            "elysian_glide",
            "geo_sans_light",
            "imperator",
            "louis_george_cafe",
            "made_tommy_soft",
            "modern_sans",
            "moirest",
            "roboto",
            "verily_serif_mono"
        )

        val availableFonts = systemFonts + customFonts
    }

    private val typeFaceNORMALCache = mutableMapOf<String, Typeface>()
    private val typeFaceBOLDCache = mutableMapOf<String, Typeface>()
    private val fontFamilyCache = mutableMapOf<String, FontFamily>()

    fun getTypeFaceNORMAL(name: String): Typeface = typeFaceNORMALCache.getOrPut(name) {
        if (customFonts.contains(name)) {
            loadFromAssets(name) ?: Typeface.create(name, Typeface.NORMAL)
        } else {
            Typeface.create(name, Typeface.NORMAL)
        }
    }

    fun getTypeFaceBOLD(name: String): Typeface = typeFaceBOLDCache.getOrPut(name) {
        if (customFonts.contains(name)) {
            // Try to find a bold variant, fallback to normal if not available
            loadFromAssets(name, Typeface.BOLD) ?: getTypeFaceNORMAL(name)
        } else {
            Typeface.create(name, Typeface.BOLD)
        }
    }

    private fun loadFromAssets(fontName: String, style: Int = Typeface.NORMAL): Typeface? {
        val context = appContext ?: return null

        // Try exact filename matches (.ttf or .otf)
        val extensions = listOf("ttf", "otf")
        for (ext in extensions) {
            val assetPath = "fonts/$fontName.$ext"
            try {
                val typeface = Typeface.createFromAsset(context.assets, assetPath)
                return Typeface.create(typeface, style)
            } catch (e: Exception) {
                // Try next extension
            }
        }
        return null
    }

    fun getFontFamily(name: String) = fontFamilyCache.getOrPut(name) {
        FontFamily(getTypeFaceNORMAL(name))
    }
}