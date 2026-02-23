package com.ncw6fg.nxhw18e.pdfreader.ui.act

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_PATH
import com.ncw6fg.nxhw18e.pdfreader.ui.screen.ImagePreviewScreen
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme

/**
 * create by colin
 * 2026/2/13
 */
class ImagePreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent?.getStringExtra(EXTRA_PATH)
        val fileName = path?.substringAfterLast('/') ?: getString(R.string.image_preview)
        if (path.isNullOrEmpty()) {
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            PDFReaderTheme {
                ImagePreviewScreen(fileName, path) {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
}