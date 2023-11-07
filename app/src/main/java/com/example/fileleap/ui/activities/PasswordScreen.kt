package com.example.fileleap.ui.activities

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.fileleap.ui.WebRTCViewModel
import com.example.fileleap.ui.theme.FileLeapTheme
import com.example.fileleap.ui.theme.Primary
import com.example.fileleap.ui.theme.Secondary
import com.example.fileleap.ui.theme.Typography
import com.example.fileleap.ui.utils.BCrypt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(
    scaffoldPadding: PaddingValues,
    webRTCViewModel: WebRTCViewModel
){
    Column(
        modifier = Modifier
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
            modifier = Modifier
                .fillMaxWidth()
                .background(Secondary, RoundedCornerShape(16.dp))
                .padding(vertical = 40.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            var enteredPassword by rememberSaveable {
                mutableStateOf("")
            }
            val context = LocalContext.current
            val maxChar = 24
            Text(
                text = "Enter Password",
                style = Typography.titleMedium,
                color = Color.White,
                modifier = Modifier
                    .padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = enteredPassword,
                onValueChange = {
                    if (it.length <= maxChar) enteredPassword = it
                },
                maxLines = 1,
                textStyle = Typography.bodySmall,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    textColor = Color.White
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                supportingText = {
                    Text(
                        text = "${enteredPassword.length} / $maxChar",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
            )

//            val showProgressBar = remember { mutableStateOf(false) }
//
            if(Constants.showProgressBar.value){
                CircularProgressIndicator()
            }
            else {
                Button(
                    onClick = {
                        if (enteredPassword.length < 8) {
                            Toast.makeText(context, "Password Too Small", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Constants.password.value = enteredPassword
                            while (Constants.password.value.length % 4 != 0) {
                                Constants.password.value += '0'
                            }
                            Constants.hashedPassword.value = BCrypt.hashpw(enteredPassword, BCrypt.gensalt())
                            if (Constants.isSender) {
                                webRTCViewModel.initializeSenderConnection()
                            } else {
                                webRTCViewModel.initializeReceiverConnection()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Proceed",
                        style = Typography.bodyLarge,
                    )
                }
            }
        }

    }
}