package com.johnny.wekit.theme.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.johnny.wekit.theme.data.ThemeStrings
import com.johnny.wekit.theme.util.DisplayName
import org.json.JSONObject

@Composable
fun StringsEditorScreen(
    strings: Map<String, String>,
    onStringUpdate: (String, String) -> Unit,
    onResetStrings: () -> Unit,
    onImportStrings: (Map<String, String>) -> Unit
) {
    val context = LocalContext.current

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
                    onImportStrings(imported)
                }
            } catch (_: Exception) {
                // Ignore
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
                strings.forEach { (key, value) ->
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top bar：标题单独一行，操作按钮单独一行
        Text("字符串编辑", style = MaterialTheme.typography.headlineMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
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
                            onResetStrings()
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
                            exportLauncher.launch("strings.json")
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // String fields
        ThemeStrings.ALL_KEYS.forEach { key ->
            val value = strings[key] ?: ""
            val displayName = DisplayName.stringKeyName(context, key)

            OutlinedTextField(
                value = value,
                onValueChange = { onStringUpdate(key, it) },
                label = { Text(displayName) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(key) },
                supportingText = {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}
