package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.logic.CalculatorEvaluator
import com.example.model.AppThemeMode
import com.example.model.CalculationHistoryItem
import com.example.model.CalculatorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorState())
    val uiState: StateFlow<CalculatorState> = _uiState.asStateFlow()

    fun onDigit(digit: String) {
        _uiState.update { current ->
            val newExpr = if (current.isResultEvaluated || current.expression == "0") {
                digit
            } else {
                current.expression + digit
            }
            current.copy(
                expression = newExpr,
                isResultEvaluated = false,
                errorMessage = null,
                previewResult = computePreview(newExpr)
            )
        }
    }

    fun onOperator(op: String) {
        _uiState.update { current ->
            val expr = current.expression.trimEnd()
            val newExpr = when {
                current.isResultEvaluated -> {
                    "$expr $op "
                }
                isLastTokenOperator(expr) -> {
                    // Replace previous operator
                    replaceLastOperator(expr, op)
                }
                expr.endsWith("(") -> {
                    // Don't append binary operator right after '(' unless it's '-'
                    if (op == "−") "$expr−" else expr
                }
                else -> {
                    "$expr $op "
                }
            }
            current.copy(
                expression = newExpr,
                isResultEvaluated = false,
                errorMessage = null,
                previewResult = computePreview(newExpr)
            )
        }
    }

    fun onDecimal() {
        _uiState.update { current ->
            if (current.isResultEvaluated) {
                return@update current.copy(
                    expression = "0.",
                    isResultEvaluated = false,
                    errorMessage = null,
                    previewResult = null
                )
            }

            val expr = current.expression
            val lastToken = expr.split(" ").lastOrNull() ?: ""

            val newExpr = when {
                lastToken.isEmpty() || isOperatorChar(lastToken) -> "$expr 0."
                lastToken.endsWith("(") -> "${expr}0."
                lastToken.contains(".") -> expr // Already has decimal
                else -> "$expr."
            }

            current.copy(
                expression = newExpr,
                errorMessage = null,
                previewResult = computePreview(newExpr)
            )
        }
    }

    fun onToggleSign() {
        _uiState.update { current ->
            val expr = current.expression
            if (expr == "0") return@update current

            // Toggle sign of last number token
            val tokens = expr.split(" ").toMutableList()
            if (tokens.isEmpty()) return@update current

            val last = tokens.removeAt(tokens.size - 1)
            val toggled = when {
                last.startsWith("(-") && last.endsWith(")") -> {
                    last.substring(2, last.length - 1)
                }
                last.startsWith("-") -> {
                    last.substring(1)
                }
                last.isNotEmpty() && !isOperatorChar(last) && last != "(" && last != ")" -> {
                    "(-$last)"
                }
                else -> last
            }
            tokens.add(toggled)
            val newExpr = tokens.joinToString(" ")
            current.copy(
                expression = newExpr,
                errorMessage = null,
                previewResult = computePreview(newExpr)
            )
        }
    }

    fun onPercentage() {
        _uiState.update { current ->
            val expr = current.expression.trimEnd()
            if (expr == "0" || isLastTokenOperator(expr) || expr.endsWith("(") || expr.endsWith("%")) {
                return@update current
            }
            val newExpr = "$expr%"
            current.copy(
                expression = newExpr,
                errorMessage = null,
                previewResult = computePreview(newExpr)
            )
        }
    }

    fun onParenthesis() {
        _uiState.update { current ->
            val expr = current.expression
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }

            val trimmed = expr.trimEnd()
            val lastChar = trimmed.lastOrNull()

            val newExpr = when {
                expr == "0" || current.isResultEvaluated -> "("
                openCount > closeCount && (lastChar?.isDigit() == true || lastChar == ')' || lastChar == '%') -> "$trimmed)"
                lastChar?.isDigit() == true || lastChar == ')' || lastChar == '%' -> "$trimmed × ("
                lastChar == '(' -> "$trimmed("
                else -> "$trimmed ("
            }

            current.copy(
                expression = newExpr,
                isResultEvaluated = false,
                errorMessage = null,
                previewResult = computePreview(newExpr)
            )
        }
    }

    fun onBackspace() {
        _uiState.update { current ->
            if (current.isResultEvaluated || current.errorMessage != null) {
                return@update current.copy(
                    expression = "0",
                    isResultEvaluated = false,
                    errorMessage = null,
                    previewResult = null
                )
            }

            var expr = current.expression.trimEnd()
            if (expr.length <= 1 || expr == "0") {
                return@update current.copy(
                    expression = "0",
                    previewResult = null,
                    errorMessage = null
                )
            }

            // If it ends with " (-" or "(-5)", remove gracefully
            if (expr.endsWith(" ")) {
                expr = expr.dropLast(1)
            }
            expr = expr.dropLast(1).trimEnd()
            if (expr.isEmpty()) expr = "0"

            current.copy(
                expression = expr,
                errorMessage = null,
                previewResult = computePreview(expr)
            )
        }
    }

    fun onClear() {
        _uiState.update { current ->
            current.copy(
                expression = "0",
                previewResult = null,
                isResultEvaluated = false,
                errorMessage = null
            )
        }
    }

    fun onEquals() {
        val current = _uiState.value
        val expr = current.expression.trimEnd()

        // Remove trailing operator if present before evaluating
        val evaluableExpr = if (isLastTokenOperator(expr)) {
            dropLastOperator(expr)
        } else {
            expr
        }

        when (val result = CalculatorEvaluator.evaluate(evaluableExpr)) {
            is CalculatorEvaluator.EvaluationResult.Success -> {
                val historyItem = CalculationHistoryItem(
                    expression = evaluableExpr,
                    result = result.formatted
                )
                val updatedHistory = (listOf(historyItem) + current.history).take(50)

                _uiState.update {
                    it.copy(
                        expression = result.formatted,
                        previewResult = null,
                        isResultEvaluated = true,
                        errorMessage = null,
                        history = updatedHistory
                    )
                }
            }
            is CalculatorEvaluator.EvaluationResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = result.message,
                        previewResult = null,
                        isResultEvaluated = false
                    )
                }
            }
        }
    }

    fun onRecallHistoryResult(result: String) {
        _uiState.update { current ->
            val cleanResult = result.replace(",", "")
            current.copy(
                expression = cleanResult,
                isResultEvaluated = true,
                errorMessage = null,
                previewResult = null,
                isHistoryOpen = false
            )
        }
    }

    fun onRecallHistoryExpression(expression: String) {
        _uiState.update { current ->
            current.copy(
                expression = expression,
                isResultEvaluated = false,
                errorMessage = null,
                previewResult = computePreview(expression),
                isHistoryOpen = false
            )
        }
    }

    fun onClearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun setHistorySheetVisible(isOpen: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = isOpen) }
    }

    fun cycleThemeMode() {
        _uiState.update { current ->
            val next = when (current.themeMode) {
                AppThemeMode.SYSTEM -> AppThemeMode.DARK
                AppThemeMode.DARK -> AppThemeMode.LIGHT
                AppThemeMode.LIGHT -> AppThemeMode.SYSTEM
            }
            current.copy(themeMode = next)
        }
    }

    private fun computePreview(expr: String): String? {
        val trimmed = expr.trimEnd()
        // Only show preview if there is at least one operator and it's not just a single number
        val hasOperator = trimmed.contains("+") || trimmed.contains("−") ||
                trimmed.contains("×") || trimmed.contains("÷") || trimmed.contains("%")

        if (!hasOperator || isLastTokenOperator(trimmed)) {
            return null
        }

        return when (val res = CalculatorEvaluator.evaluate(trimmed)) {
            is CalculatorEvaluator.EvaluationResult.Success -> res.formatted
            is CalculatorEvaluator.EvaluationResult.Error -> null
        }
    }

    private fun isLastTokenOperator(expr: String): Boolean {
        val last = expr.split(" ").lastOrNull()?.trim() ?: return false
        return isOperatorChar(last)
    }

    private fun isOperatorChar(token: String): Boolean {
        return token == "+" || token == "−" || token == "×" || token == "÷"
    }

    private fun replaceLastOperator(expr: String, newOp: String): String {
        val tokens = expr.split(" ").toMutableList()
        while (tokens.isNotEmpty() && (tokens.last().isBlank() || isOperatorChar(tokens.last()))) {
            tokens.removeAt(tokens.size - 1)
        }
        tokens.add(newOp)
        return tokens.joinToString(" ") + " "
    }

    private fun dropLastOperator(expr: String): String {
        val tokens = expr.split(" ").toMutableList()
        while (tokens.isNotEmpty() && (tokens.last().isBlank() || isOperatorChar(tokens.last()))) {
            tokens.removeAt(tokens.size - 1)
        }
        return tokens.joinToString(" ")
    }
}
