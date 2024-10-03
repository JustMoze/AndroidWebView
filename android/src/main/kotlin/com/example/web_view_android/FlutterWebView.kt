package com.example.web_view_android

import android.content.Context
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.platform.PlatformView
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.JavascriptInterface
import org.json.JSONObject
import android.os.Handler
import android.os.Looper

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

        webView.webViewClient = WebViewClient()

        CookieManager.getInstance().setAcceptCookie(true)

        webView.addJavascriptInterface(WebAppInterface(context), "Android")

        methodChannel = MethodChannel(messenger, "plugins.example/flutter_web_view_$id")
        methodChannel.setMethodCallHandler(this)
    }

    override fun getView(): View {
        return webView
    }

    override fun onMethodCall(methodCall: MethodCall, result: MethodChannel.Result) {
        when (methodCall.method) {
            "setUrl" -> setText(methodCall, result)
            "evaluateJavascript" -> evaluateJavascript(methodCall, result)
            else -> result.notImplemented()
        }
    }

    private fun setText(methodCall: MethodCall, result: MethodChannel.Result) {
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
}