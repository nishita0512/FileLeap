package com.example.fileleap

import android.annotation.SuppressLint
import android.graphics.Paint.Style
import android.os.Bundle
import com.example.fileleap.ui.theme.Primary
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fileleap.ui.theme.FileLeapTheme
import kotlinx.coroutines.launch


data class NavigationItem(
    val title: String,
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FileLeapTheme{
                val items = listOf(
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
                                            Text(text = item.title)
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
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(text = "File Leap",
                                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                                            textAlign = TextAlign.Center,
                                            color = Primary
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch{
                                                drawerState.open()
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                )
                            }
                        ){
                        }
                    }
                }
            }
        }
    }
}

/*@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview
@Composable
fun GreetingPreview() {
    FileLeapTheme {
        Greeting("Android")
    }

}*/