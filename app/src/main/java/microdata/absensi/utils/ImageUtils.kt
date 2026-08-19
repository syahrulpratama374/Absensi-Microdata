package microdata.absensi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {

    fun loadSampledBitmap(context: Context, uri: Uri, maxDim: Int = 1024): Bitmap? {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxDim || bounds.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
    }

    fun encodeToBase64(context: Context, uri: Uri, maxDim: Int = 1024, quality: Int = 70): String {
        val bitmap = loadSampledBitmap(context, uri, maxDim) ?: return ""

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        bitmap.recycle()

        val encoded = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        return "data:image/jpeg;base64,$encoded"
    }
}