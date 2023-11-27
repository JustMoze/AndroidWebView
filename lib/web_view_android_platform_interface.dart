import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'web_view_android_method_channel.dart';

abstract class WebViewAndroidPlatform extends PlatformInterface {
  /// Constructs a WebViewAndroidPlatform.
  WebViewAndroidPlatform() : super(token: _token);

  static final Object _token = Object();

  static WebViewAndroidPlatform _instance = MethodChannelWebViewAndroid();

  /// The default instance of [WebViewAndroidPlatform] to use.
  ///
  /// Defaults to [MethodChannelWebViewAndroid].
  static WebViewAndroidPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [WebViewAndroidPlatform] when
  /// they register themselves.
  static set instance(WebViewAndroidPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
