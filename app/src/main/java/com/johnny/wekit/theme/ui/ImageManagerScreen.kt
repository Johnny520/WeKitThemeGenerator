@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.johnny.wekit.theme.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.johnny.wekit.theme.data.ImageSlot
import com.johnny.wekit.theme.util.DisplayName
import com.johnny.wekit.theme.util.ImageSlotTree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ImageManagerScreen(
    images: Map<String, Uri>,
    onSetImage: (String, Uri) -> Unit,
    onClearImage: (String) -> Unit,
    onBatchImport: (Map<String, Uri>) -> Unit
) {
    val context = LocalContext.current
    var pendingImageSlot by remember { mutableStateOf<String?>(null) }

    // Single image picker
    val singleImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            pendingImageSlot?.let { path ->
                onSetImage(path, it)
                pendingImageSlot = null
            }
        }
    }

    // Batch import: pick multiple images
    val batchImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val mapping = mutableMapOf<String, Uri>()
            val allSlots = ImageSlotTree.ALL_SLOTS

            uris.forEach { uri ->
                // Try to match by file name
                val fileName = getFileName(context, uri)
                if (fileName != null) {
                    // 精确匹配文件名（去扩展名）或完整文件名（带路径最后一段），
                    // 用 substringAfterLast 避免 endsWith 误匹配前缀（如 xxbackground.png 误匹配 background.png）
                    val nameWithoutExt = fileName.substringBeforeLast(".")
                    val matchingSlot = allSlots.find { slot ->
                        slot.displayName == nameWithoutExt ||
                        slot.path.substringAfterLast("/") == fileName
                    }
                    if (matchingSlot != null) {
                        mapping[matchingSlot.path] = uri
                    }
                }
            }

            onBatchImport(mapping)
        }
    }

    // Category groups
    val categoryGroups = remember { ImageSlotTree.groupByCategory() }
    val sortedCategories = remember {
        listOf("splash", "home", "chat", "plus", "settings")
            .filter { it in categoryGroups.keys }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("图片管理", style = MaterialTheme.typography.headlineMedium)
            Row {
                OutlinedButton(onClick = {
                    batchImageLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }) {
                    Text("批量导入")
                }
            }
        }

        // Image list by category
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            sortedCategories.forEach { category ->
                val slots = categoryGroups[category] ?: emptyList()
                CategorySection(
                    category = category,
                    slots = slots,
                    images = images,
                    onPickImage = { path ->
                        pendingImageSlot = path
                        singleImageLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onClearImage = onClearImage
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CategorySection(
    category: String,
    slots: List<ImageSlot>,
    images: Map<String, Uri>,
    onPickImage: (String) -> Unit,
    onClearImage: (String) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(category != "splash") }
    val replacedCount = slots.count { it.path in images }
    val categoryName = DisplayName.categoryName(context, category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column {
            // Category header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$replacedCount/${slots.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开"
                )
            }

            if (expanded) {
                // Sub-path groups
                val subPathGroups = slots.groupBy { slot ->
                    val parts = slot.path.split("/")
                    if (parts.size > 2) parts[1] else ""
                }

                subPathGroups.forEach { (subPath, subSlots) ->
                    if (subPath.isNotEmpty()) {
                        Text(
                            text = "▸ $subPath",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 2.dp)
                        )
                    }

                    subSlots.forEach { slot ->
                        val isReplaced = slot.path in images
                        val imageUri = images[slot.path]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onPickImage(slot.path) },
                                    onLongClick = {
                                        if (isReplaced) onClearImage(slot.path)
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail
                            if (imageUri != null) {
                                ThumbnailImage(
                                    uri = imageUri,
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Path: 显示中文（找不到则 fallback 原始 path）
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = DisplayName.imageSlotName(context, slot.path),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = slot.path,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Status indicator
                            if (isReplaced) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "已替换",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "未替换",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (slot != subSlots.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }

                    if (subPath != subPathGroups.keys.last()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThumbnailImage(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(null, uri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 4
                    }
                    value = BitmapFactory.decodeStream(input, null, options)
                }
            } catch (_: Exception) {
                value = null
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.clip(MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else null
            } else null
        }
    } catch (_: Exception) {
        null
    }
}
