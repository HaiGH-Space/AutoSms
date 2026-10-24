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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    val tags = listOf("{Ten_KH}", "{So_Tien}", "{Ma_HD}", "{Ngay_Han}", "{SDT}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soạn Mẫu Tin nhắn") },
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
                value = template.content,
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("Nội dung tin nhắn (Hỗ trợ Spintax)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text("Ví dụ: Kính gửi {Ten_KH}, {xin vui lòng|vui lòng} thanh toán hợp đồng {Ma_HD}...") }
            )

            Text(
                text = "Chèn thẻ động thay thế:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    AssistChip(
                        onClick = { viewModel.insertTag(tag) },
                        label = { Text(tag) }
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        text = "Sử dụng cú pháp {Lựa chọn 1|Lựa chọn 2|Lựa chọn 3} để hệ thống chọn ngẫu nhiên 1 cụm từ mỗi lần gửi, giúp giảm nguy cơ bị chặn SPAM.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Card(
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
