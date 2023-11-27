package com.example.web_view_android

import io.flutter.embedding.engine.plugins.FlutterPlugin

class WebViewAndroidPlugin :FlutterPlugin {

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        binding.platformViewRegistry.registerViewFactory(
            "plugins.example/flutter_web_view", WebViewFactory(binding.binaryMessenger))
    }

    /*
    * onDetachedFromEngine: should release all resources in this method
    * https://api.flutter.dev/javadoc/io/flutter/embedding/engine/plugins/FlutterPlugin.html
    * */
    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        /*
        * Eg: .setMethodCallHandler(null), setStreamHandler(null) etc
        * */
        TODO("Not yet implemented")
    }
}
