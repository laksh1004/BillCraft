package com.billcraft.app.domain.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.billcraft.app.R
import com.billcraft.app.data.repository.InvoiceRepository
import com.billcraft.app.domain.usecase.MarkOverdueInvoicesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * [CoroutineWorker] that runs once per day to check for overdue invoices.
 *
 * When newly-overdue invoices are found it posts an Android notification
 * summarising them.  Uses [HiltWorker] for dependency injection.
 *
 * Schedule this worker on app start via [OverdueCheckWorker.enqueue].
 */
@HiltWorker
class OverdueCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val markOverdueInvoicesUseCase: MarkOverdueInvoicesUseCase,
    private val invoiceRepository: InvoiceRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val WORK_NAME = "BillCraft_OverdueCheck"
        const val NOTIFICATION_CHANNEL_ID = "billcraft_overdue"
        private const val NOTIFICATION_CHANNEL_NAME = "Overdue Invoices"
        private const val NOTIFICATION_ID_BASE = 9000

        /**
         * Schedules a unique periodic task that runs every 24 hours.
         * Call from [Application.onCreate] or from DI initialisation.
         */
        fun enqueue(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<OverdueCheckWorker>(1, TimeUnit.DAYS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            ensureNotificationChannel()

            // Mark overdue and get back their IDs
            val overdueIds = markOverdueInvoicesUseCase()

            if (overdueIds.isNotEmpty()) {
                postNotifications(overdueIds)
            }

            Result.success()
        } catch (e: Exception) {
            // Retry up to 3 times before giving up
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    // -------------------------------------------------------------------------
    // Notification helpers
    // -------------------------------------------------------------------------

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for invoices that have become overdue"
            }
            notificationManager().createNotificationChannel(channel)
        }
    }

    private suspend fun postNotifications(overdueIds: List<String>) {
        overdueIds.forEachIndexed { index, invoiceId ->
            val invoice = invoiceRepository.getInvoiceById(invoiceId).first()
                ?: return@forEachIndexed

            val customerName = invoice.customer?.displayName ?: "Customer"
            val amount = "₹%.2f".format(invoice.balanceDue)

            val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Invoice Overdue")
                .setContentText("${invoice.invoiceNumber} – $customerName owes $amount")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "Invoice ${invoice.invoiceNumber} for $customerName is overdue.\n" +
                            "Balance due: $amount"
                        )
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager().notify(NOTIFICATION_ID_BASE + index, notification)
        }
    }

    private fun notificationManager(): NotificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}
