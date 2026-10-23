package com.example.logic

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.ArrayDeque
import java.util.Locale

object CalculatorEvaluator {

    private val mathContext = MathContext(16, RoundingMode.HALF_UP)

    sealed class EvaluationResult {
        data class Success(val value: BigDecimal, val formatted: String) : EvaluationResult()
        data class Error(val message: String) : EvaluationResult()
    }

    /**
     * Evaluates a mathematical expression string (e.g. "12 + 3 × (4 - 1)").
     * Automatically balances unclosed parentheses if present.
     */
    fun evaluate(expression: String): EvaluationResult {
        val cleanExpr = sanitizeExpression(expression)
        if (cleanExpr.isEmpty()) {
            return EvaluationResult.Success(BigDecimal.ZERO, "0")
        }

        return try {
            val tokens = tokenize(cleanExpr)
            if (tokens.isEmpty()) {
                return EvaluationResult.Success(BigDecimal.ZERO, "0")
            }
            val postfix = toPostfix(tokens)
            val result = evaluatePostfix(postfix)
            EvaluationResult.Success(result, formatResult(result))
        } catch (e: ArithmeticException) {
            EvaluationResult.Error(e.message ?: "Cannot divide by 0")
        } catch (e: Exception) {
            EvaluationResult.Error("Invalid Expression")
        }
    }

    private fun sanitizeExpression(expr: String): String {
        return expr
            .replace(" ", "")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
    }

    private sealed class Token {
        data class Number(val value: BigDecimal) : Token()
        data class Operator(val op: Char, val precedence: Int, val isUnary: Boolean = false) : Token()
        object OpenParen : Token()
        object CloseParen : Token()
        object Percent : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectUnary = true // start of expr or right after '(', '+', '-', '*', '/'

        // Balance unclosed parentheses
        var openParenCount = 0
        for (ch in expr) {
            if (ch == '(') openParenCount++
            else if (ch == ')') openParenCount = maxOf(0, openParenCount - 1)
        }

        val balancedExpr = buildString {
            append(expr)
            repeat(openParenCount) { append(')') }
        }

        while (i < balancedExpr.length) {
            val c = balancedExpr[i]

            when {
                c.isDigit() || c == '.' -> {
                    val start = i
                    var hasDot = (c == '.')
                    i++
                    while (i < balancedExpr.length && (balancedExpr[i].isDigit() || (!hasDot && balancedExpr[i] == '.'))) {
                        if (balancedExpr[i] == '.') hasDot = true
                        i++
                    }
                    val numStr = balancedExpr.substring(start, i)
                    val num = if (numStr == ".") BigDecimal.ZERO else BigDecimal(numStr)
                    tokens.add(Token.Number(num))
                    expectUnary = false
                }
                c == '(' -> {
                    // Implicit multiplication: e.g. 5(2) -> 5 * (2)
                    if (tokens.isNotEmpty() && (tokens.last() is Token.Number || tokens.last() is Token.CloseParen || tokens.last() is Token.Percent)) {
                        tokens.add(Token.Operator('*', 2))
                    }
                    tokens.add(Token.OpenParen)
                    expectUnary = true
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.CloseParen)
                    expectUnary = false
                    i++
                }
                c == '%' -> {
                    tokens.add(Token.Percent)
                    expectUnary = false
                    i++
                }
                c == '+' || c == '-' || c == '*' || c == '/' -> {
                    if (expectUnary && c == '-') {
                        // Unary negation
                        tokens.add(Token.Operator('u', 3, isUnary = true))
                    } else if (expectUnary && c == '+') {
                        // Unary plus (noop, ignore)
                    } else {
                        val precedence = if (c == '*' || c == '/') 2 else 1
                        tokens.add(Token.Operator(c, precedence))
                    }
                    expectUnary = true
                    i++
                }
                else -> {
                    i++
                }
            }
        }
        return tokens
    }

    private fun toPostfix(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val opStack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> output.add(token)
                is Token.Percent -> {
                    // Postfix unary operator, applied immediately
                    output.add(token)
                }
                is Token.OpenParen -> opStack.push(token)
                is Token.CloseParen -> {
                    while (opStack.isNotEmpty() && opStack.peek() !is Token.OpenParen) {
                        output.add(opStack.pop())
                    }
                    if (opStack.isNotEmpty() && opStack.peek() is Token.OpenParen) {
                        opStack.pop()
                    }
                }
                is Token.Operator -> {
                    while (opStack.isNotEmpty()) {
                        val top = opStack.peek()
                        if (top is Token.Operator) {
                            if ((!token.isUnary && top.precedence >= token.precedence) ||
                                (token.isUnary && top.precedence > token.precedence)
                            ) {
                                output.add(opStack.pop())
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    opStack.push(token)
                }
            }
        }

        while (opStack.isNotEmpty()) {
            val top = opStack.pop()
            if (top !is Token.OpenParen && top !is Token.CloseParen) {
                output.add(top)
            }
        }

        return output
    }

    private fun evaluatePostfix(postfix: List<Token>): BigDecimal {
        val stack = ArrayDeque<BigDecimal>()

        for (token in postfix) {
            when (token) {
                is Token.Number -> stack.push(token.value)
                is Token.Percent -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Malformed percent expression")
                    val a = stack.pop()
                    val result = a.divide(BigDecimal("100"), mathContext)
                    stack.push(result)
                }
                is Token.Operator -> {
                    if (token.isUnary && token.op == 'u') {
                        if (stack.isEmpty()) throw IllegalArgumentException("Malformed unary minus")
                        val a = stack.pop()
                        stack.push(a.negate())
                    } else {
                        if (stack.size < 2) throw IllegalArgumentException("Malformed binary expression")
                        val b = stack.pop()
                        val a = stack.pop()
                        val res = when (token.op) {
                            '+' -> a.add(b, mathContext)
                            '-' -> a.subtract(b, mathContext)
                            '*' -> a.multiply(b, mathContext)
                            '/' -> {
                                if (b.compareTo(BigDecimal.ZERO) == 0) {
                                    throw ArithmeticException("Cannot divide by 0")
                                }
                                a.divide(b, mathContext)
                            }
                            else -> throw IllegalArgumentException("Unknown operator ${token.op}")
                        }
                        stack.push(res)
                    }
                }
                else -> {}
            }
        }

        if (stack.isEmpty()) return BigDecimal.ZERO
        return stack.pop()
    }

    fun formatResult(value: BigDecimal): String {
        val stripped = value.stripTrailingZeros()
        // Check for scientific notation if number is exceedingly huge or tiny
        val plain = stripped.toPlainString()
        if (plain.length > 14 || (plain.contains(".") && plain.substringAfter(".").length > 8)) {
            // If scale is large or number exceeds 12 digits, round cleanly or use scientific
            val abs = stripped.abs()
            if (abs.compareTo(BigDecimal("1000000000000")) >= 0 || (abs.compareTo(BigDecimal.ZERO) > 0 && abs.compareTo(BigDecimal("0.000001")) < 0)) {
                val sciFormat = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
                return sciFormat.format(value.toDouble())
            }
            // Round to 8 decimal places
            val rounded = stripped.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros()
            return formatWithThousands(rounded)
        }
        return formatWithThousands(stripped)
    }

    private fun formatWithThousands(value: BigDecimal): String {
        val plain = value.toPlainString()
        val parts = plain.split(".")
        val integerPart = parts[0]
        val isNegative = integerPart.startsWith("-")
        val cleanInt = if (isNegative) integerPart.substring(1) else integerPart

        val grouped = cleanInt.reversed().chunked(3).joinToString(",").reversed()
        val formattedInt = if (isNegative) "-$grouped" else grouped

        return if (parts.size > 1 && parts[1].isNotEmpty()) {
            "$formattedInt.${parts[1]}"
        } else {
            formattedInt
        }
    }
}
