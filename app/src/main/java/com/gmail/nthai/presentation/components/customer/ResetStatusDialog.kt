package com.gmail.nthai.presentation.components.customer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gmail.nthai.domain.model.ResetStatusScope
import com.gmail.nthai.presentation.components.common.GenericSelectionDialog
import com.gmail.nthai.presentation.components.common.SelectionOption

@Composable
fun ResetStatusDialog(
    onConfirm: (ResetStatusScope) -> Unit,
    onDismiss: () -> Unit
) {
    val options = remember {
        ResetStatusScope.entries.map { scope ->
            SelectionOption(
                value = scope,
                title = scope.label,
                description = when (scope) {
                    ResetStatusScope.ALL -> "Đặt lại toàn bộ danh sách về Chưa gửi (Xóa toàn bộ lịch sử gửi cũ)"
                    ResetStatusScope.FAILED_ONLY -> "Chỉ chuyển các tin nhắn LỖI về Chưa gửi để thử lại (Giữ nguyên lịch sử tin thành công)"
                    ResetStatusScope.SENT_ONLY -> "Chuyển các tin ĐÃ GỬI về Chưa gửi và cho phép gửi lại (Xóa lịch sử nhóm này)"
                    ResetStatusScope.SELECTED_ONLY -> "Đặt lại cho các khách hàng đang được tích chọn và cho phép gửi lại"
                }
            )
        }
    }

    GenericSelectionDialog(
        title = "Đặt lại trạng thái gửi tin",
        description = "Chọn phạm vi đặt lại trạng thái về 'Chưa gửi' để tiếp tục phát tin nhắn mà không cần xóa danh sách:",
        options = options,
        initialSelected = ResetStatusScope.ALL,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmText = "Xác nhận đặt lại"
    )
}
