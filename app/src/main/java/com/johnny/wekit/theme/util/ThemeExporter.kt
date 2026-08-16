package com.johnny.wekit.theme.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.johnny.wekit.theme.data.ThemeManifest
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Exports a WeKit theme project as a zip file.
 */
object ThemeExporter {

    private const val TAG = "ThemeExporter"

    /** 主题包保存的目标文件夹名（位于 Download 目录下） */
    const val THEME_FOLDER_NAME = "wekit主题包"

    /**
     * 清洗主题名，防御 Zip-Slip 路径穿越 + 非法文件名字符。
     * 规则：
     * 1. 去除路径分隔符 / 和 \
     * 2. 去除 .. （防目录穿越）
     * 3. 去除 Windows/Android 非法文件名字符 : * ? " < > |
     * 4. 去除控制字符
     * 5. 限制长度（最大 64 字符）
     * 6. 空结果 fallback "UntitledTheme"
     */
    private fun sanitizeThemeName(raw: String): String {
        val cleaned = raw
            .replace("\\", "")
            .replace("/", "")
            .replace("..", "")
            .replace(":", "")
            .replace("*", "")
            .replace("?", "")
            .replace("\"", "")
            .replace("<", "")
            .replace(">", "")
            .replace("|", "")
            .replace(Regex("[\\x00-\\x1f]"), "")
            .trim()
        val limited = if (cleaned.length > 64) cleaned.substring(0, 64) else cleaned
        return limited.ifBlank { "UntitledTheme" }
    }

    /**
     * Export the theme project to a zip file.
     */
    fun export(
        context: Context,
        manifest: ThemeManifest,
        colors: Map<String, String>,
        strings: Map<String, String>,
        images: Map<String, Uri>
    ): File {
        val themeName = sanitizeThemeName(manifest.name)
        val zipFile = File(context.cacheDir, "$themeName.wekit.zip")

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // manifest.json
            val manifestJson = JSONObject().apply {
                put("name", manifest.name)
                put("author", manifest.author)
                put("version", manifest.version)
                put("description", manifest.description)
            }
            addTextEntry(zos, "$themeName/manifest.json", manifestJson.toString(2))

            // colors.json —— 只导出用户实际修改过的颜色。
            // 默认值 "000000"（黑色）不写入：WeKit 引擎对缺失的 key 会回退到微信原生颜色，
            // 若把 37 个 key 全写成黑色，会导致微信所有文字被强制变黑（"主题所有颜色默认黑色" bug）。
            val colorsJson = JSONObject()
            colors.forEach { (key, value) ->
                val clean = value.trim().removePrefix("#")
                if (clean.isNotEmpty() && clean != "000000" && clean != "FF000000") {
                    colorsJson.put(key, value)
                }
            }
            addTextEntry(zos, "$themeName/colors.json", colorsJson.toString(2))

            // strings.json —— 只导出非空字符串。
            // 空字符串（如 chat.input.hint 默认空）不写入，避免清空微信输入框提示文字。
            val stringsJson = JSONObject()
            strings.forEach { (key, value) ->
                if (value.isNotEmpty()) {
                    stringsJson.put(key, value)
                }
            }
            addTextEntry(zos, "$themeName/strings.json", stringsJson.toString(2))

            // Images - only include replaced ones
            images.forEach { (path, uri) ->
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        zos.putNextEntry(ZipEntry("$themeName/$path"))
                        input.copyTo(zos)
                        zos.closeEntry()
                    }
                } catch (e: Exception) {
                    // 单张图片读取失败只跳过，不中断整体导出；记录日志便于排查
                    Log.w(TAG, "导出图片失败，已跳过: $path", e)
                }
            }
        }

        return zipFile
    }

    /**
     * 保存结果：包含实际保存的 uri 和展示用目录路径。
     */
    data class SaveResult(
        val uri: Uri,
        val dirDisplay: String
    )

    /**
     * Save the zip file to Download/wekit主题包/ 目录。
     * 保存策略（多级降级）：
     * 1. API 29+：MediaStore（标准 Downloads 目录，无需权限）
     * 2. API 26-28：legacy 公共 Download 目录（需 WRITE_EXTERNAL_STORAGE 权限）
     * 3. 上述失败 → 应用专属外部目录 getExternalFilesDir（无需权限，必然可写）
     * @return 保存结果（含实际保存目录），失败返回 null
     */
    fun saveToDownloads(context: Context, zipFile: File): SaveResult? {
        val publicDir = "Download/$THEME_FOLDER_NAME"

        // 第一优先级：按系统版本选择公共 Download 目录写入方式
        val publicUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloadsMediaStore(context, zipFile)
        } else {
            saveToDownloadsLegacy(context, zipFile)
        }
        if (publicUri != null) {
            return SaveResult(publicUri, publicDir)
        }

        // 降级：应用专属外部目录（无需权限，必然可写）
        val appDirUri = saveToAppExternalDir(context, zipFile)
        if (appDirUri != null) {
            return SaveResult(
                appDirUri,
                "Android/data/${context.packageName}/files/Download/$THEME_FOLDER_NAME"
            )
        }

        return null
    }

    /**
     * 返回主题包默认保存目录的展示路径。
     */
    fun defaultSaveDirDisplay(): String {
        return "Download/$THEME_FOLDER_NAME"
    }

    private fun saveToDownloadsMediaStore(context: Context, zipFile: File): Uri? {
        return try {
            val fileName = zipFile.name
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/$THEME_FOLDER_NAME")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            // 用标准 EXTERNAL_CONTENT_URI，避免 getContentUri("externalPrimary") 在部分 ROM 上失败
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return null

            context.contentResolver.openOutputStream(uri)?.use { output ->
                FileInputStream(zipFile).use { input ->
                    input.copyTo(output)
                }
            } ?: run {
                // 无法打开输出流，回滚插入的记录
                context.contentResolver.delete(uri, null, null)
                return null
            }

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)

            uri
        } catch (e: Exception) {
            Log.e(TAG, "保存到 Download/wekit主题包 (MediaStore) 失败，将降级", e)
            null
        }
    }

    /**
     * 降级方案：保存到应用专属外部目录（无需任何权限）。
     * 路径：Android/data/<package>/files/Download/wekit主题包/
     */
    private fun saveToAppExternalDir(context: Context, zipFile: File): Uri? {
        return try {
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            val themeDir = File(baseDir, THEME_FOLDER_NAME)
            if (!themeDir.exists()) themeDir.mkdirs()
            val destFile = File(themeDir, zipFile.name)
            FileInputStream(zipFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile)
        } catch (e: Exception) {
            Log.e(TAG, "保存到应用专属目录失败", e)
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun saveToDownloadsLegacy(context: Context, zipFile: File): Uri? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val themeDir = File(downloadsDir, THEME_FOLDER_NAME)
            if (!themeDir.exists()) themeDir.mkdirs()
            val destFile = File(themeDir, zipFile.name)
            FileInputStream(zipFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile)
        } catch (e: Exception) {
            Log.e(TAG, "保存到 Download/wekit主题包 (legacy) 失败", e)
            null
        }
    }

    private fun addTextEntry(zos: ZipOutputStream, entryName: String, content: String) {
        zos.putNextEntry(ZipEntry(entryName))
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }
}
