package com.example.fileleap.ui.webrtc

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import com.example.fileleap.ui.Constants
import com.example.fileleap.ui.Constants.TAG
import com.example.fileleap.ui.Constants.fileSize
import com.example.fileleap.ui.Constants.selectedFile
import com.example.fileleap.ui.utils.AES
import com.example.fileleap.ui.utils.BCrypt
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

class WebRTCConnection constructor(val applicationContext: Context) {

    private var peerConnection: PeerConnection? = null
    private var acknowledgementDataChannel: DataChannel? = null
    private var dataChannel: DataChannel? = null
    private lateinit var fileOutputStream: FileOutputStream
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var acknowledgementReceived = true

//    companion object {
//        private var rtConnection: WebRTCConnection? = null
//        private lateinit var context: Context
//
//        @Synchronized
//        fun getInstance(context: Context): WebRTCConnection {
//            if (rtConnection == null) {
//                rtConnection = WebRTCConnection(context)
//            }
//            return rtConnection!!
//        }
//    }

    fun initializeSenderPeerConnection(){
        Constants.showProgressBar.value = true
        val initializationOptions: PeerConnectionFactory.InitializationOptions = PeerConnectionFactory.InitializationOptions.builder(applicationContext).createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)
        val options = PeerConnectionFactory.Options()

        val peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options).createPeerConnectionFactory()
        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)

        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : CustomPeerConnectionObserver() {

                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.d(Constants.TAG,"onIceCandidate")
                    // Send ICE candidate to the other device through Firestore
                    sendSenderIceCandidate(candidate)
                }

                override fun onDataChannel(p0: DataChannel?) {
                    super.onDataChannel(p0)
                    Log.d(Constants.TAG,"Sending File... ${p0?.label()}")

                    if(p0 == null){
                        Log.d(Constants.TAG,"NULL")
                    }

                    val dataChannelInit = DataChannel.Init()
//                    acknowledgementDataChannel = peerConnection?.createDataChannel("acknowledgementChannel", dataChannelInit)
//                    acknowledgementDataChannel?.registerObserver(object : DataChannel.Observer {
//                        override fun onBufferedAmountChange(previousAmount: Long) {
//                            // Handle buffered amount change
//                        }
//
//                        override fun onStateChange() {
//                            // Handle data channel state change
//                        }
//
//                        override fun onMessage(buffer: DataChannel.Buffer) {
//                            Log.d(TAG,"Acknowledgement Received")
//                            acknowledgementReceived = true
//                        }
//                    })

                    dataChannel = p0
                    Constants.showProgressBar.value = false
                    Constants.screen.value = 3
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
                                sendFile(dataChannel!!, Constants.selectedFile!!)
                            }
                        }
                    },2000)

                }

                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                    super.onIceConnectionChange(p0)
                    if (p0 != null) {
                        if (p0 == PeerConnection.IceConnectionState.CONNECTED) {
                            Log.d(Constants.TAG,"Sender Connected")
                        }
                        if (p0 == PeerConnection.IceConnectionState.CLOSED) {
                            Log.d(Constants.TAG,"Sender Closed")
                        }
                        if (p0 == PeerConnection.IceConnectionState.FAILED) {
                            Log.d(Constants.TAG,"Sender Failed: ${p0.name}")
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
                Log.d(TAG,"onMessage")
            }
        })

        peerConnection!!.createOffer(object : CustomSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                peerConnection?.setLocalDescription(CustomSdpObserver(), sessionDescription)

                Constants.fileName = getFileName(Constants.selectedFile!!)
                Constants.fileSize.value = getFileSize(Constants.selectedFile!!)

                // sessionDescription.description is string which needs to the shared across network
                val offerData = hashMapOf(
                    "fileName" to Constants.fileName,
                    "fileSize" to Constants.fileSize.value,
                    "hashedPassword" to Constants.hashedPassword.value,
                    "type" to sessionDescription.type.canonicalForm(),
                    "sdp" to sessionDescription.description
                )

                Log.d(TAG,"sending offer to firestore...")

                firestore.collection("offers").get().addOnCompleteListener{ getAllOffersTask ->

                    val noOfDocs = getAllOffersTask.result.documents.size
                    //noOfDocs = 2
                    //documentId = 0002
                    Constants.documentId = when (noOfDocs) {
                        in 1000..9999 -> {
                            noOfDocs.toString()
                        }
                        in 100 .. 999 -> {
                            "0$noOfDocs"
                        }
                        in 10 .. 99 -> {
                            "00$noOfDocs"
                        }
                        else -> {
                            "000$noOfDocs"
                        }
                    }
                    Constants.screen.value = 2
                    Constants.isSender = true
                    Log.d(TAG,"Doc ID: ${Constants.documentId}")
                    firestore.collection("offers").document(Constants.documentId).set(offerData)
                        .addOnCompleteListener { taskReference ->
                            Log.d(TAG,"Inside onComplete")
                            if(taskReference.isSuccessful){
                                firestore.collection("offers").document(Constants.documentId).addSnapshotListener { value, error ->
                                    if(value!=null){
                                        if(value.data?.get("type").toString()=="answer"){
                                            Toast.makeText(applicationContext,"Sender Connected to Receiver",
                                                Toast.LENGTH_LONG).show()
                                            val offerSdp = value.data?.get("sdp").toString()
                                            val offerType = SessionDescription.Type.ANSWER
                                            val remoteOffer = SessionDescription(offerType, offerSdp)
                                            peerConnection?.setRemoteDescription(CustomSdpObserver(), remoteOffer)

                                            firestore.collection("offers").document(Constants.documentId).collection("receiverIceCandidates").get().addOnCompleteListener { getReceiverIceCandidatesTask ->
                                                if(!getReceiverIceCandidatesTask.isSuccessful){
                                                    Toast.makeText(applicationContext,"Failed to get receiver Ice Candidates",
                                                        Toast.LENGTH_SHORT).show()
                                                }
                                                else{
                                                    Log.d(Constants.TAG,"Added Receiver ICE candidates")
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
                            else{
                                Log.d(TAG,"Failed to make offer: ${taskReference.exception}")
                            }
                        }
                        .addOnCanceledListener {
                            Log.d(TAG,"Making Offer Cancelled by Firestore")
                        }
                        .addOnFailureListener {
                            Log.d(TAG,"Failed to make offer1: ${it.message}")
                        }

                }


            }

            override fun onCreateFailure(s: String?) {
                super.onCreateFailure(s)
                // Offer creation failed
            }

        }, MediaConstraints())

    }

    fun sendSenderIceCandidate(candidate: IceCandidate) {
        val iceCandidateData = hashMapOf(
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "sdp" to candidate.sdp
        )

        if(Constants.documentId=="") {
            Timer().schedule(object: TimerTask(){
                override fun run() {
                    sendSenderIceCandidate(candidate)
                }
            },5000)
        }
        else{
            val offerDocumentReference: DocumentReference = firestore.collection("offers").document(Constants.documentId)

            offerDocumentReference.collection("senderIceCandidates").add(iceCandidateData)
                .addOnSuccessListener { documentReference ->
                    // ICE candidate sent successfully
                }
                .addOnFailureListener { e ->
                    Log.w(Constants.TAG, "Error sending ICE candidate", e)
                }
        }

    }

    fun initializeReceiverPeerConnection() {
        Constants.showProgressBar.value = true
        val initializationOptions: PeerConnectionFactory.InitializationOptions = PeerConnectionFactory.InitializationOptions.builder(applicationContext).createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)
        val options = PeerConnectionFactory.Options()

        val peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options).createPeerConnectionFactory()
        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)

        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : CustomPeerConnectionObserver() {

                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.d(Constants.TAG,"onIceCandidate")
                    val iceCandidateData = hashMapOf(
                        "sdpMid" to candidate.sdpMid,
                        "sdpMLineIndex" to candidate.sdpMLineIndex,
                        "sdp" to candidate.sdp
                    )

                    // Use the document reference for the specific offer you want to add the ICE candidate to
                    val offerDocumentReference: DocumentReference = firestore.collection("offers").document(
                        Constants.documentId)

                    offerDocumentReference.collection("receiverIceCandidates").add(iceCandidateData)
                        .addOnSuccessListener { documentReference ->
                            // ICE candidate sent successfully
                        }
                        .addOnFailureListener { e ->
                            Log.w(Constants.TAG, "Error sending ICE candidate", e)
                        }
                }

                override fun onDataChannel(p0: DataChannel?) {
//                    acknowledgementDataChannel = p0
//                    Log.d(TAG,"Acknowledgement Data Channel: ${p0?.label()}")
                    super.onDataChannel(p0)
                }

                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                    super.onIceConnectionChange(p0)
                    if (p0 != null) {
                        if (p0 == PeerConnection.IceConnectionState.CONNECTED) {
                            Log.d(Constants.TAG,"Receiver Connected")
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
                        if (p0 == PeerConnection.IceConnectionState.CLOSED) {
                            Log.d(Constants.TAG,"Receiver Closed")
                        }
                        if (p0 == PeerConnection.IceConnectionState.FAILED) {
                            Log.d(Constants.TAG,"Receiver Failed")
                        }
                    }
                }
            })

        val dataChannelInit = DataChannel.Init()
        dataChannel = peerConnection?.createDataChannel("receiverChannel", dataChannelInit)
        dataChannel?.registerObserver(object : CustomDataChannelObserver() {

            override fun onBufferedAmountChange(p0: Long) {
                super.onBufferedAmountChange(p0)
                Log.d(Constants.TAG,"onBuffered1... $p0")
            }

            override fun onStateChange() {
                super.onStateChange()
                Log.d(Constants.TAG,"onStateChange1... ${dataChannel?.state()}")
            }
            override fun onMessage(p0: DataChannel.Buffer?) {
                Log.d(Constants.TAG,"Received1...")
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO){
                        try {
                            receiveFile(p0!!)
                        }
                        catch(e: Exception){
                            Log.d(Constants.TAG,e.stackTraceToString())
                            Toast.makeText(applicationContext,"Failed to Receive File!!",Toast.LENGTH_SHORT).show()
                            close()
                        }
                    }
                }
            }
        })

        firestore.collection("offers").document(Constants.documentId).get()
            .addOnCompleteListener { documentSnapshot ->
                if (!documentSnapshot.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Failed to Retrieve Sender Id",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val offerData = documentSnapshot.result.data
                    if (offerData != null) {

                        Constants.fileSize.value = offerData["fileSize"].toString().toLong()
                        Constants.fileName = offerData["fileName"].toString()
                        Constants.hashedPassword.value = offerData["hashedPassword"].toString()
                        if(!BCrypt.checkpw(Constants.password.value,Constants.hashedPassword.value)){
                            Constants.apply{
                                selectedFile = null
                                fileName = ""
                                fileSize.value = 0L
                                bytesReceived.value = 0L
                                bytesSent.value = 0L
                                documentId = ""
                                isSender = false
                                screen.value = 0
                                toastsToShowList.add("Wrong Password. Transfer Cancelled")
                            }
                        }
                        else{
                            Constants.showProgressBar.value = false
                            Constants.screen.value = 3
                            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val file = File(downloadsDir,Constants.fileName)
                            fileOutputStream = FileOutputStream(file, true)

                            val offerSdp = offerData["sdp"] as String
                            val offerType = SessionDescription.Type.OFFER
                            val remoteOffer = SessionDescription(offerType, offerSdp)

                            // Set the remote offer as the remote description
                            Toast.makeText(applicationContext,"Receiver Connected to Sender", Toast.LENGTH_LONG).show()
                            peerConnection?.setRemoteDescription(CustomSdpObserver(), remoteOffer)

                            firestore.collection("offers").document(Constants.documentId).collection("senderIceCandidates").get().addOnCompleteListener { getReceiverIceCandidatesTask ->
                                if(!getReceiverIceCandidatesTask.isSuccessful){
                                    Toast.makeText(applicationContext,"Failed to get sender Ice Candidates",
                                        Toast.LENGTH_SHORT).show()
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
    }

    fun sendAnswer(answer: SessionDescription) {
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
                Log.w(Constants.TAG, "Error sending answer", e)
            }
    }

    suspend fun sendFile(dataChannel: DataChannel, fileUri: Uri) {
        withContext(Dispatchers.IO) {
            val contentResolver = applicationContext.contentResolver
            val inputStream = contentResolver.openInputStream(fileUri)
            val bufferSize = 1024 * 32
            val passwordByteArray = Constants.password.value.toByteArray()

            inputStream?.use { input ->
                val buffer = ByteArray(bufferSize)
                var bytesRead: Int

                while(input.read(buffer).also { bytesRead = it } != -1) {
                    val chunk = buffer.copyOfRange(0, bytesRead)
                    val encryptedChunk = AES.encrypt(chunk,passwordByteArray)
                    val byteBuffer = ByteBuffer.wrap(encryptedChunk)

                    dataChannel.send(DataChannel.Buffer(byteBuffer, false))
                    Constants.bytesSent.value += bytesRead
                    Log.d(TAG, "Sent Bytes: ${Constants.bytesSent.value}")
                    Thread.sleep(500)
                }

            }
            Timer().schedule(object: TimerTask(){
                override fun run() {
                    Constants.apply{
                        selectedFile = null
                        fileName = ""
                        fileSize.value = 0L
                        bytesReceived.value = 0L
                        bytesSent.value = 0L
                        documentId = ""
                        isSender = false
                        screen.value = 0
                        toastsToShowList.add("File Sent Successfully")
                    }
//                            Toast.makeText(applicationContext,"Transfer Completed",Toast.LENGTH_LONG).show()
                }
            },2000)
        }
    }

//    suspend fun sendFile(dataChannel: DataChannel, fileUri: Uri) {
//        withContext(Dispatchers.IO) {
//            val contentResolver = applicationContext.contentResolver
//            val inputStream = contentResolver.openInputStream(fileUri)
//            val bufferSize = 1024 * 32
//            val buffer = ByteArray(bufferSize)
//            var bytesRead: Int
//
//            while(true){
//                if(acknowledgementReceived){
//                    bytesRead = inputStream?.read(buffer) ?: -1
//                    if(bytesRead != -1) {
//                        val chunk = buffer.copyOfRange(0, bytesRead)
//                        val byteBuffer = ByteBuffer.wrap(chunk)
//
//                        dataChannel.send(DataChannel.Buffer(byteBuffer, false))
//                        Constants.bytesSent += bytesRead
//                        Log.d(TAG, "Sent Bytes: ${Constants.bytesSent}")
//                        acknowledgementReceived = false
//                    }
//                    else{
//                        break
//                    }
//                }
//                else{
//                    delay(100)
//                }
//            }
//            inputStream?.close()
//        }
//    }

    suspend fun receiveFile(buffer: DataChannel.Buffer) {

        withContext(Dispatchers.IO) {

            try{
                val encryptedByteArray = ByteArray(buffer.data.capacity())
                buffer.data.get(encryptedByteArray)
                val byteArray = AES.decrypt(encryptedByteArray,Constants.password.value.toByteArray())
                Constants.bytesReceived.value += byteArray.size
                Log.d(TAG, "Received Bytes: ${Constants.bytesReceived.value}")
//                Toast.makeText(applicationContext,"Received: ${Constants.bytesReceived}",Toast.LENGTH_SHORT).show()

                fileOutputStream.write(byteArray)
                if(Constants.bytesReceived.value >= fileSize.value) {
                    fileOutputStream.flush()
                    Timer().schedule(object: TimerTask(){
                        override fun run() {
                            Constants.apply{
                                selectedFile = null
                                fileName = ""
                                fileSize.value = 0L
                                bytesReceived.value = 0L
                                bytesSent.value = 0L
                                documentId = ""
                                isSender = false
                                screen.value = 0
                                toastsToShowList.add("File Saved Successfully in Downloads Folder")
                            }
//                            Toast.makeText(applicationContext,"Transfer Completed",Toast.LENGTH_LONG).show()
                        }
                    },2000)
                } else { }
            }
            catch(e: Exception){
                Log.d(TAG,e.stackTraceToString())
            }


        }
    }

//    suspend fun receiveFile(buffer: DataChannel.Buffer) {
//
//        withContext(Dispatchers.IO) {
//
//            try{
//                val byteArray = ByteArray(buffer.data.capacity())
//                buffer.data.get(byteArray)
//                Constants.bytesReceived += byteArray.size
//                Log.d(TAG, "Received Bytes: ${Constants.bytesReceived}")
//                fileOutputStream.write(byteArray)
//                acknowledgementDataChannel?.send(DataChannel.Buffer(ByteBuffer.wrap("ACK".toByteArray()),false))
//                if(Constants.bytesReceived >= fileSize) {
//                    fileOutputStream.flush()
//                } else { }
//            }
//            catch(e: Exception){
//                Log.d(TAG,e.stackTraceToString())
//            }
//
//        }
//    }

    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = applicationContext.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    result = cursor.getString(
                        if(columnIndex<0){0}
                        else{columnIndex}
                    )
                }
            }
            catch(e: Exception){
                e.printStackTrace()
            }
            finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    fun getFileSize(uri: Uri): Long{
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = applicationContext.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    result = cursor.getString(
                        if(columnIndex<0){0}
                        else{columnIndex}
                    )
                }
            }
            catch(e: Exception){
                e.printStackTrace()
            }
            finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result.toLong()
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