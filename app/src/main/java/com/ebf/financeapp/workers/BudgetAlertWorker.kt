package com.ebf.financeapp.workers



import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ebf.financeapp.data.repository.GoalRepository
import com.ebf.financeapp.data.repository.TransactionRepository
import com.ebf.financeapp.util.CurrencyFormatter
import com.ebf.financeapp.util.DateFormatter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val goalRepo: GoalRepository,
    private val transactionRepo: TransactionRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "budget_alert"

        fun scheduleImmediately(context: Context) {
            val request = OneTimeWorkRequestBuilder<BudgetAlertWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        val month  = DateFormatter.getCurrentMonth()
        val year   = DateFormatter.getCurrentYear()
        val yearStr= DateFormatter.getCurrentYearString()
        val goals  = goalRepo.getGoalsForMonth(month, year).first()

        goals.forEach { gwc ->
            val spent = transactionRepo
                .getSpentForCategoryInMonth(gwc.goal.categoryId, month, yearStr).first()
            val pct   = if (gwc.goal.budgetLimit > 0)
                spent / gwc.goal.budgetLimit else 0.0

            when {
                pct >= 1.0  -> sendAlert(
                    title   = "Budget Exceeded! 🚨",
                    message = "${gwc.category.name} is over budget by ${
                        CurrencyFormatter.format(spent - gwc.goal.budgetLimit)
                    }",
                    notifId = gwc.goal.id + 2000
                )
                pct >= 0.8  -> sendAlert(
                    title   = "Budget Warning ⚠️",
                    message = "${gwc.category.name} is at ${(pct * 100).toInt()}% of budget",
                    notifId = gwc.goal.id + 3000
                )
            }
        }
        return Result.success()
    }

    private fun sendAlert(title: String, message: String, notifId: Int) {
        val notification = NotificationCompat.Builder(context, DailyReminderWorker.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(notifId, notification)
    }
}