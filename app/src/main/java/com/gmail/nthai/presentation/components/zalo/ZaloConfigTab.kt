package com.gmail.nthai.presentation.components.zalo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ZaloConfigTab(
    isAccessibilityEnabled: Boolean = false,
    messageContent: String = "{Xin chào|Chào} {ten_khach}, bên em đang có chương trình ưu đãi tri ân khách hàng. Vui lòng nhắn tin lại để nhận mã giảm giá nhé!",
    onMessageContentChange: (String) -> Unit = {},
    delayRange: ClosedFloatingPointRange<Float> = 10f..25f,
    onDelayRangeChange: (ClosedFloatingPointRange<Float>) -> Unit = {},
    autoAddFriend: Boolean = true,
    onAutoAddFriendChange: (Boolean) -> Unit = {},
    sendOnlyIfUnsent: Boolean = true,
    onSendOnlyIfUnsentChange: (Boolean) -> Unit = {},
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
        // 1. Accessibility Status Banner Card (Only show if not enabled)
        if (!isAccessibilityEnabled) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFFFFF),
                border = BorderStroke(1.dp, Color(0xFFFEE2E2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFEE0000), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trợ năng Zalo: CHƯA BẬT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Cần cấp quyền Trợ năng (Accessibility) để tự động tìm SĐT & gửi tin Zalo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenAccessibilitySettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF171717),
                            contentColor = Color(0xFFFFFFFF)
                        ),
                        shape = RoundedCornerShape(9999.dp)
                    ) {
                        Text(
                            text = "Bật ngay",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Message Template & Spintax Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, Color(0xFFEBEBEB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nội dung tin nhắn Zalo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                    Text(
                        text = "Hỗ trợ Spintax {A|B}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888888),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = messageContent,
                    onValueChange = onMessageContentChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    placeholder = { Text("Nhập nội dung tin nhắn Zalo...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF171717),
                        unfocusedBorderColor = Color(0xFFEBEBEB),
                        focusedContainerColor = Color(0xFFFFFFFF),
                        unfocusedContainerColor = Color(0xFFFFFFFF)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Chèn nhanh biến cá nhân hóa:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF888888)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = { onMessageContentChange(messageContent + " {ten_khach}") },
                        label = { Text("{ten_khach}", color = Color(0xFF171717)) },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF171717)) },
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFFFFFFF))
                    )
                    AssistChip(
                        onClick = { onMessageContentChange(messageContent + " {sdt}") },
                        label = { Text("{sdt}", color = Color(0xFF171717)) },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF171717)) },
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFFFFFFF))
                    )
                    AssistChip(
                        onClick = { onMessageContentChange(messageContent + " {Xin chào|Chào anh/chị}") },
                        label = { Text("Spintax {A|B}", color = Color(0xFF171717)) },
                        border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF5F5F5))
                    )
                }
            }
        }

        // 3. Sending Interval & Anti-Block Settings
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, Color(0xFFEBEBEB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF171717),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Giãn cách & Chống khóa Zalo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Giãn cách giữa 2 tin nhắn: ${delayRange.start.roundToInt()}s - ${delayRange.endInclusive.roundToInt()}s",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF171717)
                )
                Text(
                    text = "Thời gian ngẫu nhiên giúp hạn chế bị hệ thống Zalo quét Spam.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )

                RangeSlider(
                    value = delayRange,
                    onValueChange = onDelayRangeChange,
                    valueRange = 5f..60f,
                    steps = 11,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF171717),
                        activeTrackColor = Color(0xFF171717),
                        inactiveTrackColor = Color(0xFFEBEBEB)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tự động gửi lời mời kết bạn",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF171717)
                        )
                        Text(
                            text = "Tự bấm 'Kết Bạn' trên Zalo nếu số điện thoại chưa là bạn bè",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                    Switch(
                        checked = autoAddFriend,
                        onCheckedChange = onAutoAddFriendChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFFFFF),
                            checkedTrackColor = Color(0xFF171717),
                            uncheckedThumbColor = Color(0xFF888888),
                            uncheckedTrackColor = Color(0xFFEBEBEB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bỏ qua SĐT đã gửi thành công",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF171717)
                        )
                        Text(
                            text = "Không gửi lại cho các SĐT đã thành công trước đây",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                    Switch(
                        checked = sendOnlyIfUnsent,
                        onCheckedChange = onSendOnlyIfUnsentChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFFFFF),
                            checkedTrackColor = Color(0xFF171717),
                            uncheckedThumbColor = Color(0xFF888888),
                            uncheckedTrackColor = Color(0xFFEBEBEB)
                        )
                    )
                }
            }
        }

        // 5. Main Action Buttons
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
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
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
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
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
