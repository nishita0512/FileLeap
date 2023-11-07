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
fun AboutUsScreen(){
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
                text = "Why FileLeap?",
                style = Typography.titleLarge,
                color = Primary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "FileLeap emerges as a revolutionary peer-to-peer file transfer system that empowers users to share files directly bypassing the limitations of traditional server-based solutions.",
                style = Typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Justify
            )
            Text(
                text = "Designed for both Android and Web Platforms.",
                style = Typography.titleMedium,
                color = Primary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Justify
            )
            Text(
                text = "At the core, File Leap harnesses the power of decentralization, eliminating the need for a central server and fostering a more resilient and scalable infrastructure.",
                style = Typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Justify
            )
            Text(
                text = "FileLeap Developers",
                style = Typography.titleLarge,
                color = Primary,
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Yash Gurnani",
                style = Typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Mohit Gangwani",
                style = Typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Vinesh Choithramani",
                style = Typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Nishita Lotwani",
                style = Typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

    }
}

@Preview
@Composable
fun AboutUsScreenPreview(){
    AboutUsScreen()
}