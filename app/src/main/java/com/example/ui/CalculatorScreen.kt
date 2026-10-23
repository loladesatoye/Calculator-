package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.HistoryBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            val isWideScreen = maxWidth > 600.dp

            if (isWideScreen) {
                // Adaptive landscape / tablet side-by-side layout
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Calculator Pane
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        CalculatorTopBar(
                            themeMode = uiState.themeMode,
                            historyCount = uiState.history.size,
                            onCycleTheme = { viewModel.cycleThemeMode() },
                            onOpenHistory = { viewModel.setHistorySheetVisible(true) }
                        )

                        CalculatorDisplay(
                            expression = uiState.expression,
                            previewResult = uiState.previewResult,
                            errorMessage = uiState.errorMessage,
                            modifier = Modifier.weight(1f)
                        )

                        CalculatorKeypad(
                            isTyping = uiState.expression != "0" && !uiState.isResultEvaluated,
                            onDigit = { viewModel.onDigit(it) },
                            onOperator = { viewModel.onOperator(it) },
                            onDecimal = { viewModel.onDecimal() },
                            onEquals = { viewModel.onEquals() },
                            onClear = { viewModel.onClear() },
                            onBackspace = { viewModel.onBackspace() },
                            onToggleSign = { viewModel.onToggleSign() },
                            onPercentage = { viewModel.onPercentage() },
                            onParenthesis = { viewModel.onParenthesis() },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // On wide screens, history is persistently visible on the side
                    Surface(
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Calculation History",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (uiState.history.isNotEmpty()) {
                                    androidx.compose.material3.TextButton(onClick = { viewModel.onClearHistory() }) {
                                        Text("Clear", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            if (uiState.history.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No calculations yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                androidx.compose.foundation.lazy.LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(uiState.history.size) { idx ->
                                        val item = uiState.history[idx]
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp)
                                            ) {
                                                Text(
                                                    text = item.expression,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "= ${item.result}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Portrait standard layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 500.dp)
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    CalculatorTopBar(
                        themeMode = uiState.themeMode,
                        historyCount = uiState.history.size,
                        onCycleTheme = { viewModel.cycleThemeMode() },
                        onOpenHistory = { viewModel.setHistorySheetVisible(true) }
                    )

                    CalculatorDisplay(
                        expression = uiState.expression,
                        previewResult = uiState.previewResult,
                        errorMessage = uiState.errorMessage,
                        modifier = Modifier.weight(1f)
                    )

                    CalculatorKeypad(
                        isTyping = uiState.expression != "0" && !uiState.isResultEvaluated,
                        onDigit = { viewModel.onDigit(it) },
                        onOperator = { viewModel.onOperator(it) },
                        onDecimal = { viewModel.onDecimal() },
                        onEquals = { viewModel.onEquals() },
                        onClear = { viewModel.onClear() },
                        onBackspace = { viewModel.onBackspace() },
                        onToggleSign = { viewModel.onToggleSign() },
                        onPercentage = { viewModel.onPercentage() },
                        onParenthesis = { viewModel.onParenthesis() },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }

        // Modal Bottom Sheet for History
        if (uiState.isHistoryOpen) {
            HistoryBottomSheet(
                history = uiState.history,
                sheetState = sheetState,
                onDismiss = { viewModel.setHistorySheetVisible(false) },
                onRecallResult = { viewModel.onRecallHistoryResult(it) },
                onRecallExpression = { viewModel.onRecallHistoryExpression(it) },
                onClearHistory = { viewModel.onClearHistory() }
            )
        }
    }
}

@Composable
private fun CalculatorTopBar(
    themeMode: AppThemeMode,
    historyCount: Int,
    onCycleTheme: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App title with minimalist styling
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CALCULATOR",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme toggle button
            IconButton(
                onClick = onCycleTheme,
                modifier = Modifier.testTag("button_theme_toggle")
            ) {
                val icon = when (themeMode) {
                    AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                    AppThemeMode.DARK -> Icons.Default.DarkMode
                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Toggle theme mode",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // History button with badge
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.testTag("button_history")
            ) {
                if (historyCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text("$historyCount")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "View History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "View History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
