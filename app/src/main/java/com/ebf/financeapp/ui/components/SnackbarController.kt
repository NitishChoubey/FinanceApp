package com.ebf.financeapp.ui.components



import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object SnackbarController {
    fun show(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message     = message,
                actionLabel = actionLabel,
                duration    = SnackbarDuration.Short
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onAction?.invoke()
            }
        }
    }
}