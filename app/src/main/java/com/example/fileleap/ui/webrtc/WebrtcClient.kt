package com.example.fileleap.ui.webrtc

import android.content.Context
import android.util.Log
import com.example.fileleap.ui.utils.DataModel
import com.example.fileleap.ui.utils.DataModelType
import com.google.gson.Gson
import org.webrtc.*
import org.webrtc.PeerConnection.Observer

//class WebrtcClient(context: Context, private val gson: Gson) {
//
//    private lateinit var observer: Observer
//    var listener: Listener? = null
//    var receiverListener : ReceiverListener?=null
//
//    private var peerConnection: PeerConnection? = null
//    private val eglBaseContext = EglBase.create().eglBaseContext
//    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
//    private val mediaConstraint = MediaConstraints().apply {
//        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
//        mandatory.add(MediaConstraints.KeyValuePair("RtpDataChannels", "true"))
//    }
//
//    private val dataChannelObserver = object : DataChannel.Observer {
//        override fun onBufferedAmountChange(p0: Long) {
//
//        }
//
//        override fun onStateChange() {
//        }
//
//        override fun onMessage(p0: DataChannel.Buffer?) {
//            p0?.let { receiverListener?.onDataReceived(it) }
//        }
//
//    }
//
//    private val iceServer = listOf(
//        PeerConnection.IceServer.builder("turn:openrelay.metered.ca:443?transport=tcp")
//            .setUsername("openrelayproject").setPassword("openrelayproject").createIceServer()
//    )
//
//    init {
//        initPeerConnectionFactory(context)
//    }
//
//    fun initializeWebrtcClient(
//        username: String, observer: Observer
//    ) {
//        this.observer = observer
//        peerConnection = createPeerConnection(observer)
//        createDataChannel()
//    }
//
//    private fun createDataChannel(){
//        val initDataChannel = DataChannel.Init()
//        val dataChannel = peerConnection?.createDataChannel("dataChannelLabel",initDataChannel)
//        dataChannel?.registerObserver(dataChannelObserver)
//    }
//
//
//    private fun initPeerConnectionFactory(application: Context) {
//        val options = PeerConnectionFactory.InitializationOptions.builder(application)
//            .setEnableInternalTracer(true).setFieldTrials("WebRTC-H264HighProfile/Enabled/")
//            .createInitializationOptions()
//        PeerConnectionFactory.initialize(options)
//    }
//
//    private fun createPeerConnectionFactory(): PeerConnectionFactory {
//        return PeerConnectionFactory.builder().setVideoDecoderFactory(
//            DefaultVideoDecoderFactory(eglBaseContext)
//        ).setVideoEncoderFactory(
//            DefaultVideoEncoderFactory(
//                eglBaseContext, true, true
//            )
//        ).setOptions(PeerConnectionFactory.Options().apply {
//            disableEncryption = false
//            disableNetworkMonitor = false
//        }).createPeerConnectionFactory()
//    }
//
//    private fun createPeerConnection(observer: Observer): PeerConnection? {
//        return peerConnectionFactory.createPeerConnection(
//            iceServer, observer
//        )
//    }
//diaConstraint)
//    }
//
//    fun onRemoteSessionReceived(sessionDescription: SessionDescription){
//        peerConnection?.setRemoteDescription(MySdpObserver(),sessionDescription)
//    }
//
//    fun addIceCandidate(iceCandidate: IceCandidate){
//        peerConnection?.addIceCandidate(iceCandidate)
//    }
//
//    fun sendIceCandidate(candidate: IceCandidate,target: String){
//        addIceCandidate(candidate)
//        listener?.onTransferEventToSocket(
//            DataModel(
//                type = DataModelType.IceCandidates,
//                target = target,
//                data = gson.toJson(candidate)
//            )
//        )
//    }
//
//    interface Listener {
//        fun onTransferEventToSocket(data: DataModel)
//    }
//
//    interface ReceiverListener{
//        fun onDataReceived(it:DataChannel.Buffer)
//    }
//}
//    fun call(target: String) {
//        peerConnection?.createOffer(object : MySdpObserver() {
//            override fun onCreateSuccess(desc: SessionDescription?) {
//                super.onCreateSuccess(desc)
//                peerConnection?.setLocalDescription(object : MySdpObserver() {
//                    override fun onSetSuccess() {
//                        super.onSetSuccess()
//                        listener?.onTransferEventToSocket(
//                            DataModel(
//                                type = DataModelType.Offer, target, desc?.description
//                            )
//                        )
//                    }
//                }, desc)
//                Log.d("SDP: ",desc?.description.toString())
//            }
//        }, mediaConstraint)
//    }
//
//
//    fun answer(target: String) {
//        peerConnection?.createAnswer(object : MySdpObserver() {
//            override fun onCreateSuccess(desc: SessionDescription?) {
//                super.onCreateSuccess(desc)
//                peerConnection?.setLocalDescription(object : MySdpObserver() {
//                    override fun onSetSuccess() {
//                        super.onSetSuccess()
//                        listener?.onTransferEventToSocket(
//                            DataModel(
//                                type = DataModelType.Answer,
//                                target = target,
//                                data = desc?.description
//                            )
//                        )
//                    }
//                }, desc)
//            }
//        }, mediaConstraint)
//    }
//
//    fun onRemoteSessionReceived(sessionDescription: SessionDescription){
//        peerConnection?.setRemoteDescription(MySdpObserver(),sessionDescription)
//    }
//
//    fun addIceCandidate(iceCandidate: IceCandidate){
//        peerConnection?.addIceCandidate(iceCandidate)
//    }
//
//    fun sendIceCandidate(candidate: IceCandidate,target: String){
//        addIceCandidate(candidate)
//        listener?.onTransferEventToSocket(
//            DataModel(
//                type = DataModelType.IceCandidates,
//                target = target,
//                data = gson.toJson(candidate)
//            )
//        )
//    }
//
//    interface Listener {
//        fun onTransferEventToSocket(data: DataModel)
//    }
//
//    interface ReceiverListener{
//        fun onDataReceived(it:DataChannel.Buffer)
//    }
//}