package com.example.fileleap.ui.activities

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fileleap.ui.Constants
import com.example.fileleap.ui.theme.Primary
import com.example.fileleap.ui.theme.Secondary
import com.example.fileleap.ui.theme.Typography
import kotlin.math.roundToInt

@Composable
fun Transferring(
    scaffoldPadding: PaddingValues,
    bytesReceived: Long,
    bytesSent: Long
) {
    Column(
        modifier = Modifier
            .padding(scaffoldPadding)
            .padding(48.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        //Upper Texts
        Column(
            modifier = Modifier
                .padding(vertical = 32.dp)
                .fillMaxWidth()
        ){
            Text(
                text = "Simplified file sharing, speed and security redefined.",
                style = Typography.titleLarge,
                color = Primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Send files of any size directly from your device without ever storing anything online.",
                style = Typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Column(
            modifier = Modifier.padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Secondary, RoundedCornerShape(16.dp))
                    .padding(8.dp)
                    .clickable { },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Transferring",
                    style = Typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                )
                if(Constants.isSender){
                    Log.d(Constants.TAG,"BytesSent: $bytesSent")
                    Text(
                        text = "${((bytesSent*100.00)/Constants.fileSize.value).roundToInt()}%",
                        style = Typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier
                            .padding(4.dp)
                    )
                    LinearProgressIndicator(
                        progress = ((bytesSent*1.0)/Constants.fileSize.value).toFloat(),
                        modifier = Modifier
                            .padding(4.dp)
                    )
                }
                else{
                    Log.d(Constants.TAG,"BytesReceived: $bytesReceived")
                    Text(
                        text = "${((bytesReceived*100.00)/Constants.fileSize.value).roundToInt()}%",
                        style = Typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier
                            .padding(4.dp)
                    )
                    LinearProgressIndicator(
                        progress = ((bytesReceived*1.0)/Constants.fileSize.value).toFloat(),
                        modifier = Modifier
                            .padding(4.dp)
                    )
                }

                Text(
                    text = Constants.fileName,
                    style = Typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                )
            }

        }

    }
}