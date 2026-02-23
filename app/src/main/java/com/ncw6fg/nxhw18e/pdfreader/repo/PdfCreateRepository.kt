package com.ncw6fg.nxhw18e.pdfreader.repo

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import com.ncw6fg.nxhw18e.pdfreader.data.ImagePositionInfo
import com.ncw6fg.nxhw18e.pdfreader.data.util.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * create by colin
 * 2026/2/19
 */
class PdfCreateRepository @Inject constructor() {


    fun generateFilePath(context: Context, fileName: String): String {
        val timeStamp = System.currentTimeMillis()
        val fileFullName = "${fileName}_$timeStamp.pdf"
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        return File(directory, fileFullName).absolutePath
    }

    /**
     * 将图片列表转换为PDF文件
     */
    suspend fun createPdfFromImages(
        context: Context,
        imageItems: List<Uri>,
        outputPath: String,
        pageWidth: Float = 595f, // A4宽度 in points (210mm)
        pageHeight: Float = 842f, // A4高度 in points (297mm)
        onPageProgress: ((Int, Int) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val pdfDocument = PdfDocument()

            imageItems.forEachIndexed { index, imageItem ->
                // 更新进度
                onPageProgress?.invoke(index + 1, imageItems.size)

                val bitmap = ImageProcessor.loadAndCompressImage(
                    context = context,
                    uri = imageItem,
                    maxWidth = pageWidth * 2, // 提高分辨率
                    maxHeight = pageHeight * 2
                )

                if (bitmap != null) {
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        pageWidth.toInt(),
                        pageHeight.toInt(),
                        index + 1
                    ).create()

                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas

                    // 计算图片在页面中的最佳位置和大小
                    val positionInfo = calculateImagePosition(
                        bitmap.width,
                        bitmap.height,
                        pageWidth,
                        pageHeight
                    )

                    canvas.drawBitmap(
                        bitmap,
                        null,
                        positionInfo.rect,
                        null
                    )

                    pdfDocument.finishPage(page)
                    bitmap.recycle()
                }
            }
            val outputFile = File(outputPath)
            if (!outputFile.exists()) {
                outputFile.createNewFile()
            }
            val fileOutputStream = FileOutputStream(outputFile)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.close()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 计算图片在PDF页面中的位置和大小
     */
    private fun calculateImagePosition(
        imageWidth: Int,
        imageHeight: Int,
        pageWidth: Float,
        pageHeight: Float
    ): ImagePositionInfo {
        val imageAspectRatio = imageWidth.toFloat() / imageHeight
        val pageAspectRatio = pageWidth / pageHeight

        return if (imageAspectRatio > pageAspectRatio) {
            // 图片较宽，以宽度为准
            val scaledWidth = pageWidth
            val scaledHeight = pageWidth / imageAspectRatio
            val top = (pageHeight - scaledHeight) / 2f

            ImagePositionInfo(
                left = 0f,
                top = top,
                right = scaledWidth,
                bottom = top + scaledHeight
            )
        } else {
            // 图片较高，以高度为准
            val scaledHeight = pageHeight
            val scaledWidth = pageHeight * imageAspectRatio
            val left = (pageWidth - scaledWidth) / 2f

            ImagePositionInfo(
                left = left,
                top = 0f,
                right = left + scaledWidth,
                bottom = scaledHeight
            )
        }
    }
}