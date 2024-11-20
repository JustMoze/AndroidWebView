package com.example.web_view_android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import android.os.Environment
import android.view.View
import android.webkit.CookieManager
import android.widget.Toast
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceRequest
import org.json.JSONObject
import android.util.Log
import io.flutter.embedding.android.FlutterActivity

class FlutterWebView(
    val activity: Activity,
    messenger: BinaryMessenger,
    viewId: Int,
    args: Any?
) : PlatformView, MethodChannel.MethodCallHandler {

    private val webView: WebView = WebView(activity)
    private val methodChannel: MethodChannel
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val FILECHOOSER_RESULTCODE = 1001
    private val REQUEST_SELECT_FILE = 1002
    private val TAG = "FlutterWebView"

    init {
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.setSupportMultipleWindows(true)
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.webChromeClient = MyWebChromeClient(activity)
        webView.webViewClient = MyWebViewClient()

        CookieManager.getInstance().setAcceptCookie(true)

        // Handle file downloads
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalFilesDir(
                activity,
                Environment.DIRECTORY_DOWNLOADS,
                ".png"
            )
            val dm = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(activity, "Downloading File", Toast.LENGTH_LONG).show()
        }

        webView.addJavascriptInterface(WebAppInterface(activity), "Android")
        methodChannel = MethodChannel(messenger, "plugins.example/flutter_web_view_$viewId")
        methodChannel.setMethodCallHandler(this)
    }

    override fun getView(): View = webView

    override fun onMethodCall(methodCall: MethodCall, result: MethodChannel.Result) {
        when (methodCall.method) {
            "setUrl" -> setUrl(methodCall, result)
            "evaluateJavascript" -> evaluateJavascript(methodCall, result)
            "clearCache" -> {
                clearWebViewCache()
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    private fun clearWebViewCache() {
        webView.clearCache(true)
    }

    private fun setUrl(methodCall: MethodCall, result: MethodChannel.Result) {
        val url = methodCall.arguments as String
        webView.loadUrl(url)
        result.success(null)
    }

    private fun evaluateJavascript(methodCall: MethodCall, result: MethodChannel.Result) {
        val script = methodCall.arguments as String
        webView.evaluateJavascript(script) { value ->
            result.success(value)
        }
    }

    inner class WebAppInterface(private val mContext: Context) {
        @JavascriptInterface
        fun postMessage(message: String) {
            val json = JSONObject(message)
            val action = json.optString("action")
            val token = json.optString("token", null)

            val arguments: Map<String, String?> = mapOf("action" to action, "token" to token)
            Log.d("WebAppInterface", "Arguments: $arguments")
            Handler(Looper.getMainLooper()).post {
                methodChannel.invokeMethod("onAction", arguments)
            }
        }
    }

    override fun dispose() {
        webView.destroy()
    }

    private inner class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            view.loadUrl(request.url.toString())
            return true
        }
    }

    inner class MyWebChromeClient(private val myActivity: Activity) : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            Log.d("FlutterWebView", "onShowFileChooser triggered")

            if (fileChooserParams == null) {
                Log.e("FlutterWebView", "fileChooserParams is null!")
                return false
            }

            val contentIntent = fileChooserParams.createIntent()

            if (contentIntent == null) {
                Log.e("FlutterWebView", "Failed to create file chooser intent!")
                return false
            }

            // Call the method in MainActivity to launch the file chooser
            Log.d("FlutterWebView", "Activity is instance of FileChooserLauncher: ${activity is FileChooserLauncher}")
            Log.d("FlutterWebView", "Activity class: ${activity?.javaClass?.simpleName}")
            (activity as? FileChooserLauncher)?.launchFileChooser(filePathCallback, contentIntent)
            return true
        }
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url.toString()
            Log.d("MainActivity", "URL being loaded: $url")

            // If it's a URL that should open externally, handle it, else load in WebView
            if (url.contains("logout") || url.contains("login")) {
                // Allow loading in WebView for these URLs
                return false
            }

            // Load the URL in WebView itself
            view?.loadUrl(url)
            return true
        }
    }
}