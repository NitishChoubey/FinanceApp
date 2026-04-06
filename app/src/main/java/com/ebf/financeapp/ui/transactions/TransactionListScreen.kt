package com.ebf.financeapp.ui.transactions



import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ebf.financeapp.ui.components.EmptyState
import com.ebf.financeapp.ui.components.FilterChip
import com.ebf.financeapp.ui.components.SnackbarController
import com.ebf.financeapp.ui.components.TransactionCard
import com.ebf.financeapp.ui.navigation.Routes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var lastDeleted by remember { mutableStateOf<com.ebf.financeapp.data.model.Transaction?>(null) }

    Scaffold(

        snackbarHost = { SnackbarHost(snackbarHostState) },

        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::setSearchQuery,
                            placeholder = { Text("Search transactions...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        LaunchedEffect(isSearchActive) {
                            if (isSearchActive) focusRequester.requestFocus()
                        }
                    } else {
                        Text("Transactions")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) viewModel.setSearchQuery("")
                    }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADD_TRANSACTION) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add transaction")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Filter chips ───────────────────────────────────────────────
            AnimatedVisibility(visible = !isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        label    = "All",
                        selected = state.activeFilter == TransactionFilter.ALL,
                        onClick  = { viewModel.setFilter(TransactionFilter.ALL) }
                    )
                    FilterChip(
                        label    = "Income",
                        selected = state.activeFilter == TransactionFilter.INCOME,
                        onClick  = { viewModel.setFilter(TransactionFilter.INCOME) }
                    )
                    FilterChip(
                        label    = "Expenses",
                        selected = state.activeFilter == TransactionFilter.EXPENSE,
                        onClick  = { viewModel.setFilter(TransactionFilter.EXPENSE) }
                    )
                }
            }

            // ── Content ────────────────────────────────────────────────────
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.isEmpty -> {
                    val (title, subtitle) = if (state.searchQuery.isNotBlank())
                        "No results found" to "Try a different search term"
                    else
                        "No transactions yet" to "Tap + to add your first transaction"

                    EmptyState(
                        icon = Icons.Filled.ReceiptLong,
                        title = title,
                        subtitle = subtitle
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 20.dp, end = 20.dp,
                            top = 4.dp, bottom = 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Group transactions by date label
                        val grouped = state.transactions.groupBy { twc ->
                            com.ebf.financeapp.util.DateFormatter
                                .formatRelative(twc.transaction.date)
                        }

                        grouped.forEach { (dateLabel, txList) ->
                            item {
                                Text(
                                    text = dateLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                            items(
                                items = txList,
                                key   = { it.transaction.id }
                            ) { item ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter   = fadeIn() + slideInVertically(),
                                ) {
                                    TransactionCard(
                                        item     = item,
                                        onEdit   = {
                                            navController.navigate(
                                                Routes.editTransaction(item.transaction.id)
                                            )
                                        },
                                        onDelete = {
                                            lastDeleted = item.transaction
                                            viewModel.deleteTransaction(item.transaction)
                                            SnackbarController.show(
                                                scope          = scope,
                                                snackbarHostState = snackbarHostState,
                                                message        = "Transaction deleted",
                                                actionLabel    = "Undo",
                                                onAction       = {
                                                    lastDeleted?.let { viewModel.addBack(it) }
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}