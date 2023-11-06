package com.example.fileleap.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.fileleap.ui.webrtc.WebRTCConnection

class WebRTCViewModel(application: Application): AndroidViewModel(application) {

    private val webRTCConnection = WebRTCConnection(application.applicationContext)

    fun initializeSenderConnection(){
        webRTCConnection.initializeSenderPeerConnection()
    }

    fun initializeReceiverConnection(){
        webRTCConnection.initializeReceiverPeerConnection()
    }

    fun closeConnection(){
        webRTCConnection.close()
    }


}