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

@SuppressLint("SetJavaScriptEnabled")
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
        webView = WebView(context)
        webView.settings.javaScriptEnabled = true
        val customWebViewClient = CustomWebViewClient()
        webView.webViewClient = customWebViewClient
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
        methodChannel = MethodChannel(messenger, "plugins.example/flutter_web_view_$id")
        methodChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(methodCall: MethodCall, result: MethodChannel.Result) {
        when (methodCall.method) {
            "setUrl" -> setUrl(methodCall, result)
            else -> result.notImplemented()
        }
    }

    private fun setUrl(methodCall: MethodCall, result: MethodChannel.Result) {
        val url = methodCall.arguments as String
        val cookieString = "is_app=1; path=/; secure; HttpOnly"
        CookieManager.getInstance().setCookie(url, cookieString)
        webView.loadUrl(url)
        result.success(null)
    }

    override fun dispose() {
        webView.destroy()
    }

    private inner class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, Url: String): Boolean {
            view.loadUrl(Url)
            return true

        }
    }
}