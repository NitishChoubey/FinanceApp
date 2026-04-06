package com.ebf.financeapp



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ebf.financeapp.data.preferences.UserPreferencesStore
import com.ebf.financeapp.ui.lock.LockScreen
import com.ebf.financeapp.ui.navigation.AppNavigation
import com.ebf.financeapp.ui.theme.FinanceAppTheme
import com.ebf.financeapp.util.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {           // ← extend AppCompatActivity

    @Inject lateinit var prefs: UserPreferencesStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        val settings = runBlocking { prefs.userSettings.first() }
        CurrencyFormatter.setCurrency(settings.selectedCurrency)

        setContent {
            var isDarkMode by remember { mutableStateOf(settings.isDarkMode) }
            var isLocked   by remember { mutableStateOf(settings.isBiometricEnabled) }

            FinanceAppTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isLocked) {
                        LockScreen(onUnlocked = { isLocked = false })
                    } else {
                        AppNavigation(
                            onDarkModeChange = { isDarkMode = it }
                        )
                    }
                }
            }
        }
    }
}

