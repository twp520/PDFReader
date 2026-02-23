package com.ncw6fg.nxhw18e.pdfreader.ui.act

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_PATH
import com.ncw6fg.nxhw18e.pdfreader.ui.screen.PDFPreviewScreen
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * create by colin
 * 2026/2/11
 */
@AndroidEntryPoint
class PDFPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent?.getStringExtra(EXTRA_PATH)
        val fileName = path?.substringAfterLast('/') ?: getString(R.string.pdf_preview)
        if (path.isNullOrEmpty()) {
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            PDFReaderTheme {
                PDFPreviewScreen(fileName, path) {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
}