package com.ncw6fg.nxhw18e.pdfreader.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.CommonTopBar
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.TxtPreviewViewModel

/**
 * create by colin
 * 2026/2/13
 */

@Composable
fun TxtPreviewScreen(
    fileName: String,
    path: String,
    goBack: () -> Unit,
) {
    val viewModel: TxtPreviewViewModel = viewModel()
    // 进入页面时触发加载
    LaunchedEffect(path) {
        viewModel.loadTxtFile(path)
    }

    val isLoading = viewModel.isLoading.collectAsStateWithLifecycle()
    val txtContent = viewModel.textContent.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            // 使用你项目中已有的 CommonTopBar
            CommonTopBar(title = fileName, goBack = goBack)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading.value) {
                CircularProgressIndicator()
            } else {
                SelectionContainer {
                    // 使用 verticalScroll 实现滚动
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = txtContent.value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

        }
    }
}