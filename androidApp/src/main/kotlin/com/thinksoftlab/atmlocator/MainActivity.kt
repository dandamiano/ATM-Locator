package com.thinksoftlab.atmlocator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.thinksoftlab.atmlocator.navigation.LocatorApp
import com.thinksoftlab.atmlocator.theme.AtmTheme
import com.thinksoftlab.atmlocator.theme.Paper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AtmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Paper,
                ) {
                    LocatorApp()
                }
            }
        }
    }
}
