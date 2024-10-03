package com.example.web_view_android

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.View
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.platform.PlatformView
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.JavascriptInterface
import org.json.JSONObject
import android.os.Handler
import android.os.Looper

@SuppressLint("SetJavaScriptEnabled")
class FlutterWebView internal constructor(
    context: Context,
    messenger: BinaryMessenger,
    id: Int
) : PlatformView, MethodCallHandler {
    private val webView: WebView
    private val methodChannel: MethodChannel

    init {
        webView = WebView(context)

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        webView.webViewClient = CustomWebViewClient()
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
                context,
                Environment.DIRECTORY_DOWNLOADS,
                ".png"
            )
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(context, "Downloading File", Toast.LENGTH_LONG).show()
        }

        webView.addJavascriptInterface(WebAppInterface(context), "Android")
        methodChannel = MethodChannel(messenger, "plugins.example/flutter_web_view_$id")
        methodChannel.setMethodCallHandler(this)
    }

    override fun getView(): View {
        return webView
    }

    override fun onMethodCall(methodCall: MethodCall, result: MethodChannel.Result) {
        when (methodCall.method) {
            "setUrl" -> setUrl(methodCall, result)
            "evaluateJavascript" -> evaluateJavascript(methodCall, result)
            else -> result.notImplemented()
        }
    }

    private fun setUrl(methodCall: MethodCall, result: MethodChannel.Result) {
        val url = methodCall.arguments as String
        webView.loadUrl(url)
        result.success(null)
    }

    private fun evaluateJavascript(methodCall: MethodCall, result: MethodChannel.Result) {
        val script = methodCall.arguments as String
        webView.evaluateJavascript(script, ValueCallback { value ->
            result.success(value)
        })
    }

    inner class WebAppInterface(private val mContext: Context) {
        @JavascriptInterface
        fun postMessage(message: String) {
            val json = JSONObject(message)
            val action = json.optString("action")
            val token = json.optString("token", null)

            val arguments = mapOf("action" to action, "token" to token)
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
    }
}