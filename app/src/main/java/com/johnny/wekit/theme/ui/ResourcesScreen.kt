package com.johnny.wekit.theme.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.johnny.wekit.theme.data.ThemeProject

/**
 * 资源编辑页：集中颜色 / 字符串 / 图片 三个编辑区，顶部 Tab 切换。
 * 避免底栏塞太多入口，把同属"资源编辑"的内容合并到一处。
 */
@Composable
fun ResourcesScreen(
    project: ThemeProject,
    onColorUpdate: (String, String) -> Unit,
    onResetColors: () -> Unit,
    onImportColors: (Map<String, String>) -> Unit,
    onStringUpdate: (String, String) -> Unit,
    onResetStrings: () -> Unit,
    onImportStrings: (Map<String, String>) -> Unit,
    onSetImage: (String, Uri) -> Unit,
    onClearImage: (String) -> Unit,
    onBatchImportImages: (Map<String, Uri>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("颜色", "文字", "图片")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ColorsEditorScreen(
                colors = project.colors,
                onColorUpdate = onColorUpdate,
                onResetColors = onResetColors,
                onImportColors = onImportColors
            )
            1 -> StringsEditorScreen(
                strings = project.strings,
                onStringUpdate = onStringUpdate,
                onResetStrings = onResetStrings,
                onImportStrings = onImportStrings
            )
            2 -> ImageManagerScreen(
                images = project.images,
                onSetImage = onSetImage,
                onClearImage = onClearImage,
                onBatchImport = onBatchImportImages
            )
        }
    }
}
