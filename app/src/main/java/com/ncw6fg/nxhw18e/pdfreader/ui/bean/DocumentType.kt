package com.ncw6fg.nxhw18e.pdfreader.ui.bean

import com.ncw6fg.nxhw18e.pdfreader.R

/**
 * create by colin
 * 2026/2/2
 */
enum class DocumentType(
    val suffix: String,
    val icon: Int
) {
    WORD("doc,docx",R.drawable.icon_file_doc),
    EXCEL("xls,xlsx,cvs",R.drawable.icon_file_xls),
    PPT("ppt",R.drawable.icon_file_ppt),
    PDF("pdf",R.drawable.icon_file_pdf),
    TXT("txt",R.drawable.icon_file_txt),
    IMAGE("jpg,jpeg,png",R.drawable.icon_file_img),
}