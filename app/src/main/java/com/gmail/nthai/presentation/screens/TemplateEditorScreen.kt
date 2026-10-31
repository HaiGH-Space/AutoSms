package com.gmail.nthai.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.presentation.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TemplateEditorScreen(
    viewModel: TemplateViewModel = hiltViewModel(),
    showTopBar: Boolean = true
) {
    val context = LocalContext.current
    val allTemplates by viewModel.allTemplates.collectAsStateWithLifecycle()
    val template by viewModel.template.collectAsStateWithLifecycle()
    val previewText by viewModel.preview.collectAsStateWithLifecycle()
    val availableFields by viewModel.availableFields.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var contentFieldValue by remember {
        mutableStateOf(TextFieldValue(template.content))
    }

    LaunchedEffect(template.content) {
        if (contentFieldValue.text != template.content) {
            contentFieldValue = TextFieldValue(
                text = template.content,
                selection = TextRange(template.content.length)
            )
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Xóa mẫu tin") },
            text = { Text("Xóa mẫu tin nhắn \"${template.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteTemplate(template.id) {
                            Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    val contentBody: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Template Chips Selection Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "+ Tạo mới" Chip
                AssistChip(
                    onClick = {
                        viewModel.createNewTemplate()
                        Toast.makeText(context, "Mẫu tin mới", Toast.LENGTH_SHORT).show()
                    },
                    label = { Text("+ Tạo mới") }
                )

                allTemplates.forEach { item ->
                    val isCurrentSelected = item.id == template.id
                    FilterChip(
                        selected = isCurrentSelected,
                        onClick = { viewModel.selectTemplateToEdit(item) },
                        label = {
                            Text(
                                text = item.title.ifBlank { "Chưa đặt tên" },
                                fontWeight = if (isCurrentSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (item.isSelected) {
                            {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Đang chọn gửi SMS",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // 2. Status & Quick Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (template.isSelected) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = "✓ Đang dùng để gửi SMS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (template.id != 0L) {
                    TextButton(
                        onClick = {
                            viewModel.setSelectedTemplate(template.id) {
                                Toast.makeText(context, "Đã chọn làm mẫu gửi SMS!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dùng mẫu này gửi SMS", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Text(
                        text = "Mẫu mới (chưa lưu)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (template.id != 0L) {
                        IconButton(
                            onClick = {
                                viewModel.saveAsNewTemplate { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Tạo bản sao")
                        }
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Xóa mẫu",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // 3. Inputs
            OutlinedTextField(
                value = template.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Tên mẫu tin") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = contentFieldValue,
                onValueChange = {
                    contentFieldValue = it
                    viewModel.updateContent(it.text)
                },
                label = { Text("Nội dung tin nhắn") },
                supportingText = {
                    Text("Cú pháp {Từ 1|Từ 2} để tráo từ ngẫu nhiên (Spintax)")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Kính gửi {{Tên}}, vui lòng thanh toán hợp đồng {{Mã HD}}...") }
            )

            // 4. Excel Variables Chips
            if (availableFields.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Chèn nhanh biến Excel:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableFields.forEach { field ->
                            val variable = "{{$field}}"
                            AssistChip(
                                onClick = {
                                    val selection = contentFieldValue.selection
                                    val updatedText = buildString {
                                        append(contentFieldValue.text.substring(0, selection.start))
                                        append(variable)
                                        append(contentFieldValue.text.substring(selection.end))
                                    }
                                    val cursorPosition = selection.start + variable.length
                                    contentFieldValue = TextFieldValue(
                                        text = updatedText,
                                        selection = TextRange(cursorPosition)
                                    )
                                    viewModel.updateContent(updatedText)
                                },
                                label = { Text(variable) }
                            )
                        }
                    }
                }
            }

            // 5. Live Preview
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Xem thử nội dung gửi:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.generateRandomPreview() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Thử lại ngẫu nhiên")
                        }
                    }

                    Text(
                        text = previewText.ifBlank { "(Nhập nội dung mẫu tin để xem thử)" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Mẫu tin nhắn",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        Button(
                            onClick = {
                                viewModel.saveTemplate { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lưu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            contentBody(Modifier.padding(innerPadding))
        }
    } else {
        contentBody(Modifier)
    }
}
