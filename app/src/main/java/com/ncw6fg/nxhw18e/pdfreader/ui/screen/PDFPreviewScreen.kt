package com.ncw6fg.nxhw18e.pdfreader.ui.screen

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.ncw6fg.nxhw18e.pdfreader.data.PdfPageModel
import com.ncw6fg.nxhw18e.pdfreader.money.AnalysisUtils
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.AdDialog
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.CommonTopBar
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.SimpleViewModel
import com.ymg.pdf.viewer.PDFView
import java.io.File

/**
 * create by colin
 * 2026/2/11
 */

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
fun PDFPreviewScreen(fileName: String, path: String, goback: () -> Unit) {
    var isFullScreen by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }
    val pageCount = remember { mutableIntStateOf(-1) }
    val pdfViewRef = remember { mutableStateOf<PDFView?>(null) }
    val isLoaded = remember { mutableStateOf(false) }
    val simpleViewModel = viewModel<SimpleViewModel>()
    val showADLoading = simpleViewModel.showAdLoading.collectAsStateWithLifecycle()
    val act = LocalActivity.current
    Scaffold(
        topBar = {
            CommonTopBar(
                title = fileName,
                goBack = {
                    act?.let {
                        simpleViewModel.showAD(
                            it,
                            AnalysisUtils.FROM_BACK_INTER,
                            finish = goback
                        )
                    }
                }
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AndroidView(
                factory = { context ->
                    PDFView(context, null).also {
                        pdfViewRef.value = it
                        it.fromFile(File(path))
                            .enableSwipe(true)
                            .swipeHorizontal(!isFullScreen) // 非全屏横向，全屏纵向
                            .defaultPage(currentPage)
                            .onPageChange { index, _ -> currentPage = index }
                            .onTap {
                                isFullScreen = !isFullScreen
                                true
                            }
                            .onLoad { count ->
                                pageCount.intValue = count
                                isLoaded.value = true
                            }
                            .load()
                    }
                },
                modifier = Modifier
                    .fillMaxSize(),
                update = { pdfView ->
                    Log.d("PDFPreviewScreen", "update: $pdfView")

                }
            )

            if (pageCount.intValue > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "${currentPage + 1} / ${pageCount.intValue}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            AnimatedVisibility(
                visible = !isFullScreen && pageCount.intValue > 0,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ThumbnailBar(
                    path,
                    pageCount.intValue,
                    currentPage
                ) {
                    pdfViewRef.value?.jumpTo(it, true)
                }
            }

            if (!isLoaded.value) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (showADLoading.value) {
            AdDialog()
        }
    }
}

@Composable
fun ThumbnailBar(
    filePath: String,
    pageCount: Int,
    currentIndex: Int,
    onThumbnailClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pageCount) { index ->
            val isSelected = index == currentIndex
            Card(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onThumbnailClick(index) }
            ) {
                AsyncImage(
                    model = PdfPageModel(
                        filePath,
                        index
                    ),
                    contentDescription = "Page $index",
                )
            }
        }
    }
}