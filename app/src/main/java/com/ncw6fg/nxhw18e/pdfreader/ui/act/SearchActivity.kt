package com.ncw6fg.nxhw18e.pdfreader.ui.act

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ncw6fg.nxhw18e.pdfreader.ui.screen.SearchScreen
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * create by colin
 * 2026/2/13
 */
@AndroidEntryPoint
class SearchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PDFReaderTheme {
                SearchScreen(back = { onBackPressedDispatcher.onBackPressed() })
            }
        }
    }
}