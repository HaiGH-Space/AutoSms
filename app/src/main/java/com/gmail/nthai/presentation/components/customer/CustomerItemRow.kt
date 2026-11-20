package com.gmail.nthai.presentation.components.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.gmail.nthai.util.formatCustomFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.usecase.DetectVietnameseCarrierUseCase

@Composable
fun CustomerItemCard(
    customer: Customer,
    onToggleSelection: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (customer.isSelected) Color(0xFF171717) else Color(0xFFEBEBEB)
    val cardBackground = if (customer.isSelected) Color(0xFFFAFAFA) else Color(0xFFFFFFFF)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = cardBackground,
        border = BorderStroke(if (customer.isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = customer.isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF171717),
                    checkmarkColor = Color(0xFFFFFFFF),
                    uncheckedColor = Color(0xFF888888)
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val customerName = customer.customFields.values.firstOrNull { it.isNotBlank() }
                    if (customerName != null) {
                        Text(
                            text = customerName.trim(),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF171717),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Khách hàng",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF888888)
                        )
                    }
                    StatusBadge(customer.status)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = Color(0xFF888888)
                        )
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp
                            ),
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF171717),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    CarrierEstimateBadge(customer.phone)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CustomerDetailDialog(
    customer: Customer,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFFFFFF),
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = Color(0xFF171717))
                Text(
                    "Thông tin người nhận",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CustomerDetailRow("Số điện thoại", customer.phone)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Nhà mạng:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF888888)
                    )
                    CarrierEstimateBadge(customer.phone)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Trạng thái gửi:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF888888)
                    )
                    StatusBadge(customer.status)
                }

                if (customer.customFields.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Trường dữ liệu từ Excel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                    customer.customFields.forEach { (label, value) ->
                        CustomerDetailRow(label, formatCustomFieldValue(value).ifBlank { "—" })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = Color(0xFF171717), fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
fun CarrierEstimateBadge(phone: String) {
    val carrierName = remember(phone) { DetectVietnameseCarrierUseCase.detect(phone) } ?: "Không rõ"

    Surface(
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(9999.dp),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
    ) {
        Text(
            text = carrierName,
            color = Color(0xFF171717),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp
            ),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CustomerDetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF888888)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF171717)
        )
    }
}

@Composable
fun StatusBadge(status: CustomerStatus) {
    val (dotColor, text) = when (status) {
        CustomerStatus.PENDING -> Color(0xFFF5A623) to "Chờ gửi"
        CustomerStatus.SENT -> Color(0xFF10B981) to "Thành công"
        CustomerStatus.FAILED -> Color(0xFFEE0000) to "Lỗi"
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(9999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            text = text,
            color = Color(0xFF171717),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
