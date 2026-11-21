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
import androidx.compose.foundation.layout.width
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

import androidx.compose.material.icons.filled.Block

@Composable
fun CustomerItemCard(
    customer: Customer,
    onToggleSelection: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    isBlacklisted: Boolean = false,
    onAddToBlacklist: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val customerName = remember(customer.customFields) {
        val nameEntry = customer.customFields.entries.find { (k, _) ->
            k.contains("tên", ignoreCase = true) || k.contains("ten", ignoreCase = true) || k.contains("name", ignoreCase = true)
        }
        nameEntry?.value ?: customer.customFields.values.firstOrNull { it.isNotBlank() } ?: customer.phone
    }

    val status = when (customer.status) {
        CustomerStatus.PENDING -> com.gmail.nthai.domain.model.LogStatus.PENDING
        CustomerStatus.SENT -> com.gmail.nthai.domain.model.LogStatus.SENT_SUCCESS
        CustomerStatus.FAILED -> com.gmail.nthai.domain.model.LogStatus.FAILED
    }

    com.gmail.nthai.presentation.components.common.GenericCustomerItemCard(
        item = com.gmail.nthai.presentation.components.common.GenericCustomerItemData(
            id = customer.id,
            name = customerName,
            phone = customer.phone,
            status = status,
            isSelected = customer.isSelected
        ),
        onToggleSelection = onToggleSelection,
        onDelete = onDelete,
        onClick = onClick,
        isBlacklisted = isBlacklisted,
        onAddToBlacklist = onAddToBlacklist,
        modifier = modifier
    )
}

@Composable
fun CustomerDetailDialog(
    customer: Customer,
    isBlacklisted: Boolean = false,
    onAddToBlacklist: (() -> Unit)? = null,
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
                    if (isBlacklisted) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(9999.dp),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Text(
                                text = "Đã bị chặn (Blacklist)",
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        StatusBadge(customer.status)
                    }
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isBlacklisted && onAddToBlacklist != null) {
                    TextButton(
                        onClick = {
                            onAddToBlacklist()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm vào Blacklist", color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Đóng", color = Color(0xFF171717), fontWeight = FontWeight.SemiBold)
                }
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
