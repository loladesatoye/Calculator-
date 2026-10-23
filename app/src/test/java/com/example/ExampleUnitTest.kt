package com.example

import com.example.logic.CalculatorEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBasicArithmetic() {
        val res1 = CalculatorEvaluator.evaluate("15 + 27")
        assertTrue(res1 is CalculatorEvaluator.EvaluationResult.Success)
        assertEquals("42", (res1 as CalculatorEvaluator.EvaluationResult.Success).formatted)

        val res2 = CalculatorEvaluator.evaluate("100 − 45")
        assertTrue(res2 is CalculatorEvaluator.EvaluationResult.Success)
        assertEquals("55", (res2 as CalculatorEvaluator.EvaluationResult.Success).formatted)

        val res3 = CalculatorEvaluator.evaluate("12 × 8")
        assertTrue(res3 is CalculatorEvaluator.EvaluationResult.Success)
        assertEquals("96", (res3 as CalculatorEvaluator.EvaluationResult.Success).formatted)

        val res4 = CalculatorEvaluator.evaluate("144 ÷ 12")
        assertTrue(res4 is CalculatorEvaluator.EvaluationResult.Success)
        assertEquals("12", (res4 as CalculatorEvaluator.EvaluationResult.Success).formatted)
    }

    @Test
    fun testOrderOfOperations() {
        val res = CalculatorEvaluator.evaluate("2 + 3 × 4")
        assertTrue(res is CalculatorEvaluator.EvaluationResult.Success)
        assertEquals("14", (res as CalculatorEvaluator.EvaluationResult.Success).formatted)
    }

    @Test
    fun testParenthesesAndBalancing() {
        val res1 = CalculatorEvaluator.evaluate("(2 + 3) × 4")
        assertTrue(res1 is CalculatorEvaluator.EvaluationResult.Success)
        assertEquals("20", (res1 as CalculatorEvaluator.EvaluationResult.Success).formatted)

        // Automatically balances unclosed parenthesis
        val res2 = CalculatorEvaluator.evaluate("(2 + 3")
        assertTrue(res2 is CalculatorEvaluator.EvaluationResult.Success)
        assertEquals("5", (res2 as CalculatorEvaluator.EvaluationResult.Success).formatted)
    }

    @Test
    fun testDecimalPrecision() {
        // Floating point quirk: 0.1 + 0.2
        val res = CalculatorEvaluator.evaluate("0.1 + 0.2")
        assertTrue(res is CalculatorEvaluator.EvaluationResult.Success)
        assertEquals("0.3", (res as CalculatorEvaluator.EvaluationResult.Success).formatted)
    }

    @Test
    fun testDivisionByZero() {
        val res = CalculatorEvaluator.evaluate("10 ÷ 0")
        assertTrue(res is CalculatorEvaluator.EvaluationResult.Error)
        assertEquals("Cannot divide by 0", (res as CalculatorEvaluator.EvaluationResult.Error).message)
    }

    @Test
    fun testPercentage() {
        val res = CalculatorEvaluator.evaluate("50 × 10%")
        assertTrue(res is CalculatorEvaluator.EvaluationResult.Success)
        assertEquals("5", (res as CalculatorEvaluator.EvaluationResult.Success).formatted)
    }

    @Test
    fun testThousandsFormatting() {
        val res = CalculatorEvaluator.evaluate("1000 × 1000")
        assertTrue(res is CalculatorEvaluator.EvaluationResult.Success)
        assertEquals("1,000,000", (res as CalculatorEvaluator.EvaluationResult.Success).formatted)
    }
}
