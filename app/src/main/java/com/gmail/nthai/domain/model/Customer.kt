package com.gmail.nthai.domain.model

import androidx.compose.runtime.Immutable

enum class CustomerStatus {
    PENDING,
    SENT,
    FAILED
}

enum class ResetStatusScope(val label: String) {
    ALL("Tất cả khách hàng"),
    FAILED_ONLY("Chỉ tin nhắn bị lỗi"),
    SENT_ONLY("Chỉ tin đã gửi thành công"),
    SELECTED_ONLY("Chỉ các khách hàng được chọn")
}

enum class ImportMode(val label: String, val description: String) {
    REPLACE_ALL("Thay thế toàn bộ", "Xóa danh sách hiện tại và nhập dữ liệu từ file Excel"),
    APPEND("Thêm nối tiếp", "Giữ dữ liệu cũ và chèn thêm danh sách mới"),
    SKIP_DUPLICATES("Bỏ qua trùng SĐT", "Chèn danh sách mới và tự động bỏ qua các SĐT đã có")
}

@Immutable
data class Customer(
    val id: Long = 0,
    val phone: String,
    val customFields: Map<String, String> = emptyMap(),
    val status: CustomerStatus = CustomerStatus.PENDING,
    val isSelected: Boolean = false,
    val sendSimSlot: Int? = null
)
