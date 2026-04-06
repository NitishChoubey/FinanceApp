package com.ebf.financeapp.ui.transactions



import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ebf.financeapp.data.model.TransactionType
import com.ebf.financeapp.ui.components.getCategoryEmoji
import com.ebf.financeapp.util.DateFormatter

import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    navController: NavController,
    transactionId: Int? = null,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.addEditState.collectAsState()
    val context = LocalContext.current
    val isEditMode = transactionId != null

    // Load transaction for editing
    LaunchedEffect(transactionId) {
        if (transactionId != null) viewModel.loadTransactionForEdit(transactionId)
    }

    // Navigate back on save
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.resetAddEditState()
            navController.popBackStack()
        }
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Edit Transaction" else "Add Transaction")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetAddEditState()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = {
                            // Delete handled via swipe on list; kept for toolbar convenience
                        }) {
                            Icon(Icons.Filled.Delete, "Delete",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Income / Expense toggle ────────────────────────────────────
            TypeToggle(
                selectedType = state.selectedType,
                onTypeChange = viewModel::onTypeChange
            )

            // ── Amount ────────────────────────────────────────────────────
            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount") },
                leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = state.amountError != null,
                supportingText = state.amountError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // ── Title ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title") },
                leadingIcon = { Icon(Icons.Filled.Edit, null) },
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // ── Category picker ───────────────────────────────────────────
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (state.categoryError != null) {
                Text(
                    text = state.categoryError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            CategoryGrid(
                categories = state.categories,
                selectedId = state.selectedCategoryId,
                onSelect   = viewModel::onCategoryChange
            )

            // ── Date picker ───────────────────────────────────────────────
            DatePickerField(
                epochMillis = state.selectedDateMillis,
                onDateSelected = viewModel::onDateChange
            )

            // ── Note ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note (optional)") },
                leadingIcon = { Icon(Icons.Filled.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Spacer(Modifier.height(8.dp))

            // ── Save button ───────────────────────────────────────────────
            Button(
                onClick = { viewModel.saveTransaction(transactionId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) "Update" else "Save Transaction",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Income / Expense segmented toggle ───────────────────────────────────────

@Composable
private fun TypeToggle(
    selectedType: TransactionType,
    onTypeChange: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.Center
    ) {
        listOf(TransactionType.EXPENSE, TransactionType.INCOME).forEach { type ->
            val isSelected = selectedType == type
            val label      = if (type == TransactionType.INCOME) "Income" else "Expense"
            val tint       = if (type == TransactionType.INCOME) Color(0xFF1D9E75)
            else MaterialTheme.colorScheme.error

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onTypeChange(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (type == TransactionType.INCOME)
                            Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = if (isSelected) tint
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) tint
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// ─── Category grid ────────────────────────────────────────────────────────────

@Composable
private fun CategoryGrid(
    categories: List<com.ebf.financeapp.data.model.Category>,
    selectedId: Int,
    onSelect: (Int) -> Unit
) {
    val cols = 4
    val rows = (categories.size + cols - 1) / cols

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(cols) { col ->
                    val index = row * cols + col
                    if (index < categories.size) {
                        val cat = categories[index]
                        val isSelected = cat.id == selectedId
                        val catColor = Color(android.graphics.Color.parseColor(cat.colorHex))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) catColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) catColor else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelect(cat.id) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(getCategoryEmoji(cat.icon), style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = cat.name.split(" ")[0], // First word only
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ─── Date picker field ────────────────────────────────────────────────────────

@Composable
private fun DatePickerField(
    epochMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    val context = LocalContext.current

    OutlinedTextField(
        value = DateFormatter.format(epochMillis),
        onValueChange = {},
        readOnly = true,
        label = { Text("Date") },
        leadingIcon = { Icon(Icons.Filled.CalendarToday, null) },
        trailingIcon = {
            Icon(Icons.Filled.Edit, null,
                tint = MaterialTheme.colorScheme.primary)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val newCal = Calendar.getInstance().apply {
                            set(year, month, day, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateSelected(newCal.timeInMillis)
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
    )
}