#include "include/web_view_android/web_view_android_plugin_c_api.h"

#include <flutter/plugin_registrar_windows.h>

#include "web_view_android_plugin.h"

void WebViewAndroidPluginCApiRegisterWithRegistrar(
    FlutterDesktopPluginRegistrarRef registrar) {
  web_view_android::WebViewAndroidPlugin::RegisterWithRegistrar(
      flutter::PluginRegistrarManager::GetInstance()
          ->GetRegistrar<flutter::PluginRegistrarWindows>(registrar));
}
