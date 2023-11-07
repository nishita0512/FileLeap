package com.example.fileleap.ui.activities

import android.widget.ProgressBar
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fileleap.R
import com.example.fileleap.ui.Constants
import com.example.fileleap.ui.theme.FileLeapTheme
import com.example.fileleap.ui.theme.Primary
import com.example.fileleap.ui.theme.Secondary
import com.example.fileleap.ui.theme.Typography

@Composable
fun FileSelected(
    scaffoldPadding: PaddingValues
){
    Column(
        modifier = androidx.compose.ui.Modifier
            .padding(scaffoldPadding)
            .padding(48.dp),
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
                    text = "Sharing Code:",
                    style = Typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                )
                Text(
                    text = Constants.documentId,
                    style = Typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                )
                Text(
                    text = Constants.fileName,
                    style = Typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                )
                Text(
                    text = "${Constants.fileSize.value}KB",
                    style = Typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                )

            }

        }

    }
}

@Preview
@Composable
fun FileSelectedPreview(){
    FileLeapTheme{
        FileSelected(scaffoldPadding = PaddingValues(8.dp))
    }
}