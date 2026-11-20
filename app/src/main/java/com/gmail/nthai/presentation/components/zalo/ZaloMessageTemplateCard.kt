package com.gmail.nthai.presentation.components.zalo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gmail.nthai.domain.model.SmsTemplate
import com.gmail.nthai.presentation.components.common.AutoSmsCardContainer

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ZaloMessageTemplateCard(
    templates: List<SmsTemplate>,
    selectedTemplateId: Long?,
    onSelectTemplate: (SmsTemplate) -> Unit,
    messageContent: String,
    onMessageContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AutoSmsCardContainer(
        modifier = modifier,
        title = "Nội dung tin nhắn Zalo",
        titleAction = {
            Text(
                text = "Hỗ trợ Spintax {A|B}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF888888),
                fontWeight = FontWeight.Bold
            )
        }
    ) {
        if (templates.isNotEmpty()) {
            var isDropdownExpanded by remember { mutableStateOf(false) }
            val selectedTemplate = templates.find { it.id == selectedTemplateId }

            Text(
                text = "Chọn mẫu tin từ mục Quản lý:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF171717)
            )
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTemplate?.title ?: "Chọn mẫu tin nhắn...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF171717),
                        unfocusedBorderColor = Color(0xFFEBEBEB),
                        focusedContainerColor = Color(0xFFFFFFFF),
                        unfocusedContainerColor = Color(0xFFFFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier.background(Color(0xFFFFFFFF))
                ) {
                    templates.forEach { templateItem ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = templateItem.title,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF171717)
                                        )
                                        if (templateItem.isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(Mặc định SMS)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF0070F3),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (templateItem.content.isNotBlank()) {
                                        Text(
                                            text = templateItem.content,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelectTemplate(templateItem)
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

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
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF171717)
                    )
                },
                border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFFFFFFF))
            )
            AssistChip(
                onClick = { onMessageContentChange(messageContent + " {sdt}") },
                label = { Text("{sdt}", color = Color(0xFF171717)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF171717)
                    )
                },
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
