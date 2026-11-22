package com.gmail.nthai.presentation.components.autocall
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.presentation.components.common.CampaignStatusBanner

@Composable
fun CallConfigTab(
    hasCallPermission: Boolean,
    onRequestPermission: () -> Unit,
    delaySeconds: Int,
    onDelayChange: (Int) -> Unit,
    selectedSimSlot: Int,
    onSimSlotChange: (Int) -> Unit,
    simConfigs: List<SimConfig>,
    totalRecipientsCount: Int,
    selectedRecipientsCount: Int,
    onNextToRecipients: () -> Unit,
    onImportExcel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Permission Banner Status Card
        CampaignStatusBanner(
            isGranted = hasCallPermission,
            title = "Quyền cuộc gọi: CHƯA CẤP",
            description = "Cần quyền CALL_PHONE và READ_PHONE_STATE để tự động gọi.",
            actionLabel = "Cấp quyền",
            onActionClick = onRequestPermission
        )

        // 2. SIM Selection & Delay Slider Config Card
        AutoCallConfigCard(
            delaySeconds = delaySeconds,
            onDelayChange = onDelayChange,
            selectedSimSlot = selectedSimSlot,
            onSimSlotChange = onSimSlotChange,
            simConfigs = simConfigs,
            totalCustomersCount = totalRecipientsCount,
            selectedCustomersCount = selectedRecipientsCount,
            isRunning = false,
            onStartClick = onNextToRecipients
        )

        // 3. Navigation & Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onImportExcel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF171717))
            ) {
                Icon(
                    Icons.Default.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nhập Excel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = onNextToRecipients,
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF171717),
                    contentColor = Color(0xFFFFFFFF)
                )
            ) {
                Text("Danh sách SĐT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
