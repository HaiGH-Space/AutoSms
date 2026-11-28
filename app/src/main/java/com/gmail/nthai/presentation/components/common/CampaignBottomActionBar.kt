package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CampaignBottomActionBar(
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryButtonIcon: ImageVector? = null,
    secondaryButtonText: String? = null,
    secondaryButtonIcon: ImageVector? = null,
    onSecondaryClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (secondaryButtonText != null && onSecondaryClick != null) {
            OutlinedButton(
                onClick = onSecondaryClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF171717))
            ) {
                if (secondaryButtonIcon != null) {
                    Icon(
                        imageVector = secondaryButtonIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(secondaryButtonText, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
        }

        Button(
            onClick = onPrimaryClick,
            enabled = enabled,
            modifier = Modifier
                .weight(if (secondaryButtonText != null) 1.4f else 1f)
                .height(48.dp),
            shape = RoundedCornerShape(9999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF171717),
                contentColor = Color(0xFFFFFFFF),
                disabledContainerColor = Color(0xFFEBEBEB),
                disabledContentColor = Color(0xFF888888)
            )
        ) {
            if (primaryButtonIcon != null) {
                Icon(
                    imageVector = primaryButtonIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(primaryButtonText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}
