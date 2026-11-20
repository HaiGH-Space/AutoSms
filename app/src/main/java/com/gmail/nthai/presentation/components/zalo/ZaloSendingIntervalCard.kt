package com.gmail.nthai.presentation.components.zalo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.nthai.presentation.components.common.AutoSmsCardContainer
import kotlin.math.roundToInt

@Composable
fun ZaloSendingIntervalCard(
    delayRange: ClosedFloatingPointRange<Float>,
    onDelayRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    autoAddFriend: Boolean,
    onAutoAddFriendChange: (Boolean) -> Unit,
    sendOnlyIfUnsent: Boolean,
    onSendOnlyIfUnsentChange: (Boolean) -> Unit,
    isLowSpecDevice: Boolean,
    onIsLowSpecDeviceChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AutoSmsCardContainer(modifier = modifier) {
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tối ưu cho máy cấu hình yếu (Máy chậm)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF171717)
                )
                Text(
                    text = "Tăng thời gian chờ Zalo (18s), thêm lượt thử lại dán & bấm gửi để tránh lỗi timeout",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
            Switch(
                checked = isLowSpecDevice,
                onCheckedChange = onIsLowSpecDeviceChange,
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
