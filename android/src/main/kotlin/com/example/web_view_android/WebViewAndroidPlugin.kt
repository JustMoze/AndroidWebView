package com.example.web_view_android

import android.app.Activity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.platform.PlatformViewRegistry

class WebViewAndroidPlugin : FlutterPlugin, ActivityAware {

    private lateinit var messenger: BinaryMessenger
    private lateinit var platformViewRegistry: PlatformViewRegistry
    private var activity: Activity? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        messenger = binding.binaryMessenger
        platformViewRegistry = binding.platformViewRegistry
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        registerWebViewFactory()
    }

    private fun registerWebViewFactory() {
        if (activity != null) {
            val factory = WebViewFactory(messenger, activity!!)
            platformViewRegistry.registerViewFactory("plugins.example/flutter_web_view", factory)
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        // Clean up any necessary resources
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }
}