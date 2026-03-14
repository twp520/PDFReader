package com.ncw6fg.nxhw18e.pdfreader.ui.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ncw6fg.nxhw18e.pdfreader.money.AnalysisUtils
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.AdDialog
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.CommonTopBar
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.SimpleViewModel
import java.io.File

@Composable
fun ImagePreviewScreen(
    fileName: String,
    path: String,
    goBack: () -> Unit
) {
    val isFullScreen = remember { mutableStateOf(false) }
    val simpleViewModel = viewModel<SimpleViewModel>()
    val showAd = simpleViewModel.showAdLoading.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    Scaffold(
        containerColor = Color.Black, // 图片预览通常背景为黑
        topBar = {
            // 点击图片时隐藏/显示顶栏
            AnimatedVisibility(
                visible = !isFullScreen.value,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                // 这里使用你项目中已有的 CommonTopBar
                CommonTopBar(title = fileName, goBack = {
                    activity?.let {
                        simpleViewModel.showAD(
                            it,
                            AnalysisUtils.FROM_BACK_INTER,
                            finish = goBack
                        )
                    }
                })
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(true) {
                    isFullScreen.value = !isFullScreen.value
                }
                .background(Color.Black),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(path))
                    .crossfade(true)
                    .build(),
                contentDescription = fileName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        if (showAd.value) {
            AdDialog()
        }
    }
}