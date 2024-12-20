import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef FlutterWebViewCreatedCallback = void Function(
    WebViewController controller);

class WebView extends StatelessWidget {
  final FlutterWebViewCreatedCallback onMapViewCreated;

  const WebView({
    Key? key,
    required this.onMapViewCreated,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return AndroidView(
          viewType: "plugins.example/flutter_web_view",
          onPlatformViewCreated: _onPlatformViewCreated,
        );
      default:
        return Text(
            '$defaultTargetPlatform is not yet supported by the web_view plugin');
    }
  }

  // Callback method when platform view is created
  void _onPlatformViewCreated(int id) =>
      onMapViewCreated(WebViewController._(id));
}

// WebView Controller class to set url etc
class WebViewController {
  WebViewController._(int id)
      : _channel = MethodChannel('plugins.example/flutter_web_view_$id');

  final MethodChannel _channel;

  Future<void> setUrl({required String url}) async =>
      _channel.invokeMethod('setUrl', url);

  Future<void> setWebViewListener(
    ValueChanged<Map<String, dynamic>> listener,
  ) async =>
      _channel.setMethodCallHandler((call) async {
        if (call.method == 'onAction') {
          String action = call.arguments['action'];
          String? token = call.arguments['token'];

          listener({'action': action, 'token': token});
        }
      });

  Future<void> evaluateJavascript(String script) async =>
      _channel.invokeMethod('evaluateJavascript', script);
}
