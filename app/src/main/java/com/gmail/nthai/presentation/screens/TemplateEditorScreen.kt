package com.gmail.nthai.presentation.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gmail.nthai.presentation.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TemplateEditorScreen(
    viewModel: TemplateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val template by viewModel.template.collectAsStateWithLifecycle()
    val previewText by viewModel.preview.collectAsStateWithLifecycle()
    val availableFields by viewModel.availableFields.collectAsStateWithLifecycle()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Soạn mẫu tin nhắn",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.saveTemplate {
                                Toast.makeText(context, "Đã lưu mẫu tin nhắn!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Lưu")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = template.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Tiêu đề mẫu tin") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = contentFieldValue,
                onValueChange = {
                    contentFieldValue = it
                    viewModel.updateContent(it.text)
                },
                label = { Text("Nội dung tin nhắn (Hỗ trợ Spintax)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text("Ví dụ: Kính gửi {{Tên khách hàng}}, vui lòng thanh toán {{Mã hợp đồng}}...") }
            )

            Text(
                text = "Biến từ các cột Excel (nhấn để chèn):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (availableFields.isEmpty()) {
                Text(
                    text = "Nhập file Excel có hàng tiêu đề trước để hiển thị biến theo cột.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 Hướng dẫn cấu trúc Spintax:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dùng {{Tên cột}} để chèn dữ liệu từ Excel. Cú pháp {Lựa chọn 1|Lựa chọn 2} chọn ngẫu nhiên một cụm từ mỗi lần gửi.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Xem trước ngẫu nhiên:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.generateRandomPreview() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Thử lại ngẫu nhiên")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = previewText.ifBlank { "(Nhập nội dung mẫu tin ở trên để xem thử)" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
