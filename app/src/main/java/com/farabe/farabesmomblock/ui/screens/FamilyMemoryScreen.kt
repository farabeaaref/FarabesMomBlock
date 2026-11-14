package com.farabe.farabesmomblock.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farabe.farabesmomblock.data.db.FamilyMemoryEntity
import com.farabe.farabesmomblock.data.repository.GameRepository
import com.farabe.farabesmomblock.ui.theme.CardBorder
import com.farabe.farabesmomblock.ui.theme.DarkerGrid
import com.farabe.farabesmomblock.ui.theme.GoldAccent
import com.farabe.farabesmomblock.ui.theme.NavyBackground
import com.farabe.farabesmomblock.ui.theme.RoseAccent
import com.farabe.farabesmomblock.ui.theme.SoftCardBg
import com.farabe.farabesmomblock.ui.theme.WarmCream
import com.farabe.farabesmomblock.util.BitmapUtils
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FamilyMemoryScreen(
    repository: GameRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val memoryEntity by repository.familyMemory.collectAsState(initial = FamilyMemoryEntity())
    val memory = memoryEntity ?: FamilyMemoryEntity()
    val coroutineScope = rememberCoroutineScope()

    val localPhotoFile = remember { File(context.filesDir, "mom_photo.jpg") }
    var photoBitmap by remember {
        mutableStateOf(
            if (localPhotoFile.exists()) {
                BitmapUtils.decodeBitmapFileWithExif(localPhotoFile)
            } else {
                // Check if pre-bundled image exists in assets/custom_images
                try {
                    val assetImages = context.assets.list("custom_images") ?: emptyArray()
                    val firstImage = assetImages.firstOrNull { it.endsWith(".jpg", true) || it.endsWith(".png", true) || it.endsWith(".webp", true) }
                    if (firstImage != null) {
                        BitmapUtils.decodeAssetWithExif(context, "custom_images/$firstImage")
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    localPhotoFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                photoBitmap = BitmapUtils.decodeBitmapFileWithExif(localPhotoFile)
                coroutineScope.launch {
                    repository.updateFamilyMemory(memory.title, memory.personalNote, localPhotoFile.absolutePath)
                }
            } catch (e: Exception) {
                // Ignore failure
            }
        }
    }

    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf("") }
    var editNote by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "FAMILY MEMORY",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = WarmCream
                    )
                    Text(
                        text = "Dedicated to Mom ❤️",
                        fontSize = 12.sp,
                        color = GoldAccent
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SoftCardBg)
                    .border(1.dp, CardBorder, CircleShape)
                    .clickable {
                        editTitle = memory.title
                        editNote = memory.personalNote
                        isEditing = !isEditing
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Memory",
                    tint = WarmCream,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isEditing) {
            // Edit Mode Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkerGrid)
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Edit Family Memory",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmCream
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Memory Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
                        label = { Text("Personal Note for Mom") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
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
                                    repository.updateFamilyMemory(editTitle, editNote, memory.photoUri)
                                    isEditing = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        } else {
            // Photo & Memory Presentation Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF261D4E), DarkerGrid)
                        )
                    )
                    .border(1.5.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Photo Frame (Displays Mom's Photo or Tap to Choose)
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(SoftCardBg)
                            .border(3.dp, RoseAccent, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoBitmap != null) {
                            Image(
                                bitmap = photoBitmap!!.asImageBitmap(),
                                contentDescription = "Mom's Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = RoseAccent,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap to Add\nMom's Photo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmCream,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (photoBitmap != null) "Tap photo above to change" else "From your phone gallery",
                        fontSize = 11.sp,
                        color = GoldAccent.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = memory.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmCream,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkerGrid)
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                            .padding(18.dp)
                    ) {
                        Text(
                            text = memory.personalNote,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = WarmCream.copy(alpha = 0.95f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "“Behind all your stories is always your mother's story,\nbecause hers is where yours begins.”",
                        fontSize = 12.sp,
                        color = GoldAccent.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
