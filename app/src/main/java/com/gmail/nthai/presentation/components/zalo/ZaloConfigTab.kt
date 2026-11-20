package com.gmail.nthai.presentation.components.zalo

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.domain.model.SmsTemplate

@Composable
fun ZaloConfigTab(
    isAccessibilityEnabled: Boolean = false,
    templates: List<SmsTemplate> = emptyList(),
    selectedTemplateId: Long? = null,
    onSelectTemplate: (SmsTemplate) -> Unit = {},
    messageContent: String = "{Xin chào|Chào} {ten_khach}, bên em đang có chương trình ưu đãi tri ân khách hàng. Vui lòng nhắn tin lại để nhận mã giảm giá nhé!",
    onMessageContentChange: (String) -> Unit = {},
    delayRange: ClosedFloatingPointRange<Float> = 20f..30f,
    onDelayRangeChange: (ClosedFloatingPointRange<Float>) -> Unit = {},
    autoAddFriend: Boolean = false,
    onAutoAddFriendChange: (Boolean) -> Unit = {},
    sendOnlyIfUnsent: Boolean = false,
    onSendOnlyIfUnsentChange: (Boolean) -> Unit = {},
    isLowSpecDevice: Boolean = false,
    onIsLowSpecDeviceChange: (Boolean) -> Unit = {},
    onStartSending: () -> Unit = {},
    onNextToRecipients: () -> Unit = {},
    onImportExcel: () -> Unit = {},
    onOpenAccessibilitySettings: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Accessibility Status Banner Card
        ZaloAccessibilityStatusCard(
            isAccessibilityEnabled = isAccessibilityEnabled,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings
        )

        // 2. Message Template & Spintax Card
        ZaloMessageTemplateCard(
            templates = templates,
            selectedTemplateId = selectedTemplateId,
            onSelectTemplate = onSelectTemplate,
            messageContent = messageContent,
            onMessageContentChange = onMessageContentChange
        )

        // 3. Sending Interval & Anti-Block Settings Card
        ZaloSendingIntervalCard(
            delayRange = delayRange,
            onDelayRangeChange = onDelayRangeChange,
            autoAddFriend = autoAddFriend,
            onAutoAddFriendChange = onAutoAddFriendChange,
            sendOnlyIfUnsent = sendOnlyIfUnsent,
            onSendOnlyIfUnsentChange = onSendOnlyIfUnsentChange,
            isLowSpecDevice = isLowSpecDevice,
            onIsLowSpecDeviceChange = onIsLowSpecDeviceChange
        )

        // 4. Main Action Buttons
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

@Preview(showBackground = true)
@Composable
fun ZaloConfigTabPreview() {
    MaterialTheme {
        ZaloConfigTab(
            onStartSending = {},
            onImportExcel = {},
            onOpenAccessibilitySettings = {}
        )
    }
}
