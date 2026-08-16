package com.johnny.wekit.theme.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.johnny.wekit.theme.data.ThemeColors
import com.johnny.wekit.theme.util.DisplayName
import org.json.JSONObject

@Composable
fun ColorsEditorScreen(
    colors: Map<String, String>,
    onColorUpdate: (String, String) -> Unit,
    onResetColors: () -> Unit,
    onImportColors: (Map<String, String>) -> Unit
) {
    val context = LocalContext.current
    var showColorPicker by remember { mutableStateOf<String?>(null) }
    val grouped = remember { ThemeColors.groupBySubCategory() }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
                json?.let { text ->
                    val obj = JSONObject(text)
                    val imported = mutableMapOf<String, String>()
                    obj.keys().forEach { key ->
                        imported[key] = obj.getString(key)
                    }
                    onImportColors(imported)
                }
            } catch (_: Exception) {
                // Ignore parse errors
            }
        }
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val json = JSONObject()
                colors.forEach { (key, value) ->
                    json.put(key, value)
                }
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(json.toString(2).toByteArray())
                }
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    // Color picker dialog
    showColorPicker?.let { colorKey ->
        ColorPickerDialog(
            initialColor = colors[colorKey] ?: "000000",
            onConfirm = { newValue ->
                onColorUpdate(colorKey, newValue)
                showColorPicker = null
            },
            onDismiss = {
                showColorPicker = null
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar：标题单独一行，操作按钮单独一行
        Text(
            "颜色编辑",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "操作")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("操作")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("重置") },
                        onClick = {
                            menuExpanded = false
                            onResetColors()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("导入") },
                        onClick = {
                            menuExpanded = false
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("导出") },
                        onClick = {
                            menuExpanded = false
                            exportLauncher.launch("colors.json")
                        }
                    )
                }
            }
        }

        // Color list grouped by sub-category
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            val sortedGroups = grouped.entries.sortedBy { (groupKey, _) ->
                when {
                    groupKey.startsWith("home") -> 0
                    groupKey.startsWith("chat") -> 1
                    groupKey.startsWith("settings") -> 2
                    else -> 3
                }
            }

            sortedGroups.forEach { (groupKey, keys) ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = DisplayName.colorGroupName(LocalContext.current, groupKey),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    keys.forEach { key ->
                        val colorValue = colors[key] ?: "000000"
                        val previewColor = try {
                            val parsed = parseHexColor(colorValue)
                            Color(parsed)
                        } catch (_: Exception) {
                            Color.Black
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showColorPicker = key }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color preview box
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(previewColor)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Key name：中文化（找不到时 fallback 原始 key）
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = DisplayName.colorKeyName(LocalContext.current, key),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Color value
                            Text(
                                text = colorValue.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (key != keys.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun parseHexColor(hex: String): Int {
    val clean = hex.removePrefix("#").trim()
    return when (clean.length) {
        6 -> "FF$clean".toLong(16).toInt()
        8 -> clean.toLong(16).toInt()
        else -> 0xFF000000.toInt()
    }
}
