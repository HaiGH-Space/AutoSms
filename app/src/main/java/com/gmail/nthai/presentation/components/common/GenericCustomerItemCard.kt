package com.gmail.nthai.presentation.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.usecase.DetectVietnameseCarrierUseCase

data class GenericCustomerItemData(
    val id: Long,
    val name: String,
    val phone: String,
    val subtitle: String? = null,
    val status: LogStatus = LogStatus.PENDING,
    val isSelected: Boolean = false,
    val note: String? = null,
)

@Composable
fun GenericCustomerItemCard(
    item: GenericCustomerItemData,
    modifier: Modifier = Modifier,
    onToggleSelection: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    isBlacklisted: Boolean = false,
    onAddToBlacklist: (() -> Unit)? = null,
) {
    val borderColor = if (item.isSelected) Color(0xFF171717) else Color(0xFFEBEBEB)
    val cardBackground = if (item.isSelected) Color(0xFFFAFAFA) else Color(0xFFFFFFFF)

    val carrierName = remember(item.phone) {
        DetectVietnameseCarrierUseCase.detect(item.phone)
    }

    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardBackground,
        border = BorderStroke(if (item.isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox chọn
            if (onToggleSelection != null) {
                Checkbox(
                    checked = item.isSelected,
                    onCheckedChange = { onToggleSelection() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF171717),
                        uncheckedColor = Color(0xFF888888),
                        checkmarkColor = Color(0xFFFFFFFF)
                    ),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            // Main Info: Badges (Nhà mạng, Blacklist), Tên, SĐT, Ghi chú
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Badges thông tin bên trái (Nhà mạng, Blacklist)
                val hasBadges = carrierName != null || isBlacklisted
                if (hasBadges) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (carrierName != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = carrierName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4D4D4D)
                                )
                            }
                        }

                        if (isBlacklisted) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFFF7D4D6))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Blacklist",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFC50000)
                                )
                            }
                        }
                    }
                }

                // Tên khách hàng
                Text(
                    text = item.name.ifBlank { item.phone },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF171717),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // SĐT + Subtitle (ví dụ: · Chưa kết bạn / · Bạn bè)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Color(0xFF888888)
                    )
                    Text(
                        text = item.phone,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                        color = Color(0xFF888888)
                    )

                    if (!item.subtitle.isNullOrBlank()) {
                        Text(
                            text = "· ${item.subtitle}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                            color = Color(0xFF888888),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Ghi chú nếu có
                if (!item.note.isNullOrBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = Color(0xFF888888),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right Column: Status Badge góc trên phải & Action icons ở dưới góc phải
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Badge nằm ở góc trên bên phải, luôn canh lề phải cố định thẳng hàng với nút xóa
                StatusBadge(status = item.status)

                val hasActions = (onAddToBlacklist != null && !isBlacklisted) || onDelete != null
                if (hasActions) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        if (onAddToBlacklist != null && !isBlacklisted) {
                            IconButton(
                                onClick = { onAddToBlacklist() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = "Chặn số",
                                    tint = Color(0xFF888888),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (onDelete != null) {
                            IconButton(
                                onClick = { onDelete() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa liên hệ",
                                    tint = Color(0xFFEE0000),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
