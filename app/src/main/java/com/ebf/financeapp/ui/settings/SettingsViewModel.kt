package com.ebf.financeapp.ui.settings



import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebf.financeapp.data.preferences.UserPreferencesStore
import com.ebf.financeapp.data.preferences.UserSettings
import com.ebf.financeapp.data.remote.ApiRepository
import com.ebf.financeapp.data.remote.ApiResult
import com.ebf.financeapp.util.CurrencyFormatter
import com.ebf.financeapp.workers.DailyReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val exchangeRates: Map<String, Double> = emptyMap(),
    val isLoadingRates: Boolean = false,
    val ratesError: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesStore,
    private val apiRepo: ApiRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.userSettings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
        fetchExchangeRates()
    }

    fun updateUserName(name: String) = viewModelScope.launch {
        prefs.updateUserName(name)
    }

    fun updateCurrency(currency: String) = viewModelScope.launch {
        prefs.updateCurrency(currency)
        CurrencyFormatter.setCurrency(currency)
    }

    fun toggleDarkMode(enabled: Boolean, onDarkModeChange: (Boolean) -> Unit) {
        viewModelScope.launch {
            prefs.setDarkMode(enabled)
            onDarkModeChange(enabled)
        }
    }

    fun toggleBiometric(enabled: Boolean) = viewModelScope.launch {
        prefs.setBiometric(enabled)
    }

    fun toggleDailyReminder(enabled: Boolean) = viewModelScope.launch {
        prefs.setDailyReminder(enabled)
        if (enabled) {
            DailyReminderWorker.schedule(context)
        } else {
            DailyReminderWorker.cancel(context)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) = viewModelScope.launch {
        prefs.setReminderTime(hour, minute)
        DailyReminderWorker.schedule(context, hour, minute)
    }

    private fun fetchExchangeRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRates = true) }
            when (val result = apiRepo.getExchangeRates()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        exchangeRates  = result.data.rates,
                        isLoadingRates = false
                    )
                }
                is ApiResult.Error   -> _uiState.update {
                    it.copy(isLoadingRates = false, ratesError = result.message)
                }
                else -> {}
            }
        }
    }
}