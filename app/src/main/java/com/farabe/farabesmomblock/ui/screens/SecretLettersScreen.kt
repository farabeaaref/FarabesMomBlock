package com.farabe.farabesmomblock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.farabe.farabesmomblock.data.db.SecretLetterEntity
import com.farabe.farabesmomblock.data.repository.GameRepository
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.NavyBackground
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.WarmCream
import kotlinx.coroutines.launch

@Composable
fun SecretLettersScreen(
    repository: GameRepository,
    onBackClick: () -> Unit
) {
    val letters by repository.secretLetters.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var activeReadingLetter by remember { mutableStateOf<SecretLetterEntity?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SoftCardBg)
                    .border(1.dp, CardBorder, CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WarmCream,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "SECRET LETTERS ❤️",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = WarmCream
                )
                Text(
                    text = "Personal messages from Farabe",
                    fontSize = 12.sp,
                    color = RoseAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(letters) { letter ->
                LetterCard(
                    letter = letter,
                    onClick = {
                        if (letter.isUnlocked) {
                            coroutineScope.launch {
                                repository.markLetterRead(letter.id)
                            }
                            activeReadingLetter = letter
                            isEditing = false
                        }
                    }
                )
            }
        }
    }

    // Reading / Editing Modal Dialog
    activeReadingLetter?.let { letter ->
        Dialog(onDismissRequest = { activeReadingLetter = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkerGrid,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, CardBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isEditing) {
                        Text(
                            text = "Edit Letter",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmCream
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Letter Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editContent,
                            onValueChange = { editContent = it },
                            label = { Text("Letter Content") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            maxLines = 8
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isEditing = false },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftCardBg),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = WarmCream)
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.updateLetter(letter.id, editTitle, editContent)
                                        activeReadingLetter = letter.copy(title = editTitle, content = editContent)
                                        isEditing = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save", color = Color.White)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💌 Letter for Mom",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SoftCardBg)
                                    .clickable {
                                        editTitle = letter.title
                                        editContent = letter.content
                                        isEditing = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = WarmCream,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = letter.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmCream,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SoftCardBg)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = letter.content,
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                color = WarmCream.copy(alpha = 0.95f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { activeReadingLetter = null },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("I Love You Too, Farabe ❤️", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterCard(
    letter: SecretLetterEntity,
    onClick: () -> Unit
) {
    val alpha = if (letter.isUnlocked) 1.0f else 0.45f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkerGrid.copy(alpha = alpha))
            .border(
                1.dp,
                if (letter.isUnlocked) RoseAccent.copy(alpha = 0.6f) else CardBorder.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = letter.isUnlocked) { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (letter.isUnlocked) RoseAccent.copy(alpha = 0.2f) else SoftCardBg),
                contentAlignment = Alignment.Center
            ) {
                if (letter.isUnlocked) {
                    Icon(
                        imageVector = if (letter.isRead) Icons.Default.MarkEmailRead else Icons.Default.Mail,
                        contentDescription = null,
                        tint = RoseAccent,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = WarmCream.copy(alpha = 0.4f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (letter.isUnlocked) letter.title else "Secret Letter",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmCream.copy(alpha = alpha)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (letter.isUnlocked) "Tap to open and read ❤️" else "Unlock at: ${letter.milestoneLabel}",
                    fontSize = 12.sp,
                    color = if (letter.isUnlocked) GoldAccent else WarmCream.copy(alpha = 0.6f)
                )
            }
        }
    }
}
