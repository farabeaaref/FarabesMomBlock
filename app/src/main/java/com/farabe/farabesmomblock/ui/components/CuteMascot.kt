package com.farabe.farabesmomblock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Renders the cheerful, cute cartoon companion mascot (matching the game UI reference).
 * A happy grey-and-white kitten wearing a loving red neckerchief to cheer Mom on!
 */
@Composable
fun CuteMascot(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.52f
        val r = size.minDimension * 0.40f

        val faceGrey = Color(0xFFD4D8DD)
        val faceWhite = Color(0xFFFFFFFF)
        val outlineBlack = Color(0xFF2B2523)
        val earInnerPink = Color(0xFFFFB6C1)
        val scarfRed = Color(0xFFE63946)
        val strokeW = (size.minDimension * 0.04f).coerceAtLeast(2f)

        // 1. Red scarf behind
        val scarf = Path().apply {
            moveTo(cx - r * 0.7f, cy + r * 0.75f)
            cubicTo(cx, cy + r * 1.15f, cx + r * 0.7f, cy + r * 0.75f, cx + r * 0.7f, cy + r * 0.75f)
            lineTo(cx + r * 0.85f, cy + r * 1.1f)
            lineTo(cx - r * 0.85f, cy + r * 1.1f)
            close()
        }
        drawPath(scarf, scarfRed)
        drawPath(scarf, outlineBlack, style = Stroke(strokeW))

        // 2. Ears
        // Left Ear
        val leftEar = Path().apply {
            moveTo(cx - r * 0.75f, cy - r * 0.3f)
            lineTo(cx - r * 0.85f, cy - r * 1.05f)
            lineTo(cx - r * 0.25f, cy - r * 0.75f)
            close()
        }
        drawPath(leftEar, faceGrey)
        drawPath(leftEar, outlineBlack, style = Stroke(strokeW))

        val leftEarInner = Path().apply {
            moveTo(cx - r * 0.7f, cy - r * 0.4f)
            lineTo(cx - r * 0.78f, cy - r * 0.95f)
            lineTo(cx - r * 0.35f, cy - r * 0.72f)
            close()
        }
        drawPath(leftEarInner, earInnerPink)

        // Right Ear
        val rightEar = Path().apply {
            moveTo(cx + r * 0.75f, cy - r * 0.3f)
            lineTo(cx + r * 0.85f, cy - r * 1.05f)
            lineTo(cx + r * 0.25f, cy - r * 0.75f)
            close()
        }
        drawPath(rightEar, faceGrey)
        drawPath(rightEar, outlineBlack, style = Stroke(strokeW))

        val rightEarInner = Path().apply {
            moveTo(cx + r * 0.7f, cy - r * 0.4f)
            lineTo(cx + r * 0.78f, cy - r * 0.95f)
            lineTo(cx + r * 0.35f, cy - r * 0.72f)
            close()
        }
        drawPath(rightEarInner, earInnerPink)

        // 3. Head (Round Grey Base)
        drawCircle(faceGrey, r * 0.88f, Offset(cx, cy))
        drawCircle(outlineBlack, r * 0.88f, Offset(cx, cy), style = Stroke(strokeW))

        // 4. Cheeks & Muzzle (White fluffy cheeks)
        drawCircle(faceWhite, r * 0.42f, Offset(cx - r * 0.36f, cy + r * 0.28f))
        drawCircle(faceWhite, r * 0.42f, Offset(cx + r * 0.36f, cy + r * 0.28f))
        drawCircle(faceWhite, r * 0.32f, Offset(cx, cy + r * 0.18f))

        // 5. Forehead hair tuft
        drawCircle(faceWhite, r * 0.38f, Offset(cx, cy - r * 0.5f))

        // 6. Big Happy Cartoon Eyes
        val eyeW = r * 0.26f
        val eyeH = r * 0.38f

        // Left Eye
        drawOval(faceWhite, Offset(cx - r * 0.32f - eyeW * 0.5f, cy - r * 0.32f), Size(eyeW, eyeH))
        drawOval(outlineBlack, Offset(cx - r * 0.32f - eyeW * 0.5f, cy - r * 0.32f), Size(eyeW, eyeH), style = Stroke(strokeW * 0.8f))
        // Left Pupil & Specular Shine
        drawOval(outlineBlack, Offset(cx - r * 0.30f - eyeW * 0.4f, cy - r * 0.22f), Size(eyeW * 0.7f, eyeH * 0.65f))
        drawCircle(faceWhite, eyeW * 0.22f, Offset(cx - r * 0.34f, cy - r * 0.16f))

        // Right Eye
        drawOval(faceWhite, Offset(cx + r * 0.32f - eyeW * 0.5f, cy - r * 0.32f), Size(eyeW, eyeH))
        drawOval(outlineBlack, Offset(cx + r * 0.32f - eyeW * 0.5f, cy - r * 0.32f), Size(eyeW, eyeH), style = Stroke(strokeW * 0.8f))
        // Right Pupil & Specular Shine
        drawOval(outlineBlack, Offset(cx + r * 0.30f - eyeW * 0.3f, cy - r * 0.22f), Size(eyeW * 0.7f, eyeH * 0.65f))
        drawCircle(faceWhite, eyeW * 0.22f, Offset(cx + r * 0.28f, cy - r * 0.16f))

        // 7. Shiny Black Nose
        drawOval(outlineBlack, Offset(cx - r * 0.14f, cy + r * 0.05f), Size(r * 0.28f, r * 0.20f))
        drawCircle(faceWhite, r * 0.06f, Offset(cx - r * 0.04f, cy + r * 0.08f))

        // 8. Cheerful Open Mouth
        val mouth = Path().apply {
            moveTo(cx - r * 0.32f, cy + r * 0.34f)
            cubicTo(cx - r * 0.15f, cy + r * 0.65f, cx + r * 0.15f, cy + r * 0.65f, cx + r * 0.32f, cy + r * 0.34f)
            cubicTo(cx + r * 0.1f, cy + r * 0.38f, cx - r * 0.1f, cy + r * 0.38f, cx - r * 0.32f, cy + r * 0.34f)
            close()
        }
        drawPath(mouth, outlineBlack)
        // Red tongue
        val tongue = Path().apply {
            moveTo(cx - r * 0.18f, cy + r * 0.45f)
            cubicTo(cx, cy + r * 0.65f, cx + r * 0.18f, cy + r * 0.45f, cx + r * 0.18f, cy + r * 0.45f)
            close()
        }
        drawPath(tongue, Color(0xFFFF5252))

        // 9. Cheek Blushes
        drawCircle(Color(0xFFFF9AA2).copy(alpha = 0.5f), r * 0.18f, Offset(cx - r * 0.56f, cy + r * 0.24f))
        drawCircle(Color(0xFFFF9AA2).copy(alpha = 0.5f), r * 0.18f, Offset(cx + r * 0.56f, cy + r * 0.24f))
    }
}
