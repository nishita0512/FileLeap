package com.example.fileleap.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.example.fileleap.ui.Constants
import com.example.fileleap.ui.WebRTCViewModel
import com.example.fileleap.ui.theme.FileLeapTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.PeerConnectionFactory

class MainActivity : ComponentActivity() {

    private lateinit var webRTCViewModel: WebRTCViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webRTCViewModel = WebRTCViewModel(application)

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

                    //Show Password Screen
                    Constants.isSender = true
                    Constants.screen.value = 1
                }
            }
        }

        lifecycleScope.launch {
            while(true){
                if(Constants.toastsToShowList.isNotEmpty()){
                    showToast(Constants.toastsToShowList[0])
                    Constants.toastsToShowList.removeFirst()
                    delay(3500)
                }
                delay(500)
            }
        }

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
                        Constants.isSender = false
                        Constants.screen.value = 1
                    },
                    Constants.screen.value,
                    Constants.bytesReceived.value,
                    Constants.bytesSent.value,
                    webRTCViewModel
                )
            }
        }
    }

    private fun showToast(message: String){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        webRTCViewModel.closeConnection()
    }

}