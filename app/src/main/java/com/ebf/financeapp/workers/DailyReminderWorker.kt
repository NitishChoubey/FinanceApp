package com.ebf.financeapp.workers



import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ebf.financeapp.MainActivity
import com.ebf.financeapp.R
import com.ebf.financeapp.data.repository.TransactionRepository
import com.ebf.financeapp.util.CurrencyFormatter
import com.ebf.financeapp.util.DateFormatter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepo: TransactionRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID   = "finance_reminders"
        const val CHANNEL_NAME = "Finance Reminders"
        const val NOTIF_ID     = 1001
        const val WORK_NAME    = "daily_reminder"

        fun schedule(context: Context, hour: Int = 21, minute: Int = 0) {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
            }
            if (target.before(now)) target.add(java.util.Calendar.DAY_OF_YEAR, 1)

            val delayMs = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        createChannel()

        val month   = DateFormatter.getCurrentMonth()
        val year    = DateFormatter.getCurrentYearString()
        val expense = transactionRepo.getMonthlyExpense(month, year).first()
        val income  = transactionRepo.getMonthlyIncome(month, year).first()

        val message = when {
            income > 0 && expense / income > 0.9 ->
                "You've spent ${CurrencyFormatter.format(expense)} this month — 90% of your income. Time to slow down! 🛑"
            income > 0 && expense / income > 0.7 ->
                "₹${CurrencyFormatter.format(expense)} spent so far. Don't forget to log today's expenses! 📊"
            else ->
                "Don't forget to log your expenses today. Tracking = saving! 💰"
        }

        showNotification(message)
        return Result.success()
    }

    private fun createChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Daily spending reminders"
                }
            )
        }
    }

    private fun showNotification(message: String) {
        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Finance Reminder")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(intent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(NOTIF_ID, notification)
    }
}