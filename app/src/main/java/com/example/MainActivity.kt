package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppThemeMode
import com.example.ui.CalculatorScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val calcViewModel: CalculatorViewModel = viewModel()
            val uiState by calcViewModel.uiState.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (uiState.themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                CalculatorScreen(viewModel = calcViewModel)
            }
        }
    }
}
