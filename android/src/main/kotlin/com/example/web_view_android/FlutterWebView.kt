package com.example.web_view_android

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.app.Dialog
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import org.json.JSONObject

class FlutterWebView(
    val activity: Activity,
    messenger: BinaryMessenger,
    viewId: Int,
    args: Any?
) : PlatformView, MethodChannel.MethodCallHandler {

    private val webView: WebView
    private val methodChannel: MethodChannel
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val FILECHOOSER_RESULTCODE = 1001
    private val REQUEST_SELECT_FILE = 1002
    private val TAG = "FlutterWebView"

    init {
        webView = WebView(activity)
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setSupportMultipleWindows(true)
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.webViewClient = MyWebViewClient(activity)
        webView.webChromeClient = MyWebChromeClient(activity)

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            (activity as? FileChooserLauncher)?.launchFileDownload(url)
        }

        CookieManager.getInstance().setAcceptCookie(true)

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

        override fun onCreateWindow(
            view: WebView?,
            dialog: Boolean,
            userGesture: Boolean,
            resultMsg: android.os.Message?
        ): Boolean {
            if (view == null || resultMsg == null) return false

            val newWebView: WebView = WebView(view.context)

            newWebView.settings.javaScriptEnabled = true
            newWebView.webViewClient = MyWebViewClient(myActivity)

            val dialog: Dialog = Dialog(view.context)
            dialog.setContentView(newWebView)
            dialog.show()

            val transport: WebView.WebViewTransport? = resultMsg.obj as? WebView.WebViewTransport
            transport?.webView = newWebView
            resultMsg.sendToTarget()

            return true
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            if (fileChooserParams == null) {
                Log.e("FlutterWebView", "fileChooserParams is null!")
                return false
            }

            val contentIntent = fileChooserParams.createIntent()

            if (contentIntent == null) {
                Log.e("FlutterWebView", "Failed to create file chooser intent!")
                return false
            }

            (activity as? FileChooserLauncher)?.launchFileChooser(filePathCallback, contentIntent)
            return true
        }
    }

    inner class MyWebViewClient(private val mActivity: Activity) : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url.isNullOrBlank()) return false

            if (url.contains("logout") || url.contains("login")) {
                view?.loadUrl(url)
                return true
            }

            view?.loadUrl(url)
            return true
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url: String? = request.getUrl().toString()
            if (url != null) {
                view.loadUrl(url)
                return true
            }
            return false
        }
    }
}