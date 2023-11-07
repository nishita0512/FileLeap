package com.example.fileleap.ui.activities

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.example.fileleap.R
import com.example.fileleap.ui.Constants
import com.example.fileleap.ui.theme.Primary
import com.example.fileleap.ui.theme.Secondary
import com.example.fileleap.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendOrReceive(
    scaffoldPadding: PaddingValues,
    selectFile: () -> Unit,
    receiveFile: () -> Unit
){
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

        //Sender and Receiver Box
        Column(
            modifier = Modifier.padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            //Sender Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Secondary, RoundedCornerShape(16.dp))
                    .padding(8.dp)
                    .clickable { selectFile() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add File",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .padding(8.dp)
                )
                Text(
                    text = "Select  File to share",
                    style = Typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                )
                Text(
                    text = "up to 10GB",
                    style = Typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                )

            }

            Text(
                text = "OR",
                style = Typography.titleLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(8.dp)
            )

            //Receiver Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Secondary, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                var code by rememberSaveable {
                    mutableStateOf("")
                }
                val context = LocalContext.current
                val maxChar = 4
                Text(
                    text = "Enter Receiving Code",
                    style = Typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        if (it.length <= maxChar) code = it
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
                            text = "${code.length} / $maxChar",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                )
                Button(
                    onClick = {
                        if(code.length>4){
                            Toast.makeText(context,"Invalid Code", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Constants.documentId = code
                            receiveFile()
                        }
                    },
                    colors =  ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Receive",
                        style = Typography.bodyLarge,
                    )
                }
            }
        }

    }
}