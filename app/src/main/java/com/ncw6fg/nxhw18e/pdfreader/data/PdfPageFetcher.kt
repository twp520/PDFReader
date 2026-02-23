package com.ncw6fg.nxhw18e.pdfreader.data

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import coil3.size.pxOrElse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * create by colin
 * 2026/2/13
 */
class PdfPageFetcher
    (
    private val model: PdfPageModel,
    private val options: Options
) : Fetcher {

    companion object {
        private val renderingLock = Mutex()
    }

    override suspend fun fetch(): FetchResult? = renderingLock.withLock {
        withContext(Dispatchers.IO) {
            runInterruptible {
                var pfd: ParcelFileDescriptor? = null
                var renderer: PdfRenderer? = null

                try {
                    val file = File(model.path)
                    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    renderer = PdfRenderer(pfd)
                    // 检查索引合法性
                    if (model.pageIndex >= renderer.pageCount) return@runInterruptible null
                    val page = renderer.openPage(model.pageIndex)
                    // 自动适配尺寸：从 options.size 获取 Coil 请求的宽高
                    val dstWidth = options.size.width.pxOrElse { 120 }
                    val dstHeight = options.size.height.pxOrElse { 160 }
                    // 使用 RGB_565 进一步节省内存 (不含透明度，PDF 渲染通常不需要)
                    val bitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_4444)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    ImageFetchResult(
                        image = bitmap.asImage(),
                        isSampled = true,
                        dataSource = DataSource.MEMORY
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    // 严格释放资源，防止内存泄漏和文件描述符溢出
                    try {
                        renderer?.close()
                        pfd?.close()
                    } catch (e: Exception) {
                        Log.w("PdfPageDecoder", "decode error : ${e.message}")
                    }
                }
            }
        }
    }

    class Factory : Fetcher.Factory<PdfPageModel> {

        override fun create(
            data: PdfPageModel,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return PdfPageFetcher(data, options)
        }

    }
}