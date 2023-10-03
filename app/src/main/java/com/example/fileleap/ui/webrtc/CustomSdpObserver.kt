package com.example.fileleap.ui.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class CustomSdpObserver : SdpObserver{
    override fun onCreateSuccess(sessionDescription: SessionDescription) {
    }

    override fun onSetSuccess() {
    }

    override fun onCreateFailure(p0: String?) {
    }

    override fun onSetFailure(p0: String?) {
    }
}