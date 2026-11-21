package com.gmail.nthai.presentation.components.config

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmail.nthai.domain.model.BlacklistItem
import com.gmail.nthai.presentation.components.common.AutoSmsCardContainer
import com.gmail.nthai.presentation.components.common.AutoSmsSearchBar
import com.gmail.nthai.presentation.viewmodel.BlacklistViewModel
import com.gmail.nthai.util.DateTimeUtils

@Composable
fun BlacklistTab(
    viewModel: BlacklistViewModel = hiltViewModel()
) {
    val items by viewModel.blacklistItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddSingleDialog by remember { mutableStateOf(false) }
    var showAddBatchDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<BlacklistItem?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Header Stats
        AutoSmsCardContainer(
            title = "Danh sách đen (${items.size})"
        ) {
            Text(
                text = "Số điện thoại trong danh sách này sẽ tự động bị bỏ qua, không gửi SMS hay Zalo.",
                fontSize = 13.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AutoSmsSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                placeholder = "Tìm số điện thoại hoặc ghi chú...",
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showAddSingleDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF171717),
                        contentColor = Color(0xFFFFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Thêm số", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }

                OutlinedButton(
                    onClick = { showAddBatchDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF171717)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Nhập DS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }

                if (items.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearAllConfirm = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEE2E2))
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Xóa tất cả",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // List of Items
        if (items.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFEBEBEB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            tint = Color(0xFF888888),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "Danh sách đen đang trống" else "Không tìm thấy kết quả",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF171717)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "Thêm số điện thoại cần chặn vào danh sách đen để hệ thống tự động bỏ qua." else "Thử tìm kiếm với từ khóa khác.",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.id }) { item ->
                    BlacklistItemCard(
                        item = item,
                        onDelete = { itemToDelete = item }
                    )
                }
            }
        }
    }

    // Dialog: Add Single Number
    if (showAddSingleDialog) {
        var phoneInput by remember { mutableStateOf("") }
        var reasonInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSingleDialog = false },
            title = {
                Text(
                    "Thêm số vào Danh sách đen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF171717)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Số điện thoại *") },
                        placeholder = { Text("0912345678") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF171717),
                            focusedLabelColor = Color(0xFF171717)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text("Ghi chú / Lý do (không bắt buộc)") },
                        placeholder = { Text("Ví dụ: Khách yêu cầu không làm phiền") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF171717),
                            focusedLabelColor = Color(0xFF171717)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (phoneInput.isNotBlank()) {
                            viewModel.addPhone(phoneInput, reasonInput)
                            showAddSingleDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171717))
                ) {
                    Text("Thêm vào danh sách", color = Color(0xFFFFFFFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSingleDialog = false }) {
                    Text("Hủy", color = Color(0xFF666666))
                }
            }
        )
    }

    // Dialog: Add Batch Numbers
    if (showAddBatchDialog) {
        var batchInput by remember { mutableStateOf("") }
        var defaultReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddBatchDialog = false },
            title = {
                Text(
                    "Nhập danh sách số điện thoại",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF171717)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Dán danh sách số điện thoại (mỗi số 1 dòng hoặc cách nhau bằng dấu phẩy):",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )
                    OutlinedTextField(
                        value = batchInput,
                        onValueChange = { batchInput = it },
                        placeholder = { Text("0912345678\n0987654321\n0901112223") },
                        minLines = 4,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF171717),
                            focusedLabelColor = Color(0xFF171717)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = defaultReason,
                        onValueChange = { defaultReason = it },
                        label = { Text("Ghi chú chung (không bắt buộc)") },
                        placeholder = { Text("Nhập từ danh sách chặn") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF171717),
                            focusedLabelColor = Color(0xFF171717)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (batchInput.isNotBlank()) {
                            viewModel.addBatch(batchInput, defaultReason)
                            showAddBatchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171717))
                ) {
                    Text("Thêm danh sách", color = Color(0xFFFFFFFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBatchDialog = false }) {
                    Text("Hủy", color = Color(0xFF666666))
                }
            }
        )
    }

    // Dialog: Delete Single Item
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Xác nhận xóa khỏi Danh sách đen") },
            text = { Text("Bạn có chắc chắn muốn bỏ chặn số ${item.rawPhone}? Sau khi xóa, hệ thống sẽ tiếp tục gửi tin nhắn cho số này bình thường.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePhone(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Xóa bỏ", color = Color(0xFFFFFFFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Hủy", color = Color(0xFF666666))
                }
            }
        )
    }

    // Dialog: Clear All Confirm
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Xóa toàn bộ Danh sách đen?") },
            text = { Text("Hành động này sẽ xóa tất cả ${items.size} số điện thoại khỏi danh sách đen. Bạn có chắc chắn không?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAll()
                        showClearAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Xóa tất cả", color = Color(0xFFFFFFFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("Hủy", color = Color(0xFF666666))
                }
            }
        )
    }
}

@Composable
private fun BlacklistItemCard(
    item: BlacklistItem,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFFFF),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.rawPhone,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF171717)
                        )
                        if (item.phone != item.rawPhone && item.phone.isNotBlank()) {
                            Text(
                                text = "(${item.phone})",
                                fontSize = 12.sp,
                                color = Color(0xFF888888)
                            )
                        }
                    }

                    if (item.reason.isNotBlank()) {
                        Text(
                            text = item.reason,
                            fontSize = 13.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    Text(
                        text = "Thêm lúc: ${DateTimeUtils.formatDateTime(item.createdAt)}",
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = Color(0xFF888888)
                )
            }
        }
    }
}
