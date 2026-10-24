package com.gmail.nthai.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gmail.nthai.domain.repository.CustomerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class DailySmsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val customerRepository: CustomerRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingCustomers = customerRepository.getPendingCustomers().first()
        if (pendingCustomers.isNotEmpty()) {
            val intent = Intent(context, SmsForegroundService::class.java).apply {
                action = SmsForegroundService.ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }
        return Result.success()
    }
}
