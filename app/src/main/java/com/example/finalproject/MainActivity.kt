package com.example.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.finalproject.ui.navigation.AppNav
import com.example.finalproject.ui.theme.FinalprojectTheme
import dagger.hilt.android.AndroidEntryPoint

// AI Generated.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalprojectTheme {
                AppNav()
            }
        }
    }
}