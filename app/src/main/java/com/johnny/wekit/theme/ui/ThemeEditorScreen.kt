package com.johnny.wekit.theme.ui

import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johnny.wekit.theme.BuildConfig
import com.johnny.wekit.theme.R
import com.johnny.wekit.theme.data.ThemeManifest

@Composable
fun ThemeEditorScreen(
    manifest: ThemeManifest,
    onManifestUpdate: (ThemeManifest) -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val author = stringResource(R.string.app_author)
    val githubUrl = stringResource(R.string.app_author_github)
    val description = stringResource(R.string.about_description)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ===== 顶部：APP 信息卡（作者 / 版本 / 项目地址）=====
        AboutHeaderCard(
            author = author,
            githubUrl = githubUrl,
            description = description,
            onGithubClick = {
                try {
                    uriHandler.openUri(githubUrl)
                } catch (e: Exception) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                    context.startActivity(intent)
                }
            }
        )

        // ===== 主题信息编辑 =====
        Text("主题信息", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = manifest.name,
            onValueChange = { onManifestUpdate(manifest.copy(name = it)) },
            label = { Text("主题名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例如：暗夜主题") }
        )

        OutlinedTextField(
            value = manifest.author,
            onValueChange = { onManifestUpdate(manifest.copy(author = it)) },
            label = { Text("作者") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例如：Johnny") }
        )

        OutlinedTextField(
            value = manifest.version,
            onValueChange = { onManifestUpdate(manifest.copy(version = it)) },
            label = { Text("版本") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例如：1.0") }
        )

        OutlinedTextField(
            value = manifest.description,
            onValueChange = { onManifestUpdate(manifest.copy(description = it)) },
            label = { Text("描述") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            placeholder = { Text("主题描述...") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 预览卡
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("预览", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("名称: ${manifest.name.ifBlank { "未命名" }}")
                Text("作者: ${manifest.author.ifBlank { "未填写" }}")
                Text("版本: ${manifest.version}")
                Text("描述: ${manifest.description.ifBlank { "无描述" }}")
            }
        }
    }
}

@Composable
private fun AboutHeaderCard(
    author: String,
    githubUrl: String,
    description: String,
    onGithubClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    AppIconSafe(modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            AboutInfoRow(
                icon = Icons.Default.Person,
                label = stringResource(R.string.about_author_label),
                value = author
            )
            AboutDivider()
            AboutInfoRow(
                icon = Icons.Default.Tag,
                label = stringResource(R.string.about_version_label),
                value = stringResource(R.string.app_version_format, BuildConfig.VERSION_NAME)
            )
            AboutDivider()
            AboutInfoRow(
                icon = Icons.Default.Code,
                label = stringResource(R.string.about_github_label),
                value = githubUrl,
                clickable = true,
                onClick = onGithubClick
            )
        }
    }
}

@Composable
private fun AboutInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    clickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    val rowModifier = if (clickable) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AboutDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

@Composable
private fun AppIconSafe(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember {
        try {
            val res: Resources = context.resources
            val mipmapId = res.getIdentifier("ic_launcher", "mipmap", context.packageName)
            val drawableId = res.getIdentifier("ic_launcher", "drawable", context.packageName)
            val targetId = when {
                mipmapId != 0 -> mipmapId
                drawableId != 0 -> drawableId
                else -> 0
            }
            if (targetId != 0) {
                res.openRawResource(targetId).use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } else {
                null
            }
        } catch (e: Throwable) {
            null
        }
    }

    when {
        bitmap != null -> {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.app_name),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        else -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "W",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        }
    }
}
