package com.gmail.nthai.presentation.components.zalo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.presentation.components.common.CampaignProgressData
import com.gmail.nthai.presentation.components.common.GenericProgressLogsContent
import com.gmail.nthai.service.ZaloSendingProgressState

@Composable
fun ZaloProgressTab(
    progressState: ZaloSendingProgressState = ZaloSendingProgressState(),
    logs: List<LogRecord> = emptyList(),
    onPauseCampaign: () -> Unit = {},
    onResumeCampaign: () -> Unit = {},
    onStopCampaign: () -> Unit = {},
    onClearLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSending = progressState.isSending
    val isPaused = progressState.isPaused
    val sentCount = progressState.sentCount
    val totalCount = progressState.totalCount

    val statusText = when {
        !isSending -> progressState.statusMessage
        isPaused -> "Trạng thái: Tạm dừng"
        else -> progressState.statusMessage
    }

    val currentInfo = progressState.currentRecipient?.let { recipient ->
        if (recipient.name.isNotBlank()) "${recipient.name} (${recipient.phone})" else recipient.phone
    }

    val progressData = CampaignProgressData(
        title = "TRUNG TÂM GỬI ZALO",
        statusText = statusText,
        processedCount = sentCount,
        totalCount = totalCount,
        currentTargetInfo = currentInfo,
        countdownSeconds = progressState.countdownSeconds,
        isRunning = isSending,
        isPaused = isPaused
    )

    GenericProgressLogsContent(
        progressData = progressData,
        logs = logs,
        modifier = modifier,
        onPauseCampaign = onPauseCampaign,
        onResumeCampaign = onResumeCampaign,
        onStopCampaign = onStopCampaign,
        onClearLogs = onClearLogs,
        emptyLogMessage = "Chưa có nhật ký gửi Zalo nào."
    )
}
