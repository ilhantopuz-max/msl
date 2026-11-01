package com.okuyazma.app.ui

import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Harfin yazılış animasyonu: harfin kontürü (çizgisi) kalemle çiziliyormuş gibi adım adım çizilir.
 * replay değeri değişince animasyon baştan oynar.
 * (Gerçek "çizgi sırası" için harf başına SVG yolu eklenebilir, bkz. README.)
 */
@Composable
fun LetterTracer(text: String, replay: Int, color: Color, modifier: Modifier = Modifier.size(150.dp)) {
    val progress = remember(text, replay) { Animatable(0f) }
    LaunchedEffect(text, replay) { progress.animateTo(1f, tween(2800, easing = LinearEasing)) }

    Canvas(modifier) {
        val paint = android.graphics.Paint().apply {
            textSize = size.height * 0.85f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val raw = android.graphics.Path()
        paint.getTextPath(text, 0, text.length, 0f, 0f, raw)
        val b = RectF()
        raw.computeBounds(b, true)
        raw.offset(size.width / 2 - b.centerX(), size.height / 2 - b.centerY())

        val sw = size.height * 0.05f
        drawPath(raw.asComposePath(), color.copy(alpha = 0.18f), style = Stroke(sw))

        val m = android.graphics.PathMeasure(raw, false)
        val lens = ArrayList<Float>()
        do { lens.add(m.length) } while (m.nextContour())
        var remaining = lens.sum() * progress.value

        val m2 = android.graphics.PathMeasure(raw, false)
        val seg = android.graphics.Path()
        for (l in lens) {
            if (remaining > 0f) {
                m2.getSegment(0f, minOf(l, remaining), seg, true)
                remaining -= l
            }
            m2.nextContour()
        }
        drawPath(seg.asComposePath(), color, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        if (progress.value >= 1f) drawPath(raw.asComposePath(), color.copy(alpha = 0.35f))
    }
}
