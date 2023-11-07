package com.example.fileleap.ui.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fileleap.ui.theme.Primary
import com.example.fileleap.ui.theme.Typography

@Composable
fun HowItWorksScreen(){
    Column(
        modifier = Modifier
            .padding(48.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Column(
            modifier = Modifier
                .padding(vertical = 32.dp)
                .fillMaxWidth()
        ){
            Text(
                text = "How It Works?",
                style = Typography.titleLarge,
                color = Primary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "File Leap is a peer-to-peer file sharing system that uses WebRTC for peer-to-peer connection and AES encryption to encrypt data while transferring. This means that your files are shared directly between your computer and the receiver's computer, without going through a central server. This makes File Leap very fast and secure.",
                style = Typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Justify
            )
            Text(
                text = "To send a file with File Leap:",
                style = Typography.titleMedium,
                color = Primary,
                modifier = Modifier
                    .padding(top = 32.dp,bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Justify
            )
            Text(
                text = "1. Select the file you want to share.\n" +
                        "2. Enter a password for the file. This password will be used to encrypt the file before it is transferred.\n" +
                        "3. Share the 4-digit sharing code with the receiver. The receiver can use this code to connect to your computer and download the file.",
                style = Typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Justify
            )
            Text(
                text = "To receive a file with File Leap:",
                style = Typography.titleMedium,
                color = Primary,
                modifier = Modifier
                    .padding(top = 32.dp,bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Justify
            )
            Text(
                text = "1. Enter the 4-digit sharing code that the sender gave you.\n" +
                        "2. Enter the password that the sender gave you. This password will be used to decrypt the file after it is downloaded.\n" +
                        "3. File Leap will start downloading the file.",
                style = Typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Justify
            )
        }

    }
}

@Preview
@Composable
fun HowItWorksScreenPreview(){
    HowItWorksScreen()
}