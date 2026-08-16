package com.johnny.wekit.theme.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.johnny.wekit.theme.util.ThemeExporter
import com.johnny.wekit.theme.viewmodel.ThemeViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Theme : Screen("theme", "主题", Icons.Default.Info)
    data object Resources : Screen("resources", "资源", Icons.Default.Style)
    data object Export : Screen("export", "导出", Icons.Default.Download)
}

@Composable
fun ThemeNavigation(viewModel: ThemeViewModel = viewModel()) {
    val navController = rememberNavController()
    val project by viewModel.project.collectAsState()

    val screens = listOf(
        Screen.Theme,
        Screen.Resources,
        Screen.Export
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Theme.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Theme.route) {
                ThemeEditorScreen(
                    manifest = project.manifest,
                    onManifestUpdate = { viewModel.updateManifest(it) }
                )
            }
            composable(Screen.Resources.route) {
                ResourcesScreen(
                    project = project,
                    onColorUpdate = { key, value -> viewModel.updateColor(key, value) },
                    onResetColors = { viewModel.resetColors() },
                    onImportColors = { viewModel.importColors(it) },
                    onStringUpdate = { key, value -> viewModel.updateString(key, value) },
                    onResetStrings = { viewModel.resetStrings() },
                    onImportStrings = { viewModel.importStrings(it) },
                    onSetImage = { path, uri -> viewModel.setImage(path, uri) },
                    onClearImage = { path -> viewModel.clearImage(path) },
                    onBatchImportImages = { mapping -> viewModel.batchImportImages(mapping) }
                )
            }
            composable(Screen.Export.route) {
                ExportScreen(
                    project = project,
                    onExport = { context ->
                        ThemeExporter.export(
                            context = context,
                            manifest = project.manifest,
                            colors = project.colors,
                            strings = project.strings,
                            images = project.images
                        )
                    }
                )
            }
        }
    }
}
