package com.ebf.financeapp.data.preferences



import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserSettings(
    val userName: String       = "User",
    val selectedCurrency: String = "INR",
    val isDarkMode: Boolean    = false,
    val isBiometricEnabled: Boolean = false,
    val isDailyReminderOn: Boolean  = true,
    val reminderHour: Int      = 21,
    val reminderMinute: Int    = 0,
    val monthlyIncomeGoal: Double = 0.0
)

@Singleton
class UserPreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val USER_NAME           = stringPreferencesKey("user_name")
        val CURRENCY            = stringPreferencesKey("currency")
        val DARK_MODE           = booleanPreferencesKey("dark_mode")
        val BIOMETRIC           = booleanPreferencesKey("biometric_enabled")
        val DAILY_REMINDER      = booleanPreferencesKey("daily_reminder")
        val REMINDER_HOUR       = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE     = intPreferencesKey("reminder_minute")
        val MONTHLY_INCOME_GOAL = doublePreferencesKey("monthly_income_goal")
        val FIRST_LAUNCH        = booleanPreferencesKey("first_launch")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            UserSettings(
                userName           = prefs[Keys.USER_NAME]       ?: "User",
                selectedCurrency   = prefs[Keys.CURRENCY]        ?: "INR",
                isDarkMode         = prefs[Keys.DARK_MODE]       ?: false,
                isBiometricEnabled = prefs[Keys.BIOMETRIC]       ?: false,
                isDailyReminderOn  = prefs[Keys.DAILY_REMINDER]  ?: true,
                reminderHour       = prefs[Keys.REMINDER_HOUR]   ?: 21,
                reminderMinute     = prefs[Keys.REMINDER_MINUTE] ?: 0,
                monthlyIncomeGoal  = prefs[Keys.MONTHLY_INCOME_GOAL] ?: 0.0
            )
        }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.FIRST_LAUNCH] ?: true }

    suspend fun updateUserName(name: String) = context.dataStore.edit {
        it[Keys.USER_NAME] = name
    }

    suspend fun updateCurrency(currency: String) = context.dataStore.edit {
        it[Keys.CURRENCY] = currency
    }

    suspend fun setDarkMode(enabled: Boolean) = context.dataStore.edit {
        it[Keys.DARK_MODE] = enabled
    }

    suspend fun setBiometric(enabled: Boolean) = context.dataStore.edit {
        it[Keys.BIOMETRIC] = enabled
    }

    suspend fun setDailyReminder(enabled: Boolean) = context.dataStore.edit {
        it[Keys.DAILY_REMINDER] = enabled
    }

    suspend fun setReminderTime(hour: Int, minute: Int) = context.dataStore.edit {
        it[Keys.REMINDER_HOUR]   = hour
        it[Keys.REMINDER_MINUTE] = minute
    }

    suspend fun setMonthlyIncomeGoal(amount: Double) = context.dataStore.edit {
        it[Keys.MONTHLY_INCOME_GOAL] = amount
    }

    suspend fun markFirstLaunchDone() = context.dataStore.edit {
        it[Keys.FIRST_LAUNCH] = false
    }
}