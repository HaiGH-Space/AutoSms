package com.gmail.nthai.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class SmsTemplate(
    val id: Long = 0,
    val title: String = "Mẫu tin nhắn nhắc nợ",
    val content: String = "Kính gửi {Ten_KH}, hợp đồng {Ma_HD} số tiền {So_Tien} đ hạn {Ngay_Han}. {Xin vui lòng thanh toán|Vui lòng thanh toán sớm}.",
    val isSelected: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
