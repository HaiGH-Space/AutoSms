package com.gmail.nthai.presentation.components.autocall

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gmail.nthai.domain.model.AutoCallProgress
import com.gmail.nthai.domain.model.AutoCallStatus
import com.gmail.nthai.domain.model.CallLogRecord
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.presentation.components.common.CampaignProgressData
import com.gmail.nthai.presentation.components.common.GenericProgressLogsContent

@Composable
fun CallProgressTab(
    progressState: AutoCallProgress = AutoCallProgress(),
    callLogs: List<CallLogRecord> = emptyList(),
    selectedSimSlot: Int = 1,
    modifier: Modifier = Modifier,
    onPauseCampaign: () -> Unit = {},
    onResumeCampaign: () -> Unit = {},
    onStopCampaign: () -> Unit = {},
    onClearCallLogs: () -> Unit = {}
) {
    val isRunning = progressState.status == AutoCallStatus.RUNNING
    val isPaused = progressState.status == AutoCallStatus.PAUSED
    val statusText = when (progressState.status) {
        AutoCallStatus.RUNNING -> if (progressState.isWaitingDelay) "Đang chờ đếm ngược..." else "Đang thực hiện cuộc gọi..."
        AutoCallStatus.PAUSED -> "Trạng thái: Tạm dừng"
        AutoCallStatus.FINISHED -> "Đã hoàn thành"
        AutoCallStatus.CANCELLED -> "Đã hủy"
        AutoCallStatus.IDLE -> "Sẵn sàng"
    }

    val currentInfo = progressState.currentCustomerName ?: progressState.currentPhone

    val progressData = CampaignProgressData(
        title = "TRUNG TÂM GỌI TỰ ĐỘNG",
        statusText = statusText,
        processedCount = progressState.completedCount,
        totalCount = progressState.totalCount,
        currentTargetInfo = currentInfo,
        countdownSeconds = if (progressState.isWaitingDelay) progressState.countdownRemainingSeconds else 0,
        isRunning = isRunning,
        isPaused = isPaused
    )

    val logs = callLogs.map { log ->
        LogRecord(
            id = log.id,
            customerId = log.customerId,
            customerName = log.customerName,
            phone = log.phone,
            messageContent = if (log.callDurationSeconds > 0) "Cuộc gọi (${log.callDurationSeconds}s)" else "Cuộc gọi tự động",
            simSlot = selectedSimSlot,
            timestamp = log.timestamp,
            status = if (log.status == "SUCCESS" || log.status == "CONNECTED") LogStatus.SENT_SUCCESS else LogStatus.FAILED,
            errorMessage = log.errorMessage
        )
    }

    GenericProgressLogsContent(
        progressData = progressData,
        logs = logs,
        modifier = modifier.fillMaxSize(),
        onPauseCampaign = onPauseCampaign,
        onResumeCampaign = onResumeCampaign,
        onStopCampaign = onStopCampaign,
        onClearLogs = onClearCallLogs,
        emptyLogMessage = "Chưa có nhật ký cuộc gọi nào."
    )
}
