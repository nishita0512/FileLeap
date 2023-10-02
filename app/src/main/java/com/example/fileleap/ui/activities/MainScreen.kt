package com.example.fileleap.ui.activities

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.fileleap.ui.theme.FileLeapTheme
import com.example.fileleap.ui.theme.Primary
import com.example.fileleap.ui.theme.Secondary
import com.example.fileleap.ui.theme.Typography
import kotlinx.coroutines.launch

data class NavigationItem(
    val title: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    selectFile: () -> Unit,
    receiveFile: (firestoreDocumentId: String) -> Unit
){
    val items = listOf(
        NavigationItem(
            title = "Home"
        ),
        NavigationItem(
            title = "How it works?"
        ),
        NavigationItem(
            title = "About Us"
        ),
        NavigationItem(
            title = "FAQ"
        ),
        NavigationItem(
            title = "Privacy Policy"
        ),
    )
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ){
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var selectedItemIndex by rememberSaveable {
            mutableStateOf(0)
        }

        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet{
                    Spacer(modifier = Modifier.height(16.dp))
                    items.forEachIndexed{index, item ->
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = item.title,
                                    style = Typography.bodyLarge
                                )
                            },
                            selected = index == selectedItemIndex,
                            onClick = {
                                selectedItemIndex = index
                                scope.launch{
                                    drawerState.close()
                                }
                            },
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding),

                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Primary
                            )
                        )
                    }
                }
            },
            drawerState = drawerState
        ) {
            Scaffold(
                containerColor = Color.Black,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "File Leap",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp),
                                textAlign = TextAlign.Center,
                                color = Primary,
                                style = Typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch{
                                        drawerState.open()
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Primary
                                )
                            ){
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = TopAppBarDefaults.mediumTopAppBarColors(
                            containerColor = Color.Black
                        )
                    )
                }
            ){ scaffoldPadding ->
                when(selectedItemIndex){
                    0->{
                        HomePage(
                            scaffoldPadding = scaffoldPadding,
                            selectFile,
                            receiveFile
                        )
                    }
                    else->{

                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    scaffoldPadding: PaddingValues,
    selectFile: () -> Unit,
    receiveFile: (firestoreDocumentId: String) -> Unit
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

        //Sender and Receiver Box
        Column(
            modifier = Modifier.padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            //Sender Box
            Column(
                modifier = Modifier
                    .weight(4f)
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
                        .weight(5f)
                        .padding(8.dp)
                )
                Text(
                    text = "Select  File to share",
                    style = Typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .weight(3f)
                        .padding(8.dp)
                )
                Text(
                    text = "up to 10GB",
                    style = Typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .weight(2f)
                        .padding(4.dp)
                )

            }

            Text(
                text = "OR",
                style = Typography.titleLarge,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )

            //Receiver Box
            Column(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxWidth()
                    .background(Secondary, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                var code by rememberSaveable {
                    mutableStateOf("")
                }
                val context = LocalContext.current
                val maxChar = 24
                Text(
                    text = "Enter Receiving Code",
                    style = Typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        if (it.length <= maxChar) code = it
                    },
                    maxLines = 1,
                    textStyle = Typography.bodyMedium,
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(2f)
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
                        if(code.length>24){
                            Toast.makeText(context,"Invalid Code", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            receiveFile(code)
                        }
                    },
                    colors =  ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1.5f)
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

@Preview
@Composable
fun MainScreenPreview() {
    FileLeapTheme {
        MainScreen({},{})
    }
}