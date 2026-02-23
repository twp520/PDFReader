package com.ncw6fg.nxhw18e.pdfreader.ui.act

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ncw6fg.nxhw18e.pdfreader.ui.screen.CreatePdfScreen
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreatePdfActivity : ComponentActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PDFReaderTheme {
                CreatePdfScreen(onBackClick = {
                    onBackPressedDispatcher.onBackPressed()
                })
            }
        }
    }
}

