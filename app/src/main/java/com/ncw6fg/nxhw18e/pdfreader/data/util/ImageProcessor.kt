package com.ncw6fg.nxhw18e.pdfreader.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream

/**
 * create by colin
 * 2026/2/19
 */

object ImageProcessor {

    /**
     * 加载并压缩图片
     */
    fun loadAndCompressImage(
        context: Context,
        uri: Uri,
        maxWidth: Float,
        maxHeight: Float? = null,
    ): Bitmap? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            // 第一次解码获取原始尺寸
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // 计算采样率
            val sampleSize = calculateSampleSize(
                options.outWidth,
                options.outHeight,
                maxWidth.toInt(),
                maxHeight?.toInt() ?: maxWidth.toInt()
            )

            // 第二次解码，这次真正加载图片
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val newInputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            val bitmap = BitmapFactory.decodeStream(newInputStream, null, decodeOptions)
            newInputStream.close()

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}