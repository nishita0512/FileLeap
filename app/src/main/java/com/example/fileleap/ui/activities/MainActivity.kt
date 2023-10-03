package com.example.fileleap.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import com.example.fileleap.ui.Constants
import com.example.fileleap.ui.theme.FileLeapTheme
import com.example.fileleap.ui.webrtc.CustomDataChannelObserver
import com.example.fileleap.ui.webrtc.CustomPeerConnectionObserver
import com.example.fileleap.ui.webrtc.CustomSdpObserver
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.IceConnectionState
import org.webrtc.PeerConnectionFactory
import org.webrtc.PeerConnectionFactory.InitializationOptions
import org.webrtc.SessionDescription
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

class MainActivity : ComponentActivity() {
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null
    private lateinit var fileOutputStream: FileOutputStream
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir,"test.txt")
        fileOutputStream = FileOutputStream(file, true)

        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(applicationContext)
            .setFieldTrials("WebRTC-IntelVP8/Enabled/")
            .setEnableInternalTracer(true)
            .createInitializationOptions())

        // The rest of your onCreate method
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    Constants.selectedFile = data.data
                    Log.d("Selected File: ", Constants.selectedFile?.path.toString())

                    // Initialize and establish a WebRTC connection
                    initializeSenderPeerConnection()
                }
            }
        }

        firestore = Firebase.firestore // Initialize Firestore

        setContent {
            FileLeapTheme {
                MainScreen(
                    selectFile = {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "*/*"
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        resultLauncher.launch(intent)
                    },
                    receiveFile = {
                        initializeReceiverPeerConnection()
                    }
                )
            }
        }
    }

    private fun initializeSenderPeerConnection(){
        val initializationOptions: InitializationOptions = InitializationOptions.builder(applicationContext).createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)
        val options = PeerConnectionFactory.Options()

        val peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options).createPeerConnectionFactory()
        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)

        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : CustomPeerConnectionObserver() {

                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.d(TAG,"onIceCandidate")
                    // Send ICE candidate to the other device through Firestore
                    if(Constants.documentId=="") {
                        Timer().schedule(object: TimerTask(){
                            override fun run() {
                                sendSenderIceCandidate(candidate)
                            }
                        },2000)
                    }
                    else{
                        sendSenderIceCandidate(candidate)
                    }
                }

                override fun onDataChannel(p0: DataChannel?) {
                    super.onDataChannel(p0)
                    Log.d(TAG,"Sending File... ${p0?.label()}")

                    if(p0 == null){
                        Log.d(TAG,"NULL")
                    }

                    Timer().schedule(object: TimerTask(){
                        override fun run() {
//                            val buffer = ByteBuffer.wrap("Test".encodeToByteArray())
//                            p0!!.send(DataChannel.Buffer(buffer, false))
                            CoroutineScope(Dispatchers.IO).launch {
//                                p0?.send(
//                                    DataChannel.Buffer(
//                                        ByteBuffer.wrap(File(Constants.selectedFile?.path.toString()).readBytes()),
//                                        false
//                                    )
//                                )
                                sendFile(p0!!,Constants.selectedFile!!)
                            }
                        }
                    },2000)

                }

                override fun onIceConnectionChange(p0: IceConnectionState?) {
                    super.onIceConnectionChange(p0)
                    if (p0 != null) {
                        if (p0 == IceConnectionState.CONNECTED) {
                            Log.d(TAG,"Sender Connected")
                        }
                        if (p0 == IceConnectionState.CLOSED) {
                            Log.d(TAG,"Sender Closed")
                        }
                        if (p0 == IceConnectionState.FAILED) {
                            Log.d(TAG,"Sender Failed")
                        }
                    }
                }
            })

        val dataChannelInit = DataChannel.Init()
        dataChannel = peerConnection?.createDataChannel("senderChannel", dataChannelInit)
        dataChannel?.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) {
                // Handle buffered amount change
            }

            override fun onStateChange() {
                // Handle data channel state change
            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                Log.d(TAG,"Received0...")
            }
        })

        peerConnection?.createOffer(object : CustomSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                peerConnection?.setLocalDescription(CustomSdpObserver(), sessionDescription)

                // sessionDescription.description is string which needs to the shared across network
                val offerData = hashMapOf(
                    "type" to sessionDescription.type.canonicalForm(),
                    "sdp" to sessionDescription.description
                )

                firestore.collection("offers").add(offerData)
                    .addOnSuccessListener { documentReference ->
                        Constants.documentId = documentReference.id
                        firestore.collection("offers").document(Constants.documentId).addSnapshotListener { value, error ->
                            if(value!=null){
                                if(value.data?.get("type").toString()=="answer"){
                                    Toast.makeText(this@MainActivity,"Sender Connected to Receiver",Toast.LENGTH_LONG).show()
                                    val offerSdp = value.data?.get("sdp").toString()
                                    val offerType = SessionDescription.Type.ANSWER
                                    val remoteOffer = SessionDescription(offerType, offerSdp)
                                    peerConnection?.setRemoteDescription(CustomSdpObserver(), remoteOffer)

                                    firestore.collection("offers").document(Constants.documentId).collection("receiverIceCandidates").get().addOnCompleteListener {getReceiverIceCandidatesTask ->
                                        if(!getReceiverIceCandidatesTask.isSuccessful){
                                            Toast.makeText(this@MainActivity,"Failed to get receiver Ice Candidates",Toast.LENGTH_SHORT).show()
                                        }
                                        else{
                                            Log.d(TAG,"Added Receiver ICE candidates")
                                            getReceiverIceCandidatesTask.result.documents.forEach {
                                                peerConnection?.addIceCandidate(IceCandidate(it["sdpMid"].toString(), it["sdpMLineIndex"].toString().toInt(), it["sdp"].toString()))
                                            }
                                        }
                                    }

                                    firestore.collection("offers").document(Constants.documentId).update("type","connected")

                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error sending offer", e)
                    }
            }

            override fun onCreateFailure(s: String?) {
                super.onCreateFailure(s)
                // Offer creation failed
            }

        }, MediaConstraints())

    }

    private fun sendSenderIceCandidate(candidate: IceCandidate) {
        val iceCandidateData = hashMapOf(
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "sdp" to candidate.sdp
        )

        // Use the document reference for the specific offer you want to add the ICE candidate to
        val offerDocumentReference: DocumentReference = firestore.collection("offers").document(Constants.documentId)

        offerDocumentReference.collection("senderIceCandidates").add(iceCandidateData)
            .addOnSuccessListener { documentReference ->
                // ICE candidate sent successfully
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error sending ICE candidate", e)
            }
    }

    private fun initializeReceiverPeerConnection() {

        val initializationOptions: InitializationOptions = InitializationOptions.builder(applicationContext).createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)
        val options = PeerConnectionFactory.Options()

        val peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options).createPeerConnectionFactory()
        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)

        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : CustomPeerConnectionObserver() {

                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.d(TAG,"onIceCandidate")
                    val iceCandidateData = hashMapOf(
                        "sdpMid" to candidate.sdpMid,
                        "sdpMLineIndex" to candidate.sdpMLineIndex,
                        "sdp" to candidate.sdp
                    )

                    // Use the document reference for the specific offer you want to add the ICE candidate to
                    val offerDocumentReference: DocumentReference = firestore.collection("offers").document(Constants.documentId)

                    offerDocumentReference.collection("receiverIceCandidates").add(iceCandidateData)
                        .addOnSuccessListener { documentReference ->
                            // ICE candidate sent successfully
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error sending ICE candidate", e)
                        }
                }

                override fun onDataChannel(p0: DataChannel?) {
                    super.onDataChannel(p0)

                }

                override fun onIceConnectionChange(p0: IceConnectionState?) {
                    super.onIceConnectionChange(p0)
                    if (p0 != null) {
                        if (p0 == IceConnectionState.CONNECTED) {
                            Log.d(TAG,"Receiver Connected")
//                            val dcInit = DataChannel.Init()
//                            dataChannel = peerConnection!!.createDataChannel("receiverChannel2", dcInit)
//                            dataChannel?.registerObserver(object : CustomDataChannelObserver() {
//
//                                override fun onBufferedAmountChange(p0: Long) {
//                                    super.onBufferedAmountChange(p0)
//                                    Log.d(TAG,"onBuffered2... $p0")
//                                }
//
//                                override fun onStateChange() {
//                                    super.onStateChange()
//                                    Log.d(TAG,"onStateChange2... ${dataChannel?.state()}")
//                                }
//
//                                override fun onMessage(p0: DataChannel.Buffer?) {
//                                    super.onMessage(p0)
//                                    Log.d(TAG,"Received2...")
//                                }
//
//                            })
                        }
                        if (p0 == IceConnectionState.CLOSED) {
                            Log.d(TAG,"Receiver Closed")
                        }
                        if (p0 == IceConnectionState.FAILED) {
                            Log.d(TAG,"Receiver Failed")
                        }
                    }
                }
            })

        val dataChannelInit = DataChannel.Init()
        dataChannel = peerConnection?.createDataChannel("receiverChannel1", dataChannelInit)
        dataChannel?.registerObserver(object : CustomDataChannelObserver() {

            override fun onBufferedAmountChange(p0: Long) {
                super.onBufferedAmountChange(p0)
                Log.d(TAG,"onBuffered1... $p0")
            }

            override fun onStateChange() {
                super.onStateChange()
                Log.d(TAG,"onStateChange1... ${dataChannel?.state()}")
            }
            override fun onMessage(p0: DataChannel.Buffer?) {
                Log.d(TAG,"Received1...")
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO){
                        try {
                            receiveFile(p0!!)
                        }
                        catch(e: Exception){
                            Log.d(TAG,e.stackTraceToString())
                        }
                    }
                }
            }
        })

        firestore.collection("offers").document(Constants.documentId).get()
            .addOnCompleteListener { documentSnapshot ->
                if (!documentSnapshot.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to Retrieve Sender Id",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val offerData = documentSnapshot.result.data
                    if (offerData != null) {
                        val offerSdp = offerData["sdp"] as String
                        val offerType = SessionDescription.Type.OFFER
                        val remoteOffer = SessionDescription(offerType, offerSdp)

                        // Set the remote offer as the remote description
                        Toast.makeText(this@MainActivity,"Receiver Connected to Sender",Toast.LENGTH_LONG).show()
                        peerConnection?.setRemoteDescription(CustomSdpObserver(), remoteOffer)

                        firestore.collection("offers").document(Constants.documentId).collection("senderIceCandidates").get().addOnCompleteListener {getReceiverIceCandidatesTask ->
                            if(!getReceiverIceCandidatesTask.isSuccessful){
                                Toast.makeText(this@MainActivity,"Failed to get sender Ice Candidates",Toast.LENGTH_SHORT).show()
                            }
                            else{
                                getReceiverIceCandidatesTask.result.documents.forEach {
                                    peerConnection?.addIceCandidate(IceCandidate(it["sdpMid"].toString(), it["sdpMLineIndex"].toString().toInt(), it["sdp"].toString()))
                                }
                            }
                        }

                        // Create an answer and set it as the local description
                        peerConnection?.createAnswer(object : CustomSdpObserver() {

                            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                                peerConnection?.setLocalDescription(
                                    CustomSdpObserver(),
                                    sessionDescription
                                )
                                // Send the answer sessionDescription to the other device through Firestore
                                sendAnswer(sessionDescription)
                            }

                        }, MediaConstraints())
                    }
                }


            }
    }

    private fun sendAnswer(answer: SessionDescription) {
        val answerData = hashMapOf(
            "type" to answer.type.canonicalForm(),
            "sdp" to answer.description
        )

        val offerDocumentReference: DocumentReference = firestore.collection("offers").document(Constants.documentId)

        // Explicitly cast the HashMap to Map<String, Any>
        val answerDataAsMap: Map<String, Any> = answerData

        offerDocumentReference.update(answerDataAsMap)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error sending answer", e)
            }
    }

    private suspend fun sendFile(dataChannel: DataChannel, fileUri: Uri) {
        withContext(Dispatchers.IO) {
            val contentResolver = applicationContext.contentResolver
            val inputStream = contentResolver.openInputStream(fileUri)
            val bufferSize = 1024 * 16 // 16 KB buffer size, adjust as needed

            inputStream?.use { input ->
                val buffer = ByteArray(bufferSize)
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val chunk = buffer.copyOfRange(0, bytesRead)
                    val byteBuffer = ByteBuffer.wrap(chunk)

                    dataChannel.send(DataChannel.Buffer(byteBuffer, false))
                }
            }
        }
    }

    private suspend fun receiveFile(buffer: DataChannel.Buffer) {
        withContext(Dispatchers.IO) {

            try{
                val byteArray = ByteArray(buffer.data.capacity())
                buffer.data.get(byteArray)

                fileOutputStream.write(byteArray)
//                fileOutputStream.flush()
            }
            catch(e: Exception){
                Log.d(TAG,e.stackTraceToString())
            }


        }
    }


//    private fun initializePeerConnection() {
//        // Create a factory for PeerConnection
//        val factory = PeerConnectionFactory.builder()
//            .setOptions(PeerConnectionFactory.Options())
//            .createPeerConnectionFactory()
//
//        // Create PeerConnection
//        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
//        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
//        peerConnection = factory.createPeerConnection(rtcConfig, object : CustomPeerConnectionObserver() {
//            override fun onIceCandidate(candidate: IceCandidate) {
//                Log.d(TAG,"onIceCandidate")
//                // Send ICE candidate to the other device through Firestore
//                if(Constants.documentId=="") {
//                    Timer().schedule(object: TimerTask(){
//                        override fun run() {
//                            sendIceCandidate(candidate)
//                        }
//                    },2000)
//                }
//                else{
//                    sendIceCandidate(candidate)
//                }
//            }
//
//            override fun onDataChannel(p0: DataChannel?) {
//                Log.d(TAG,"Data Channel Received")
//                CoroutineScope(Dispatchers.IO).launch {
//                    dataChannel = p0
//                    sendFile()
//                }
//            }
//
//            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
//                if(newState == PeerConnection.PeerConnectionState.CONNECTED){
//                    Log.d(TAG,"Connection Established")
//                }
//                else{
//                    Log.d(TAG,"Connection Change: ${newState?.name}")
//                }
//            }
//
//        })
//
//        // Create a data channel for file transfer
//        val dataChannelInit = DataChannel.Init()
//        dataChannel = peerConnection?.createDataChannel("fileChannel", dataChannelInit)
//        dataChannel?.registerObserver(object : DataChannel.Observer {
//            override fun onBufferedAmountChange(previousAmount: Long) {
//                // Handle buffered amount change
//            }
//
//            override fun onStateChange() {
//                // Handle data channel state change
//            }
//
//            override fun onMessage(buffer: DataChannel.Buffer) {
//                // Handle received file data
//            }
//        })
//
//        // Create an offer and set it as the local description
//        peerConnection?.createOffer(object : CustomSdpObserver() {
//            override fun onCreateSuccess(sessionDescription: SessionDescription) {
//                peerConnection?.setLocalDescription(CustomSdpObserver(), sessionDescription)
//                // Send the offer sessionDescription to the other device through Firestore
//                sendOffer(sessionDescription)
//            }
//        }, MediaConstraints())
//    }
//
//    private suspend fun sendFile(){
//        val fileUri = Constants.selectedFile
//
//        if (fileUri == null) {
//            Log.e(TAG, "No file selected.")
//            return
//        }
//
//        val contentResolver = applicationContext.contentResolver
//        val inputStream = contentResolver.openInputStream(fileUri)
//
//        inputStream?.use { input ->
//            val buffer = ByteArray(1024)
//            var bytesRead: Int
//
//            while (input.read(buffer).also { bytesRead = it } != -1) {
//                // Send the file data using the DataChannel
//                dataChannel?.send(DataChannel.Buffer(ByteBuffer.wrap(buffer, 0, bytesRead), false))
//            }
//
//            // Notify the receiver that the file transfer is complete
//            dataChannel?.send(DataChannel.Buffer(ByteBuffer.wrap("EOF".toByteArray()), true))
//
//            Log.d(TAG, "File sent successfully.")
//        } ?: run {
//            Log.e(TAG, "Error opening file.")
//        }
//    }
//
//    private fun sendOffer(offer: SessionDescription) {
//        val offerData = hashMapOf(
//            "type" to offer.type.canonicalForm(),
//            "sdp" to offer.description
//        )
//
//        firestore.collection("offers").add(offerData)
//            .addOnSuccessListener { documentReference ->
//                Constants.documentId = documentReference.id
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Error sending offer", e)
//            }
//    }
//
//    private fun sendIceCandidate(candidate: IceCandidate) {
//        val iceCandidateData = hashMapOf(
//            "sdpMid" to candidate.sdpMid,
//            "sdpMLineIndex" to candidate.sdpMLineIndex,
//            "sdp" to candidate.sdp
//        )
//
//        // Use the document reference for the specific offer you want to add the ICE candidate to
//        val offerDocumentReference: DocumentReference = firestore.collection("offers").document(Constants.documentId)
//
//        offerDocumentReference.collection("iceCandidates").add(iceCandidateData)
//            .addOnSuccessListener { documentReference ->
//                // ICE candidate sent successfully
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Error sending ICE candidate", e)
//            }
//    }
//
//    private fun receiveFile(firestoreDocumentId: String) {
//        // Create a factory for PeerConnection
//        val factory = PeerConnectionFactory.builder()
//            .setOptions(PeerConnectionFactory.Options())
//            .createPeerConnectionFactory()
//
//        // Create PeerConnection
//        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
//        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
//        peerConnection = factory.createPeerConnection(rtcConfig, object : CustomPeerConnectionObserver() {
//            override fun onIceCandidate(candidate: IceCandidate) {
//                // Send ICE candidate to the other device through Firestore
//                sendIceCandidate(candidate, firestoreDocumentId)
//            }
//
//            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
//                Log.d(TAG,"Ice Connection Change: ${p0?.name}")
//                if(p0 == PeerConnection.IceConnectionState.CONNECTED){
//                    // Create a data channel for file transfer
//                    val dataChannelInit = DataChannel.Init()
//                    dataChannel = peerConnection?.createDataChannel("fileChannel", dataChannelInit)
//                    dataChannel?.registerObserver(object : DataChannel.Observer {
//                        override fun onBufferedAmountChange(previousAmount: Long) {
//                            // Handle buffered amount change
//                        }
//
//                        override fun onStateChange() {
//                            // Handle data channel state change
//                        }
//
//                        override fun onMessage(buffer: DataChannel.Buffer) {
//                            // Handle received file data
//                            receiveFileData(buffer)
//                        }
//                    })
//                }
//            }
//
//            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
//                Log.d(TAG,"Peer Connection Change: ${newState?.name}")
//                if(newState == PeerConnection.PeerConnectionState.CONNECTED){
//                    // Create a data channel for file transfer
//                    val dataChannelInit = DataChannel.Init()
//                    dataChannel = peerConnection?.createDataChannel("fileChannel", dataChannelInit)
//                    dataChannel?.registerObserver(object : DataChannel.Observer {
//                        override fun onBufferedAmountChange(previousAmount: Long) {
//                            // Handle buffered amount change
//                        }
//
//                        override fun onStateChange() {
//                            // Handle data channel state change
//                        }
//
//                        override fun onMessage(buffer: DataChannel.Buffer) {
//                            // Handle received file data
//                            receiveFileData(buffer)
//                        }
//                    })
//                }
//            }
//
//        })
//
//        // Initialize Firestore
//        firestore = Firebase.firestore
//
//        // Retrieve the offer and other signaling data from Firestore
//        val offerDocumentReference: DocumentReference = firestore.collection("offers").document(firestoreDocumentId)
//
//        offerDocumentReference.get()
//            .addOnSuccessListener { documentSnapshot ->
//                val offerData = documentSnapshot.data
//                if (offerData != null) {
//                    val offerSdp = offerData["sdp"] as String
//                    val offerType = SessionDescription.Type.OFFER
//                    val remoteOffer = SessionDescription(offerType, offerSdp)
//
//                    // Set the remote offer as the remote description
//                    peerConnection?.setRemoteDescription(CustomSdpObserver(), remoteOffer)
//
//                    // Create an answer and set it as the local description
//                    peerConnection?.createAnswer(object : CustomSdpObserver() {
//                        override fun onCreateSuccess(sessionDescription: SessionDescription) {
//                            peerConnection?.setLocalDescription(CustomSdpObserver(), sessionDescription)
//                            // Send the answer sessionDescription to the other device through Firestore
//                            sendAnswer(sessionDescription, firestoreDocumentId)
//                        }
//                    }, MediaConstraints())
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Error receiving offer", e)
//            }
//    }
//
//    private fun sendAnswer(answer: SessionDescription, firestoreDocumentId: String) {
//        val answerData = hashMapOf(
//            "type" to answer.type.canonicalForm(),
//            "sdp" to answer.description
//        )
//
//        val offerDocumentReference: DocumentReference = firestore.collection("offers").document(firestoreDocumentId)
//
//        // Explicitly cast the HashMap to Map<String, Any>
//        val answerDataAsMap: Map<String, Any> = answerData
//
//        offerDocumentReference.update(answerDataAsMap)
//            .addOnSuccessListener {
//                // Answer sent successfully
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Error sending answer", e)
//            }
//    }
//
//    private fun sendIceCandidate(candidate: IceCandidate, firestoreDocumentId: String) {
//        val iceCandidateData = hashMapOf(
//            "sdpMid" to candidate.sdpMid,
//            "sdpMLineIndex" to candidate.sdpMLineIndex,
//            "sdp" to candidate.sdp
//        )
//
//        val offerDocumentReference: DocumentReference = firestore.collection("offers").document(firestoreDocumentId)
//
//        offerDocumentReference.collection("iceCandidates").add(iceCandidateData)
//            .addOnSuccessListener { documentReference ->
//                // ICE candidate sent successfully
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Error sending ICE candidate", e)
//            }
//    }
//
//    private fun receiveFileData(buffer: DataChannel.Buffer) {
//        if (buffer.binary) {
//            val data = ByteArray(buffer.data.remaining())
//            buffer.data.get(data)
//
//            val fileName = "received_file.txt" // Change this to your desired file name
//            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            val outputFile = File(downloadsDir, fileName)
//
//            try {
//                val fileOutputStream = FileOutputStream(outputFile, true)
//                fileOutputStream.write(data)
//                fileOutputStream.close()
//
//                // File received and saved to the downloads folder
//                Log.d(TAG, "File received and saved: ${outputFile.absolutePath}")
//            } catch (e: Exception) {
//                Log.e(TAG, "Error saving received file", e)
//            }
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        peerConnection?.close()
        dataChannel?.close()
    }

    companion object {
        private const val TAG = "MainActivityTag"
    }
}


//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == Activity.RESULT_OK) {
//                    val data: Intent? = result.data
//                    if (data != null) {
//                        Constants.selectedFile = data.data
//                        Log.d("Selected File: ", Constants.selectedFile?.path.toString())
//
//                        // Send Bytes of File
//                        CoroutineScope(Dispatchers.Main).launch {
//                            sendFile(Constants.selectedFile)
//                        }
//
//
//                    }
//                }
//            }
//
//        setContent {
//            FileLeapTheme{
//                MainScreen() {
//                    val intent = Intent(Intent.ACTION_GET_CONTENT)
//                    intent.type = "*/*"
//                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                    resultLauncher.launch(intent)
//                }
//            }
//        }
//    }
//
//    suspend fun sendFile(fileUri: Uri?) {
////        withContext(Dispatchers.IO){
////            if(fileUri==null){
////                return@withContext
////            }
////            else{
////                val inputStream = contentResolver.openInputStream(fileUri) ?: return@withContext
////                inputStream.use {
////                    val buffer = ByteArray(1024)
////                    val bytesRead = inputStream.read(buffer)
////                    while (bytesRead != -1) {
////                        /*TODO send bytes to receiver*/
////                    }
////                }
////            }
////        }
//    }
//
//}