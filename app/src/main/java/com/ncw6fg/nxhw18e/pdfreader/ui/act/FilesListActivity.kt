package com.ncw6fg.nxhw18e.pdfreader.ui.act

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_IS_ALL
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_IS_BOOKMARK
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_TITLE
import com.ncw6fg.nxhw18e.pdfreader.data.EXTRA_TYPE
import com.ncw6fg.nxhw18e.pdfreader.ui.bean.DocumentType
import com.ncw6fg.nxhw18e.pdfreader.ui.screen.FileListScreen
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.PDFReaderTheme
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.FileListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilesListActivity : ComponentActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val type = intent?.getSerializableExtra(EXTRA_TYPE)
        val isAll = intent?.getBooleanExtra(EXTRA_IS_ALL, false) ?: false
        val isBookmark = intent?.getBooleanExtra(EXTRA_IS_BOOKMARK, false) ?: false
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)
        val viewModel by viewModels<FileListViewModel>()
        if (type is DocumentType) {
            viewModel.refreshFilesFromType(isAll, isBookmark, type)
        } else {
            viewModel.refreshFilesFromType(isAll, isBookmark, null)
        }
        setContent {
            PDFReaderTheme {
                FileListScreen(title = title, viewModel = viewModel) {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
}

