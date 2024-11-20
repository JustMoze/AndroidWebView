package com.example.web_view_android

import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback

interface FileChooserLauncher {
    fun launchFileChooser(filePathCallback: ValueCallback<Array<Uri>>?, intent: Intent)
}