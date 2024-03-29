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


class FlutterWebView internal constructor(
    context: Context,
    messenger: BinaryMessenger,
    id: Int
) :
    PlatformView, MethodCallHandler {
    private val webView: WebView
    private val methodChannel: MethodChannel
    override fun getView(): View {
        return webView
    }

    init {
        // Init WebView
        webView = WebView(context)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        methodChannel = MethodChannel(messenger, "plugins.example/flutter_web_view_$id")
        // Init methodCall Listener
        methodChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(methodCall: MethodCall, result: MethodChannel.Result) {
        when (methodCall.method) {
            "setUrl" -> setText(methodCall, result)
            else -> result.notImplemented()
        }
    }

    // set and load new Url
    private fun setText(methodCall: MethodCall, result: MethodChannel.Result ) {
        val url = methodCall.arguments as String
        val cookieString = "is_app=1; path=/; secure; HttpOnly"
        CookieManager.getInstance().setCookie(url, cookieString)
        webView.loadUrl(url)
        result.success(null)
    }

    // Destroy WebView when PlatformView is destroyed
    override fun dispose() {
        webView.destroy()
    }

}