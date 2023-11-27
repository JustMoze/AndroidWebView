#ifndef FLUTTER_PLUGIN_WEB_VIEW_ANDROID_PLUGIN_H_
#define FLUTTER_PLUGIN_WEB_VIEW_ANDROID_PLUGIN_H_

#include <flutter/method_channel.h>
#include <flutter/plugin_registrar_windows.h>

#include <memory>

namespace web_view_android {

class WebViewAndroidPlugin : public flutter::Plugin {
 public:
  static void RegisterWithRegistrar(flutter::PluginRegistrarWindows *registrar);

  WebViewAndroidPlugin();

  virtual ~WebViewAndroidPlugin();

  // Disallow copy and assign.
  WebViewAndroidPlugin(const WebViewAndroidPlugin&) = delete;
  WebViewAndroidPlugin& operator=(const WebViewAndroidPlugin&) = delete;

  // Called when a method is called on this plugin's channel from Dart.
  void HandleMethodCall(
      const flutter::MethodCall<flutter::EncodableValue> &method_call,
      std::unique_ptr<flutter::MethodResult<flutter::EncodableValue>> result);
};

}  // namespace web_view_android

#endif  // FLUTTER_PLUGIN_WEB_VIEW_ANDROID_PLUGIN_H_
