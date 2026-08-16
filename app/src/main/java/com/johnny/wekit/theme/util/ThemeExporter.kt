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

            // colors.json
            val colorsJson = JSONObject()
            colors.forEach { (key, value) ->
                colorsJson.put(key, value)
            }
            addTextEntry(zos, "$themeName/colors.json", colorsJson.toString(2))

            // strings.json
            val stringsJson = JSONObject()
            strings.forEach { (key, value) ->
                stringsJson.put(key, value)
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
     * Save the zip file to the Downloads/wekit主题包/ directory.
     * @return The content URI of the saved file, or null on failure
     */
    fun saveToDownloads(context: Context, zipFile: File): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloadsMediaStore(context, zipFile)
        } else {
            saveToDownloadsLegacy(context, zipFile)
        }
    }

    /**
     * 返回主题包默认保存目录的展示路径。
     * API 29+ 相对路径为 Download/wekit主题包；legacy 为 /storage/emulated/0/Download/wekit主题包。
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
            val collection = MediaStore.Downloads.getContentUri("externalPrimary")
            val uri = context.contentResolver.insert(collection, values) ?: return null

            context.contentResolver.openOutputStream(uri)?.use { output ->
                FileInputStream(zipFile).use { input ->
                    input.copyTo(output)
                }
            }

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)

            uri
        } catch (e: Exception) {
            Log.e(TAG, "保存到 Download/wekit主题包 (MediaStore) 失败", e)
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
