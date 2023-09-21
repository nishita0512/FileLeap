package com.example.fileleap.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fileleap.ui.Constants
import com.example.fileleap.ui.theme.FileLeapTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Arrays

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {
                        Constants.fileUri = data.data
                        Log.d("Selected File: ", Constants.fileUri?.path.toString())

                        // Send Bytes of File
                        CoroutineScope(Dispatchers.Main).launch {
                            sendFile(Constants.fileUri)
                        }

                    }
                }
            }

        setContent {
            FileLeapTheme{
                MainScreen() {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "*/*"
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    resultLauncher.launch(intent)
                }
            }
        }
    }

    suspend fun sendFile(fileUri: Uri?) {
        withContext(Dispatchers.IO){
            if(fileUri==null){
                return@withContext
            }
            else{
                val inputStream = contentResolver.openInputStream(fileUri) ?: return@withContext
                inputStream.use {
                    val buffer = ByteArray(1024)
                    val bytesRead = inputStream.read(buffer)
                    while (bytesRead != -1) {
                        /*TODO send bytes to receiver*/
                    }
                }
            }
        }
    }

}