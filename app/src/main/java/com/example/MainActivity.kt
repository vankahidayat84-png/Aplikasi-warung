package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.WarungApp
import com.example.ui.WarungViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WarungViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val darkThemeEnabled by viewModel.darkThemeEnabled.collectAsState()

            MyApplicationTheme(darkTheme = darkThemeEnabled) {
                WarungApp(viewModel = viewModel)
            }
        }
    }
}
