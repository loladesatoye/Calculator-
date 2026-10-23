package com.example.model

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class CalculatorState(
    val expression: String = "0",
    val previewResult: String? = null,
    val isResultEvaluated: Boolean = false,
    val errorMessage: String? = null,
    val history: List<CalculationHistoryItem> = emptyList(),
    val isHistoryOpen: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)
