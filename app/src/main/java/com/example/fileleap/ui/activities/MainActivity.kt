package com.example.fileleap.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fileleap.ui.Constants
import com.example.fileleap.ui.theme.FileLeapTheme
import com.example.fileleap.ui.webrtc.MyPeerObserver
import com.example.fileleap.ui.webrtc.MySdpObserver
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.util.Timer
import java.util.TimerTask

class MainActivity : ComponentActivity() {
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    initializePeerConnection()
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
                        receiveFile(it)
                    }
                )
            }
        }
    }

    private fun initializePeerConnection() {
        // Create a factory for PeerConnection
        val factory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()

        // Create PeerConnection
        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = factory.createPeerConnection(rtcConfig, object : MyPeerObserver() {
            override fun onIceCandidate(candidate: IceCandidate) {
                // Send ICE candidate to the other device through Firestore
                if(Constants.senderDocumentId=="") {
                    Timer().schedule(object: TimerTask(){
                        override fun run() {
                            sendIceCandidate(candidate)
                        }
                    },2000)
                }
                else{
                    sendIceCandidate(candidate)
                }
            }
        })

        // Create a data channel for file transfer
        val dataChannelInit = DataChannel.Init()
        dataChannel = peerConnection?.createDataChannel("fileChannel", dataChannelInit)
        dataChannel?.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) {
                // Handle buffered amount change
            }

            override fun onStateChange() {
                // Handle data channel state change
            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                // Handle received file data
            }
        })

        // Create an offer and set it as the local description
        peerConnection?.createOffer(object : MySdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(MySdpObserver(), sessionDescription)
                // Send the offer sessionDescription to the other device through Firestore
                sendOffer(sessionDescription)
            }
        }, MediaConstraints())
    }

    private fun sendOffer(offer: SessionDescription) {
        val offerData = hashMapOf(
            "type" to offer.type.canonicalForm(),
            "sdp" to offer.description
        )

        firestore.collection("offers").add(offerData)
            .addOnSuccessListener { documentReference ->
                Constants.senderDocumentId = documentReference.id
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error sending offer", e)
            }
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        val iceCandidateData = hashMapOf(
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "sdp" to candidate.sdp
        )

        // Use the document reference for the specific offer you want to add the ICE candidate to
        val offerDocumentReference: DocumentReference = firestore.collection("offers").document(Constants.senderDocumentId)

        offerDocumentReference.collection("iceCandidates").add(iceCandidateData)
            .addOnSuccessListener { documentReference ->
                // ICE candidate sent successfully
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error sending ICE candidate", e)
            }
    }

    private fun receiveFile(firestoreDocumentId: String) {
        // Create a factory for PeerConnection
        val factory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()

        // Create PeerConnection
        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = factory.createPeerConnection(rtcConfig, object : MyPeerObserver() {
            override fun onIceCandidate(candidate: IceCandidate) {
                // Send ICE candidate to the other device through Firestore
                sendIceCandidate(candidate, firestoreDocumentId)
            }
        })

        // Create a data channel for file transfer
        val dataChannelInit = DataChannel.Init()
        dataChannel = peerConnection?.createDataChannel("fileChannel", dataChannelInit)
        dataChannel?.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) {
                // Handle buffered amount change
            }

            override fun onStateChange() {
                // Handle data channel state change
            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                // Handle received file data
                receiveFileData(buffer)
            }
        })

        // Initialize Firestore
        firestore = Firebase.firestore

        // Retrieve the offer and other signaling data from Firestore
        val offerDocumentReference: DocumentReference = firestore.collection("offers").document(firestoreDocumentId)

        offerDocumentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                val offerData = documentSnapshot.data
                if (offerData != null) {
                    val offerSdp = offerData["sdp"] as String
                    val offerType = SessionDescription.Type.fromCanonicalForm(offerData["type"] as String)
                    val remoteOffer = SessionDescription(offerType, offerSdp)

                    // Set the remote offer as the remote description
                    peerConnection?.setRemoteDescription(MySdpObserver(), remoteOffer)

                    // Create an answer and set it as the local description
                    peerConnection?.createAnswer(object : MySdpObserver() {
                        override fun onCreateSuccess(sessionDescription: SessionDescription) {
                            peerConnection?.setLocalDescription(MySdpObserver(), sessionDescription)
                            // Send the answer sessionDescription to the other device through Firestore
                            sendAnswer(sessionDescription, firestoreDocumentId)
                        }
                    }, MediaConstraints())
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error receiving offer", e)
            }
    }

    private fun sendAnswer(answer: SessionDescription, firestoreDocumentId: String) {
        val answerData = hashMapOf(
            "type" to answer.type.canonicalForm(),
            "sdp" to answer.description
        )

        val offerDocumentReference: DocumentReference = firestore.collection("offers").document(firestoreDocumentId)

        // Explicitly cast the HashMap to Map<String, Any>
        val answerDataAsMap: Map<String, Any> = answerData

        offerDocumentReference.update(answerDataAsMap)
            .addOnSuccessListener {
                // Answer sent successfully
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error sending answer", e)
            }
    }

    private fun sendIceCandidate(candidate: IceCandidate, firestoreDocumentId: String) {
        val iceCandidateData = hashMapOf(
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "sdp" to candidate.sdp
        )

        val offerDocumentReference: DocumentReference = firestore.collection("offers").document(firestoreDocumentId)

        offerDocumentReference.collection("iceCandidates").add(iceCandidateData)
            .addOnSuccessListener { documentReference ->
                // ICE candidate sent successfully
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error sending ICE candidate", e)
            }
    }

    private fun receiveFileData(buffer: DataChannel.Buffer) {
        if (buffer.binary) {
            val data = ByteArray(buffer.data.remaining())
            buffer.data.get(data)

            val fileName = "received_file.txt" // Change this to your desired file name
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outputFile = File(downloadsDir, fileName)

            try {
                val fileOutputStream = FileOutputStream(outputFile, true)
                fileOutputStream.write(data)
                fileOutputStream.close()

                // File received and saved to the downloads folder
                Log.d(TAG, "File received and saved: ${outputFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving received file", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release resources when the activity is destroyed
        peerConnection?.close()
        dataChannel?.close()
    }

    companion object {
        private const val TAG = "MainActivity"
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