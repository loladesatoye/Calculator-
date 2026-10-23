package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class KeyType {
    object Number : KeyType()
    object Function : KeyType()
    object Operator : KeyType()
    object Accent : KeyType()
}

@Composable
fun CalculatorKeypad(
    isTyping: Boolean,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onDecimal: () -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onToggleSign: () -> Unit,
    onPercentage: () -> Unit,
    onParenthesis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val triggerHaptic = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick auxiliary ribbon for advanced minimalist actions: ( ), ±, 00, ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AuxiliaryKey(
                text = "( )",
                testTag = "key_parens",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onParenthesis()
                }
            )
            AuxiliaryKey(
                text = "±",
                testTag = "key_plus_minus",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onToggleSign()
                }
            )
            AuxiliaryKey(
                text = "%",
                testTag = "key_percent",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onPercentage()
                }
            )
            AuxiliaryKey(
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                testTag = "key_backspace",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onBackspace()
                }
            )
        }

        // Row 1: AC, 7, 8, ÷
        // Let's use the classic 4x4 + 1 keypad layout:
        // Row 1: AC / C, (, ), ÷
        // Row 2: 7, 8, 9, ×
        // Row 3: 4, 5, 6, −
        // Row 4: 1, 2, 3, +
        // Row 5: 0, ., = (with 0 spanning or 0, 00, ., =)

        // Main Keypad Rows
        // Row 1: Clear, (already have %, (), let's put AC, ⌫, %, ÷ or clean classic)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcButton(
                text = if (isTyping) "C" else "AC",
                type = KeyType.Function,
                testTag = "key_clear",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onClear()
                }
            )
            CalcButton(
                text = "00",
                type = KeyType.Number,
                testTag = "key_00",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDigit("00")
                }
            )
            CalcButton(
                text = "%",
                type = KeyType.Function,
                testTag = "key_percent_row",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onPercentage()
                }
            )
            CalcButton(
                text = "÷",
                type = KeyType.Operator,
                testTag = "key_divide",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onOperator("÷")
                }
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcButton(
                text = "7",
                type = KeyType.Number,
                testTag = "key_7",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDigit("7")
                }
            )
            CalcButton(
                text = "8",
                type = KeyType.Number,
                testTag = "key_8",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDigit("8")
                }
            )
            CalcButton(
                text = "9",
                type = KeyType.Number,
                testTag = "key_9",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDigit("9")
                }
            )
            CalcButton(
                text = "×",
                type = KeyType.Operator,
                testTag = "key_multiply",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onOperator("×")
                }
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcButton(
                text = "4",
                type = KeyType.Number,
                testTag = "key_4",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDigit("4")
                }
            )
            CalcButton(
                text = "5",
                type = KeyType.Number,
                testTag = "key_5",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDigit("5")
                }
            )
            CalcButton(
                text = "6",
                type = KeyType.Number,
                testTag = "key_6",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDigit("6")
                }
            )
            CalcButton(
                text = "−",
                type = KeyType.Operator,
                testTag = "key_subtract",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onOperator("−")
                }
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcButton(
                text = "1",
                type = KeyType.Number,
                testTag = "key_1",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDigit("1")
                }
            )
            CalcButton(
                text = "2",
                type = KeyType.Number,
                testTag = "key_2",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDigit("2")
                }
            )
            CalcButton(
                text = "3",
                type = KeyType.Number,
                testTag = "key_3",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDigit("3")
                }
            )
            CalcButton(
                text = "+",
                type = KeyType.Operator,
                testTag = "key_add",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onOperator("+")
                }
            )
        }

        // Row 5: 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcButton(
                text = "0",
                type = KeyType.Number,
                testTag = "key_0",
                modifier = Modifier.weight(2f), // Spans 2 columns nicely
                isWide = true,
                onClick = {
                    triggerHaptic()
                    onDigit("0")
                }
            )
            CalcButton(
                text = ".",
                type = KeyType.Number,
                testTag = "key_decimal",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onDecimal()
                }
            )
            CalcButton(
                text = "=",
                type = KeyType.Accent,
                testTag = "key_equals",
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onEquals()
                }
            )
        }
    }
}

@Composable
private fun CalcButton(
    text: String,
    type: KeyType,
    testTag: String,
    modifier: Modifier = Modifier,
    isWide: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = when (type) {
        KeyType.Number -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        KeyType.Function -> MaterialTheme.colorScheme.surfaceVariant
        KeyType.Operator -> MaterialTheme.colorScheme.primaryContainer
        KeyType.Accent -> MaterialTheme.colorScheme.primary
    }

    val contentColor = when (type) {
        KeyType.Number -> MaterialTheme.colorScheme.onSurface
        KeyType.Function -> MaterialTheme.colorScheme.onSurfaceVariant
        KeyType.Operator -> MaterialTheme.colorScheme.onPrimaryContainer
        KeyType.Accent -> MaterialTheme.colorScheme.onPrimary
    }

    val fontSize = when (type) {
        KeyType.Operator, KeyType.Accent -> 28.sp
        KeyType.Function -> 20.sp
        KeyType.Number -> 24.sp
    }

    val shape = RoundedCornerShape(26.dp)

    Surface(
        onClick = onClick,
        shape = shape,
        color = containerColor,
        modifier = modifier
            .then(
                if (isWide) {
                    Modifier.height(68.dp)
                } else {
                    Modifier.aspectRatio(1.15f)
                }
            )
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = if (isWide) Alignment.CenterStart else Alignment.Center,
            modifier = Modifier.padding(horizontal = if (isWide) 28.dp else 0.dp)
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = if (type == KeyType.Accent || type == KeyType.Operator) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AuxiliaryKey(
    text: String? = null,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier
            .height(44.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            } else if (text != null) {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
