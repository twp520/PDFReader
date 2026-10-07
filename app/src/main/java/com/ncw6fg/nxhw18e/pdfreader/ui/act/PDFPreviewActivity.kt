package com.ncw6fg.nxhw18e.pdfreader.ui.act

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_PATH
import com.ncw6fg.nxhw18e.pdfreader.ui.screen.PDFPreviewScreen
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.PDFPreviewViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * create by colin
 * 2026/2/11
 */
@AndroidEntryPoint
class PDFPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 检查是否是从外部应用打开的PDF文件
        val viewModel by viewModels<PDFPreviewViewModel>()
        if (intent?.action == Intent.ACTION_VIEW && intent?.data != null) {
            val uri: Uri? = intent.data
            if (uri != null) {
                viewModel.loadFromUri(this, uri)
            }
        } else {
            // 内部调用，使用原来的方式获取路径
            val path = intent?.getStringExtra(EXTRA_PATH)
            val fileName = path?.substringAfterLast('/') ?: getString(R.string.pdf_preview)
            viewModel.loadFromPath(path, fileName)
        }
        setContent {
            PDFReaderTheme {
                PDFPreviewScreen(viewModel) {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
}