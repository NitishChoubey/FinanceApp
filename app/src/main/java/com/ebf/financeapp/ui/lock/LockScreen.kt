package com.ebf.financeapp.ui.lock



import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    // Auto-trigger biometric on first composition
    LaunchedEffect(Unit) {
        val activity = context as? androidx.fragment.app.FragmentActivity ?: return@LaunchedEffect
        isAuthenticating = true
        BiometricHelper.showPrompt(
            activity  = activity,
            onSuccess = { onUnlocked() },
            onFailed  = {
                isAuthenticating = false
                errorMessage = "Authentication failed. Try again."
            },
            onError   = { err ->
                isAuthenticating = false
                errorMessage = err
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF185FA5), Color(0xFF1D9E75))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App logo area
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("₹", fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Finance",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "Your personal finance companion",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(16.dp))

            // Fingerprint button
            FilledTonalButton(
                onClick = {
                    val activity = context as? androidx.fragment.app.FragmentActivity
                        ?: return@FilledTonalButton
                    isAuthenticating = true
                    errorMessage = null
                    BiometricHelper.showPrompt(
                        activity  = activity,
                        onSuccess = { onUnlocked() },
                        onFailed  = {
                            isAuthenticating = false
                            errorMessage = "Authentication failed. Try again."
                        },
                        onError   = { err ->
                            isAuthenticating = false
                            errorMessage = err
                        }
                    )
                },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor   = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = "Authenticate",
                    modifier = Modifier.size(40.dp)
                )
            }

            AnimatedVisibility(visible = isAuthenticating) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp,
                    modifier = Modifier.size(28.dp))
            }

            errorMessage?.let { err ->
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}