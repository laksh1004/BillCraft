package com.billcraft.app.presentation.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.billcraft.app.analytics.AnalyticsManager
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────
// Colour tokens  (mirror your app's theme or replace with MaterialTheme references)
// ─────────────────────────────────────────────────────────────────
private val GradientStart = Color(0xFF1565C0)   // Blue 800
private val GradientEnd   = Color(0xFF00897B)   // Teal 600
private val OnGradient    = Color.White
private val ButtonBg      = Color.White
private val ButtonText    = Color(0xFF1565C0)

// ─────────────────────────────────────────────────────────────────
// Feature data
// ─────────────────────────────────────────────────────────────────
private data class Feature(val emoji: String, val text: String)

private val features = listOf(
    Feature("📄", "GST-Ready Invoices"),
    Feature("📲", "Share via WhatsApp & Email"),
    Feature("💳", "Track Payments & UPI QR"),
)

/**
 * Full-screen welcome / onboarding screen for BillCraft.
 *
 * Analytics:
 *  - Fires [AnalyticsManager.logOnboardingStarted] once on first composition.
 *
 * Navigation:
 *  - [onGetStarted]  → navigate to Business Setup screen.
 *  - [onContinue]    → navigate directly to Dashboard (returning user).
 *
 * @param onGetStarted   Called when the primary CTA button is tapped.
 * @param onContinue     Called when the secondary "Already set up?" link is tapped.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onContinue: () -> Unit,
) {
    // ── Analytics ──────────────────────────────────────────────
    LaunchedEffect(Unit) {
        AnalyticsManager.logOnboardingStarted()
    }

    // ── Feature item reveal state ──────────────────────────────
    val featureVisible = remember { mutableStateListOf(false, false, false) }

    LaunchedEffect(Unit) {
        features.forEachIndexed { index, _ ->
            delay(600L + index * 350L)   // staggered fade-in
            featureVisible[index] = true
        }
    }

    // ── Root container with full-bleed gradient ────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            Spacer(Modifier.height(16.dp))

            // ── App icon (emoji-styled) ────────────────────────
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧾",
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── App name ──────────────────────────────────────
            Text(
                text = "BillCraft",
                color = OnGradient,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(8.dp))

            // ── Tagline ───────────────────────────────────────
            Text(
                text = "Smart Billing for Bharat 🇮🇳",
                color = OnGradient.copy(alpha = 0.88f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // ── Feature list (staggered animation) ───────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    features.forEachIndexed { index, feature ->
                        AnimatedVisibility(
                            visible = featureVisible.getOrElse(index) { false },
                            enter = fadeIn(animationSpec = tween(450)) +
                                    slideInVertically(
                                        animationSpec = tween(450),
                                        initialOffsetY = { it / 2 }
                                    )
                        ) {
                            FeatureRow(emoji = feature.emoji, text = feature.text)
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // ── Primary CTA ───────────────────────────────────
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBg,
                    contentColor = ButtonText
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Secondary link ────────────────────────────────
            TextButton(onClick = onContinue) {
                Text(
                    text = "Already set up? Continue →",
                    color = OnGradient.copy(alpha = 0.80f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────────────

@Composable
private fun FeatureRow(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Text(
            text = text,
            color = OnGradient,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
