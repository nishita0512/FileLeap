package com.example.fileleap.ui.webrtc

import android.content.Context
import org.webrtc.*
import java.nio.ByteBuffer

class WebRTCConnection private constructor(c: Context) {

    companion object {
        private var rtConnection: WebRTCConnection? = null
        private lateinit var context: Context

        @Synchronized
        fun getInstance(context: Context): WebRTCConnection {
            if (rtConnection == null) {
                rtConnection = WebRTCConnection(context)
            }
            return rtConnection!!
        }
    }

    private var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null

    init {
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options)
            .createPeerConnectionFactory()

        val iceServers = mutableListOf<PeerConnection.IceServer>()
        iceServers.add(PeerConnection.IceServer.builder("stun:stun2.1.google.com:19302").createIceServer())
        iceServers.add(PeerConnection.IceServer.builder("turn:numb.viagenie.ca")
            .setUsername("webrtc@live.com")
            .setPassword("muazkh")
            .createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : CustomPeerConnectionObserver() {
            override fun onIceCandidate(candidate: IceCandidate) {
                super.onIceCandidate(candidate)
                // Handle onIceCandidate
            }

            override fun onDataChannel(p0: DataChannel?) {
                super.onDataChannel(dataChannel)
                // Handle onDataChannel
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                super.onIceConnectionChange(p0)
                // Handle onIceConnectionChange
            }
        })

        if (peerConnection == null) {
            // Handle case where peerConnection is null
        }

        val dcInit = DataChannel.Init()
        dataChannel = peerConnection?.createDataChannel("1", dcInit)
        dataChannel?.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(p0: Long) {

            }

            override fun onStateChange() {

            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                // Handle onMessage
            }
        })
    }

    fun createOffer() {
        if (peerConnection == null) {
            // Handle case where peerConnection is null
            return
        }

        peerConnection?.createOffer(object : CustomSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                // Handle onCreateSuccess
            }

            override fun onCreateFailure(p0: String?) {
                super.onCreateFailure(p0)
                // Handle onCreateFailure
            }
        }, MediaConstraints())
    }

    fun setRemoteAnswer(sessionDescription: String) {
        if (peerConnection == null) {
            // Handle case where peerConnection is null
            return
        }

        peerConnection?.setRemoteDescription(CustomSdpObserver(), SessionDescription(SessionDescription.Type.ANSWER, sessionDescription))
    }

    fun setIceCandidate(candidate: String, sdpmid: String, sdpmlineIndex: Int) {
        if (peerConnection == null) {
            // Handle case where peerConnection is null
            return
        }

        peerConnection?.addIceCandidate(IceCandidate(sdpmid, sdpmlineIndex, candidate))
    }

    fun sendData(message: String) {
        if (peerConnection == null || dataChannel == null) {
            // Handle case where peerConnection or dataChannel is null
            return
        }

        val buffer = ByteBuffer.wrap(message.toByteArray())
        dataChannel?.send(DataChannel.Buffer(buffer, false))
    }

    fun close() {
        if (peerConnection == null) {
            return
        }

        if (dataChannel == null) {
            peerConnection?.close()
            peerConnection = null
        } else {
            dataChannel?.close()
            peerConnection?.close()
            peerConnection = null
        }
    }
}