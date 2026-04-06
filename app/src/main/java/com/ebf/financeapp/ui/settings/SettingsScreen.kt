package com.ebf.financeapp.ui.settings



import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ebf.financeapp.data.remote.ApiResult
import com.ebf.financeapp.util.CsvExporter

val SUPPORTED_CURRENCIES = listOf("INR", "USD", "EUR", "GBP", "JPY", "AED", "SGD")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onDarkModeChange: (Boolean) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state   by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val settings= state.settings

    var showNameDialog     by remember { mutableStateOf(false) }
    var showCurrencySheet  by remember { mutableStateOf(false) }
    var nameInput          by remember(settings.userName) { mutableStateOf(settings.userName) }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title   = { Text("Your Name") },
            text    = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateUserName(nameInput)
                    showNameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCurrencySheet) {
        ModalBottomSheet(onDismissRequest = { showCurrencySheet = false }) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)
            ) {
                Text("Select Currency", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                // Show exchange rates from mock API
                if (state.isLoadingRates) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    SUPPORTED_CURRENCIES.forEach { currency ->
                        val rate = state.exchangeRates[currency]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (currency == settings.selectedCurrency)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable {
                                    viewModel.updateCurrency(currency)
                                    showCurrencySheet = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                currency,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (currency == settings.selectedCurrency)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            if (rate != null) {
                                Text(
                                    "1 INR = ${"%.4f".format(rate)} $currency",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            if (currency == settings.selectedCurrency) {
                                Icon(Icons.Filled.Check, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Exchange rates via Finance API (mock)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }
        }
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Profile card ───────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = settings.userName.take(1).uppercase(),
                            fontSize     = 24.sp,
                            fontWeight   = FontWeight.Bold,
                            color        = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            settings.userName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Currency: ${settings.selectedCurrency}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { showNameDialog = true }) {
                        Icon(Icons.Filled.Edit, null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // ── Preferences ────────────────────────────────────────────────
            SettingsSection("Preferences") {
                SettingsSwitchRow(
                    icon   = Icons.Filled.DarkMode,
                    label  = "Dark mode",
                    subtitle = "Switch to dark theme",
                    checked = settings.isDarkMode,
                    onCheckedChange = {
                        viewModel.toggleDarkMode(it, onDarkModeChange)
                    }
                )
                SettingsClickRow(
                    icon    = Icons.Filled.CurrencyExchange,
                    label   = "Currency",
                    subtitle= settings.selectedCurrency,
                    onClick = { showCurrencySheet = true }
                )
            }

            // ── Security ────────────────────────────────────────────────────
            SettingsSection("Security") {
                SettingsSwitchRow(
                    icon    = Icons.Filled.Fingerprint,
                    label   = "Biometric lock",
                    subtitle= "Require fingerprint to open app",
                    checked = settings.isBiometricEnabled,
                    onCheckedChange = viewModel::toggleBiometric
                )
            }

            // ── Notifications ──────────────────────────────────────────────
            SettingsSection("Notifications") {
                SettingsSwitchRow(
                    icon    = Icons.Filled.Notifications,
                    label   = "Daily reminder",
                    subtitle= "Remind me to log expenses at 9 PM",
                    checked = settings.isDailyReminderOn,
                    onCheckedChange = viewModel::toggleDailyReminder
                )
            }

            // ── Data ───────────────────────────────────────────────────────
            SettingsSection("Data") {
                SettingsClickRow(
                    icon    = Icons.Filled.Download,
                    label   = "Export transactions",
                    subtitle= "Download as CSV file",
                    onClick = {
                        // Trigger export — handled in parent or via ViewModel
                    }
                )
            }

            // ── About ──────────────────────────────────────────────────────
            SettingsSection("About") {
                SettingsInfoRow("Version", "1.0.0")
                SettingsInfoRow("Built with", "Jetpack Compose + Room + Hilt")
                SettingsInfoRow("API", "Finance API v1 (mock)")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Reusable settings composables ────────────────────────────────────────────

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text  = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsClickRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Icon(Icons.Filled.ChevronRight, null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}