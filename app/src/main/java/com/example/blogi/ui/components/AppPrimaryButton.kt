package com.example.blogi.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.blogi.ui.theme.PrimaryButtonBackground
import com.example.blogi.ui.theme.PrimaryButtonText
object PrimaryButtonTokens {
    val buttonHeight: Dp = 36.dp
    val borderRadius: Dp = 6.dp
}

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            // MUUDETUD: Figma width(328.dp) asemel fillMaxWidth()
            .fillMaxWidth()
            // Figma height(36.dp)
            .height(PrimaryButtonTokens.buttonHeight),
        // Figma roundedMd
        shape = RoundedCornerShape(PrimaryButtonTokens.borderRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryButtonBackground,
            contentColor = PrimaryButtonText
        )
    ) {
        Text(text = text)
    }
}