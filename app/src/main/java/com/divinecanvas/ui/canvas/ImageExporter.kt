package com.divinecanvas.ui.canvas

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/** Saves and shares the rendered canvas. All file I/O runs off the main thread. */
object ImageExporter {

    private const val ALBUM = "DivineCanvas"
    private const val SHARE_DIR = "shared_images"
    private const val MIME_JPEG = "image/jpeg"

    /** Persist [bitmap] to the device gallery. Returns the content Uri or null. */
    suspend fun saveToGallery(context: Context, bitmap: Bitmap): Uri? =
        withContext(Dispatchers.IO) {
            val fileName = "DivineCanvas_${System.currentTimeMillis()}.jpg"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, MIME_JPEG)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext null
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                } ?: return@withContext null
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            } else {
                // Legacy path (API <= 28). Requires WRITE_EXTERNAL_STORAGE.
                @Suppress("DEPRECATION")
                val picturesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    ALBUM,
                ).apply { mkdirs() }
                val file = File(picturesDir, fileName)
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                // Make it visible to the gallery.
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, file.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, MIME_JPEG)
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }

    /** Write [bitmap] to a shareable cache file and return its FileProvider Uri. */
    suspend fun cacheForShare(context: Context, bitmap: Bitmap): Uri =
        withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
            val file = File(dir, "share_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }

    /** True if a WhatsApp package is installed and can receive the image. */
    fun isWhatsAppInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return listOf("com.whatsapp", "com.whatsapp.w4b").any { pkg ->
            runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess
        }
    }

    private fun baseShareIntent(uri: Uri) = Intent(Intent.ACTION_SEND).apply {
        type = MIME_JPEG
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /**
     * Share directly to WhatsApp when available, otherwise open the system chooser.
     * Returns true if a target was launched.
     */
    fun shareToWhatsApp(context: Context, uri: Uri): Boolean {
        val pkg = listOf("com.whatsapp", "com.whatsapp.w4b").firstOrNull { p ->
            runCatching { context.packageManager.getPackageInfo(p, 0) }.isSuccess
        }
        return if (pkg != null) {
            val intent = baseShareIntent(uri).setPackage(pkg)
            runCatching { context.startActivity(intent) }.isSuccess
        } else {
            shareGeneric(context, uri)
        }
    }

    /** Open the system share sheet. Returns true if launched. */
    fun shareGeneric(context: Context, uri: Uri): Boolean {
        val chooser = Intent.createChooser(baseShareIntent(uri), null)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return runCatching { context.startActivity(chooser) }.isSuccess
    }
}
