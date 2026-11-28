package com.gmail.nthai.presentation.components.zalo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gmail.nthai.presentation.components.common.CampaignStatusBanner

@Composable
fun ZaloAccessibilityStatusCard(
    isAccessibilityEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    CampaignStatusBanner(
        isGranted = isAccessibilityEnabled,
        title = "Trợ năng Zalo: CHƯA BẬT",
        description = "Cần cấp quyền Trợ năng (Accessibility) để tự động tìm SĐT & gửi tin Zalo.",
        actionLabel = "Bật ngay",
        onActionClick = onOpenAccessibilitySettings,
        modifier = modifier
    )
}
