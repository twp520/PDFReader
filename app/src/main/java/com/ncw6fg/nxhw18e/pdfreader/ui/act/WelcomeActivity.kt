package com.ncw6fg.nxhw18e.pdfreader.ui.act

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ncw6fg.nxhw18e.pdfreader.ui.screen.WelcomeScreen
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * create by colin
 * 2026/2/13
 */
@AndroidEntryPoint
class WelcomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PDFReaderTheme {
                WelcomeScreen(
                    startToMain = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}