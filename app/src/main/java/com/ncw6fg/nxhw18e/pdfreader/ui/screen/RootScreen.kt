package com.ncw6fg.nxhw18e.pdfreader.ui.screen

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.data.findActivity
import com.ncw6fg.nxhw18e.pdfreader.money.AnalysisUtils
import com.ncw6fg.nxhw18e.pdfreader.money.DisplaySmallNativeAdView
import com.ncw6fg.nxhw18e.pdfreader.ui.act.CreatePdfActivity
import com.ncw6fg.nxhw18e.pdfreader.ui.act.SearchActivity
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.AdDialog
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.CommonSpace
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.ShimmerButton
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.colorMainGridAll
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.colorMainGridBookMark
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.colorMainGridCreate
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.fillMax
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.MainViewModel

/**
 * create by colin
 * 2026/2/2
 */

@ExperimentalMaterial3Api
@Composable
fun RootScreen(viewModel: MainViewModel) {
    val loadingState = viewModel.isLoading.collectAsStateWithLifecycle()
    val isPermissionGranted = viewModel.isPermissionGranted.collectAsStateWithLifecycle()
    val isShowPermission = viewModel.showPermission.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = {
            // 返回 false 表示拦截所有滑动关闭手势，使用户无法通过下滑关闭
            viewModel.isRunB()
        },
        skipPartiallyExpanded = true // 直接全展开
    )
    val context = LocalContext.current
    val nativeAd = viewModel.nativeAd.collectAsStateWithLifecycle()
    Scaffold(
        modifier = fillMax,
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(R.drawable.name_logo),
                        contentDescription = stringResource(R.string.app_name)
                    )
                },
                actions = {
                    IconButton(content = {
                        Icon(
                            imageVector = Icons.Default.Search, contentDescription = "search"
                        )
                    }, onClick = {
                        context.startActivity(Intent(context, SearchActivity::class.java))
                    })
                }
            )
        },
        content = {
            Box(
                modifier = fillMax
                    .padding(it)
            ) {
                Column(fillMax.padding(16.dp)) {
                    Row {
                        Text(
                            stringResource(R.string.discover_your_files),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        CommonSpace(width = 8.dp)
                        if (loadingState.value) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.CenterVertically),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    CommonSpace(height = 16.dp)
                    DocumentGrid(viewModel)
                    Spacer(Modifier.weight(1f))
                    nativeAd.value?.let { ad ->
                        DisplaySmallNativeAdView(ad)
                    }
                }
                if (isShowPermission.value || (!isPermissionGranted.value && !viewModel.isRunB())) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            viewModel.dismissPermission()
                        },
                        sheetState = sheetState,
                        dragHandle = if (viewModel.isRunB()) {
                            { BottomSheetDefaults.DragHandle() }
                        } else null,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        // 放入权限布局
                        Box(modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)) {
                            PermissionLayout(viewModel)
                        }
                    }
                }
            }
        })
}

@Composable
private fun DocumentGrid(viewModel: MainViewModel) {
    val gridItems = viewModel.gridItems.collectAsStateWithLifecycle()
    val allFiles = viewModel.allFiles.collectAsStateWithLifecycle()
    val bookmarkFiles = viewModel.bookmarkFiles.collectAsStateWithLifecycle()
    val showAdLoading = viewModel.showAdLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = {
            item {
                MainGridItemCompose(
                    colorMainGridAll,
                    R.drawable.icon_file_all,
                    R.string.main_item_all,
                    allFiles.value.size,
                    onClick = {
                        context.findActivity()?.let {
                            viewModel.onGridAllClicked(it)
                        }
                    }
                )
            }
            items(gridItems.value) {
                MainGridItemCompose(
                    it.bgColor,
                    it.icon,
                    it.title,
                    it.count,
                    onClick = {
                        context.findActivity()?.let { act ->
                            viewModel.onGridItemClicked(
                                act, it.type,
                                act.getString(it.title)
                            )
                        }
                    }
                )
            }
            item {
                MainGridItemCompose(
                    colorMainGridBookMark,
                    R.drawable.icon_file_book,
                    R.string.main_item_bookmark,
                    bookmarkFiles.value.size,
                    onClick = {
                        context.findActivity()?.let {
                            viewModel.onGridBookmarkClicked(it)
                        }
                    }
                )
            }
            item {
                MainGridItemCompose(
                    colorMainGridCreate,
                    R.drawable.icon_file_create,
                    R.string.main_item_create,
                    0,
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                CreatePdfActivity::class.java
                            )
                        )
                    }
                )
            }
        })

    if (showAdLoading.value) {
        AdDialog()
    }
    LaunchedEffect(Unit) {
        AnalysisUtils.logEvent(AnalysisUtils.SCREEN_SHOW_MAIN)
    }
}

@Composable
private fun MainGridItemCompose(
    bgColor: Color,
    icon: Int,
    title: Int,
    count: Int,
    onClick: () -> Unit = {}
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = bgColor.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, bgColor.copy(alpha = 0.2f)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = Modifier.size(50.dp, 60.dp),
                painter = painterResource(icon),
                contentDescription = ""
            )
            CommonSpace()
            Text(
                stringResource(title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            CommonSpace()
            Text(
                stringResource(
                    R.string.main_file_counts,
                    count
                ),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun PermissionLayout(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isDeclined = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        AnalysisUtils.logEvent(AnalysisUtils.PERMISSION_SHOW)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp), // 增加边距
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isDeclined.value) {
            Image(
                modifier = Modifier.size(160.dp), // 略微缩小图标以适应 Sheet
                painter = painterResource(R.drawable.icon_permission_declined),
                contentDescription = "permission_declined"
            )
            CommonSpace(height = 20.dp)
            Text(
                stringResource(R.string.no_permission),
                style = MaterialTheme.typography.titleLarge
            )
            CommonSpace(height = 12.dp)
            Text(
                stringResource(R.string.no_permission_content),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            CommonSpace(height = 24.dp)
            ShimmerButton(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                text = stringResource(R.string.allow)
            ) {
                viewModel.navigateToManageStorageSetting(context)
                AnalysisUtils.logEvent(AnalysisUtils.PERMISSION_BUTTON_CLICK)
            }
        } else {
            Image(
                modifier = Modifier.size(160.dp),
                painter = painterResource(R.drawable.icon_permission_request),
                contentDescription = "permission_request"
            )
            CommonSpace(height = 20.dp)
            Text(
                stringResource(R.string.permission_required),
                style = MaterialTheme.typography.titleLarge
            )
            CommonSpace(height = 12.dp)
            Text(
                stringResource(R.string.permission_required_content),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            CommonSpace(height = 24.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        1.dp, MaterialTheme.colorScheme.tertiary,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Allow to access manage all files",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Box(contentAlignment = Alignment.Center) {
                    Image(
                        modifier = Modifier.size(35.dp),
                        painter = painterResource(R.drawable.icon_toogle),
                        contentDescription = ""
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            CommonSpace(height = 24.dp)
            ShimmerButton(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                text = stringResource(R.string.allow)
            ) {
                viewModel.navigateToManageStorageSetting(context)
                AnalysisUtils.logEvent(AnalysisUtils.PERMISSION_BUTTON_CLICK)
            }
            TextButton(
                modifier = Modifier.padding(top = 8.dp),
                onClick = { isDeclined.value = true }
            ) {
                Text(stringResource(R.string.decline), color = Color.Gray)
            }
        }
    }
}