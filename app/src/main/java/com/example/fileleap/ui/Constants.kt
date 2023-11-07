package com.example.fileleap.ui

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf

object Constants {

    var selectedFile: Uri? = null
    var fileName = ""
    var fileSize = mutableStateOf(0L)
    var bytesReceived = mutableStateOf(0L)
    var bytesSent = mutableStateOf(0L)
    var documentId = ""
    var isSender = false
    val screen = mutableStateOf(0)
    val password = mutableStateOf("")
    val hashedPassword = mutableStateOf("")
    val toastsToShowList = ArrayList<String>()
    val showProgressBar = mutableStateOf(false)
    const val TAG = "FileLeapDebugTag"

}