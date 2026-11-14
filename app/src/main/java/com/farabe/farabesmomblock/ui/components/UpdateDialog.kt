package com.farabe.farabesmomblock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.WarmCream
import com.farabe.farabesmomblock.updater.UpdateInfo

/**
 * Loving update dialog for Mom with progress bar and one-tap install.
 */
@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    isDownloading: Boolean,
    downloadProgress: Float,
    onConfirmUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isDownloading) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, GoldAccent.copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
            color = Color(0xFF1E1428)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Loving Gift Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF758C), Color(0xFFFF7EB3))
                            )
                        )
                        .border(2.dp, GoldAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDownloading) Icons.Default.Download else Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = updateInfo.titleBengali.ifBlank { "আম্মু, নতুন সারপ্রাইজ এসেছে! ❤️" },
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmCream,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Version Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftCardBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Version ${updateInfo.versionName} • নতুন আপডেট",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = updateInfo.descriptionBengali.ifBlank {
                        "ফারাবী তোমার জন্য নতুন গান ও সুন্দর ফিচার যোগ করেছে। এখনই আপডেট করে নাও!"
                    },
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = WarmCream.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                if (updateInfo.releaseNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📝 ${updateInfo.releaseNotes}",
                        fontSize = 11.sp,
                        color = GoldAccent.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isDownloading) {
                    // Downloading State
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ডাউনলোড হচ্ছে... ${(downloadProgress * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = RoseAccent,
                            trackColor = DarkerGrid
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ডাউনলোড শেষে স্বয়ংক্রিয়ভাবে ইনস্টলার চালু হবে",
                            fontSize = 10.sp,
                            color = WarmCream.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = SoftCardBg),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("পরে (Later)", color = WarmCream, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onConfirmUpdate,
                            colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "এখনই আপডেট",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
