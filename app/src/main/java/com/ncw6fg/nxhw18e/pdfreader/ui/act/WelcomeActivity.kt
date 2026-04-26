package com.ncw6fg.nxhw18e.pdfreader.ui.act

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ncw6fg.nxhw18e.pdfreader.money.ActivityUtil
import com.ncw6fg.nxhw18e.pdfreader.money.AnalysisUtils
import com.ncw6fg.nxhw18e.pdfreader.ui.screen.WelcomeScreen
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.WelcomeViewModel
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
                WelcomeScreen()
            }
        }
        val simpleViewModel by viewModels<WelcomeViewModel>()
        simpleViewModel.showAD(
            this,
            AnalysisUtils.FROM_SPLASH_INTER,
            5000L
        ) {
            ActivityUtil.startToMain(this)
        }
    }
}