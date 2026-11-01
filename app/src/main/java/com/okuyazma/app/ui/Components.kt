package com.okuyazma.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KidButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    color: Color = Pastel.Pink, enabled: Boolean = true,
) {
    Button(
        onClick = onClick, modifier = modifier.heightIn(min = 60.dp), enabled = enabled,
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color, contentColor = Pastel.Ink,
            disabledContainerColor = color.copy(alpha = 0.5f), disabledContentColor = Pastel.Ink,
        ),
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        KidButton("⬅", onBack, color = Pastel.Lavender)
        Text(
            title, fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
        )
        trailing()
    }
}

/** Yıldızlar yükselir + "Aferin!" yazısı. Dokunmaları engellemez. */
@Composable
fun RewardOverlay() {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(1400, easing = LinearEasing)) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        repeat(14) { i ->
            val fx = 0.02f + i * 0.07f
            val rise = 0.35f + (i % 4) * 0.12f
            Text(
                if (i % 3 == 0) "🌟" else "⭐", fontSize = (28 + (i % 3) * 10).sp,
                modifier = Modifier
                    .offset(x = w * fx, y = h * (0.95f - rise * t.value))
                    .alpha((1f - t.value * t.value).coerceIn(0f, 1f)),
            )
        }
        Text(
            "Aferin!", fontSize = 56.sp, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Center).alpha((1f - t.value * 0.6f)),
        )
    }
}
