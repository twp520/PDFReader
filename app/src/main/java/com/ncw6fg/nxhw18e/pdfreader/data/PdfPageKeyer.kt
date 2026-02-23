package com.ncw6fg.nxhw18e.pdfreader.data

import coil3.key.Keyer
import coil3.request.Options

/**
 * create by colin
 * 2026/2/13
 */
class PdfPageKeyer : Keyer<PdfPageModel> {
    override fun key(
        data: PdfPageModel,
        options: Options
    ): String {
        return data.path + ":" + data.pageIndex
    }
}