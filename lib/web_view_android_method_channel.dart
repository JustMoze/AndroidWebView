import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'web_view_android_platform_interface.dart';

/// An implementation of [WebViewAndroidPlatform] that uses method channels.
class MethodChannelWebViewAndroid extends WebViewAndroidPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('web_view_android');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
