package com.ebf.financeapp.ui.transactions



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebf.financeapp.data.model.Category
import com.ebf.financeapp.data.model.Transaction
import com.ebf.financeapp.data.model.TransactionType
import com.ebf.financeapp.data.model.TransactionWithCategory
import com.ebf.financeapp.data.repository.CategoryRepository
import com.ebf.financeapp.data.repository.TransactionRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Filter state ───────────────────────────────────────────────────────────

enum class TransactionFilter { ALL, INCOME, EXPENSE }

// ─── UI State ───────────────────────────────────────────────────────────────

data class TransactionListUiState(
    val transactions: List<TransactionWithCategory> = emptyList(),
    val activeFilter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val error: String? = null
)

data class AddEditUiState(
    // Form fields
    val amount: String = "",
    val title: String = "",
    val note: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategoryId: Int = -1,
    val selectedDateMillis: Long = System.currentTimeMillis(),
    // Supporting data
    val categories: List<Category> = emptyList(),
    // Validation
    val amountError: String? = null,
    val titleError: String? = null,
    val categoryError: String? = null,
    // State flags
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isEditMode: Boolean = false,
    val error: String? = null
)

// ─── ViewModel ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    // List screen state
    private val _listState = MutableStateFlow(TransactionListUiState())
    val listState: StateFlow<TransactionListUiState> = _listState.asStateFlow()

    // Add/Edit screen state
    private val _addEditState = MutableStateFlow(AddEditUiState())
    val addEditState: StateFlow<AddEditUiState> = _addEditState.asStateFlow()

    // Internal filter + search signals
    private val _activeFilter = MutableStateFlow(TransactionFilter.ALL)
    private val _searchQuery  = MutableStateFlow("")

    init {
        observeTransactions()
        loadCategories()
    }

    // ─── List screen ────────────────────────────────────────────────────────

    private fun observeTransactions() {
        viewModelScope.launch {
            combine(_activeFilter, _searchQuery) { filter, query -> filter to query }
                .flatMapLatest { (filter, query) ->
                    if (query.isNotBlank()) {
                        transactionRepo.searchTransactions(query)
                    } else {
                        when (filter) {
                            TransactionFilter.ALL     -> transactionRepo.getAllTransactions()
                            TransactionFilter.INCOME  -> transactionRepo.getTransactionsByType(TransactionType.INCOME)
                            TransactionFilter.EXPENSE -> transactionRepo.getTransactionsByType(TransactionType.EXPENSE)
                        }
                    }
                }
                .catch { e -> _listState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list ->
                    _listState.update {
                        it.copy(
                            transactions  = list,
                            isLoading     = false,
                            isEmpty       = list.isEmpty(),
                            activeFilter  = _activeFilter.value,
                            searchQuery   = _searchQuery.value
                        )
                    }
                }
        }
    }

    fun setFilter(filter: TransactionFilter) {
        _activeFilter.value = filter
        _listState.update { it.copy(activeFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _listState.update { it.copy(searchQuery = query) }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo.deleteTransaction(transaction)
        }
    }

    // ─── Add / Edit screen ──────────────────────────────────────────────────

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepo.getAllCategories()
                .collect { cats ->
                    _addEditState.update { it.copy(categories = cats) }
                }
        }
    }

    /** Call this when navigating to the edit screen with an existing transaction */
    fun loadTransactionForEdit(id: Int) {
        viewModelScope.launch {
            _addEditState.update { it.copy(isLoading = true) }
            val txWithCat = transactionRepo.getTransactionById(id)
            txWithCat?.let { twc ->
                _addEditState.update {
                    it.copy(
                        amount              = twc.transaction.amount.toString(),
                        title               = twc.transaction.title,
                        note                = twc.transaction.note,
                        selectedType        = twc.transaction.type,
                        selectedCategoryId  = twc.transaction.categoryId,
                        selectedDateMillis  = twc.transaction.date,
                        isEditMode          = true,
                        isLoading           = false
                    )
                }
            } ?: _addEditState.update { it.copy(isLoading = false, error = "Transaction not found") }
        }
    }

    // Form field updates
    fun onAmountChange(value: String)   = _addEditState.update { it.copy(amount = value, amountError = null) }
    fun onTitleChange(value: String)    = _addEditState.update { it.copy(title = value, titleError = null) }
    fun onNoteChange(value: String)     = _addEditState.update { it.copy(note = value) }
    fun onTypeChange(type: TransactionType) = _addEditState.update { it.copy(selectedType = type, selectedCategoryId = -1) }
    fun onCategoryChange(id: Int)       = _addEditState.update { it.copy(selectedCategoryId = id, categoryError = null) }
    fun onDateChange(millis: Long)      = _addEditState.update { it.copy(selectedDateMillis = millis) }

    fun saveTransaction(existingId: Int? = null) {
        if (!validateForm()) return

        viewModelScope.launch {
            _addEditState.update { it.copy(isLoading = true) }
            val state = _addEditState.value

            val transaction = Transaction(
                id         = existingId ?: 0,
                amount     = state.amount.toDouble(),
                type       = state.selectedType,
                categoryId = state.selectedCategoryId,
                title      = state.title.trim(),
                note       = state.note.trim(),
                date       = state.selectedDateMillis
            )

            if (existingId != null) {
                transactionRepo.updateTransaction(transaction)
            } else {
                transactionRepo.addTransaction(transaction)
            }

            _addEditState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    private fun validateForm(): Boolean {
        val state = _addEditState.value
        var isValid = true

        val amountDouble = state.amount.toDoubleOrNull()
        val amountError = when {
            state.amount.isBlank()        -> "Amount is required"
            amountDouble == null          -> "Enter a valid number"
            amountDouble <= 0             -> "Amount must be greater than 0"
            amountDouble > 99_99_999.0    -> "Amount too large"
            else                          -> null
        }
        val titleError = when {
            state.title.isBlank()         -> "Title is required"
            state.title.length < 2        -> "Title is too short"
            else                          -> null
        }
        val categoryError = if (state.selectedCategoryId == -1) "Please select a category" else null

        if (amountError != null || titleError != null || categoryError != null) {
            isValid = false
        }

        _addEditState.update {
            it.copy(
                amountError   = amountError,
                titleError    = titleError,
                categoryError = categoryError
            )
        }
        return isValid
    }

    fun resetAddEditState() {
        _addEditState.update {
            AddEditUiState(categories = it.categories) // preserve loaded categories
        }
    }

    fun clearError() {
        _addEditState.update { it.copy(error = null) }
        _listState.update { it.copy(error = null) }
    }

    fun addBack(transaction: com.ebf.financeapp.data.model.Transaction) {
        viewModelScope.launch {
            transactionRepo.addTransaction(transaction.copy(id = 0))
        }
    }
}