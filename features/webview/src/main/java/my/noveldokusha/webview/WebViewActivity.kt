package my.noveldokusha.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import my.noveldoksuha.coreui.theme.Theme
import my.noveldoksuha.coreui.theme.ThemeProvider
import my.noveldokusha.core.Toasty
import my.noveldokusha.core.utils.Extra_String
import my.noveldokusha.network.toUrl
import javax.inject.Inject

@AndroidEntryPoint
class WebViewActivity : ComponentActivity() {

    @Inject
    lateinit var toasty: Toasty

    @Inject
    lateinit var themeProvider: ThemeProvider

    class IntentData : Intent {
        var url by Extra_String()

        constructor(intent: Intent) : super(intent)
        constructor(ctx: Context, url: String) : super(ctx, WebViewActivity::class.java) {
            this.url = url
        }
    }

    private val extras by lazy { IntentData(intent) }
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WEBVIEW)) {
            toasty.show(R.string.web_view_not_available)
            finish()
            return
        }

        extras.url.toUrl()?.authority ?: run {
            toasty.show(R.string.invalid_URL)
            finish()
            return
        }

        // Create progress bar (shown while page loads)
        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
            isVisible = true
        }

        webView = WebView(this).apply {
            setupWebViewSettings()
            setInitialScale(100)

            // Add progress bar as overlay
            addView(progressBar, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                setMargins(0, 100, 0, 0)
            })

            loadUrl(extras.url)
        }

        setContent {
            Theme(themeProvider = themeProvider) {
                WebViewScreen(
                    toolbarTitle = extras.url,
                    webViewFactory = { webView },
                    onBackClicked = { @Suppress("DEPRECATION") this@WebViewActivity.onBackPressed() },
                    onReloadClicked = { webView.reload() }
                )
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun WebView.setupWebViewSettings() {
        with(settings) {
            // Enable JavaScript (required for Cloudflare challenge)
            javaScriptEnabled = true

            // Enable DOM storage (required for Cloudflare to persist challenge state)
            domStorageEnabled = true
            databaseEnabled = true

            // Enable mixed content (HTTP resources on HTTPS pages)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            // Enable caching for better performance
            cacheMode = WebSettings.LOAD_DEFAULT

            // Set proper User-Agent
            useWideViewPort = true
            loadWithOverviewMode = true

            // Enable zoom for user convenience
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (view != null && url != null) {
                    progressBar.isVisible = false
                    toasty.show(R.string.cookies_saved)
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    progressBar.isVisible = false
                }
            }

            @Suppress("DEPRECATION")
            @Deprecated("Deprecated in Android API level 23, use onReceivedError(WebResourceRequest) instead")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                progressBar.isVisible = false
            }
        }
    }

    override fun onDestroy() {
        try {
            webView.removeView(progressBar)
            webView.removeAllViews()
            webView.destroy()
        } catch (e: Exception) {
            // Ignore WebView cleanup errors
        }
        super.onDestroy()
    }
}