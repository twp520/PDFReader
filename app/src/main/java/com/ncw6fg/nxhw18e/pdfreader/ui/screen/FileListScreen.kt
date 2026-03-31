package com.ncw6fg.nxhw18e.pdfreader.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.data.DocFile
import com.ncw6fg.nxhw18e.pdfreader.money.AnalysisUtils
import com.ncw6fg.nxhw18e.pdfreader.ui.bean.DocumentType
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.AdDialog
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.CommonTopBar
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.fillMax
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.FileListViewModel


/**
 * create by colin
 * 2026/2/7
 */

@Composable
fun FileListScreen(
    title: String, viewModel: FileListViewModel = viewModel(),
    back: () -> Unit
) {
    val files = viewModel.files.collectAsStateWithLifecycle()
    val showAdLoading = viewModel.showAdLoading.collectAsStateWithLifecycle()
    Scaffold(
        modifier = fillMax,
        topBar = {
            CommonTopBar(title = title, back)
        }) { innerPadding ->
        LazyColumn(
            fillMax.padding(innerPadding)
        ) {
            items(files.value) {
                FileListItem(modifier = Modifier.animateItem(), file = it, viewModel = viewModel)
            }
        }
    }
    if (showAdLoading.value) {
        AdDialog()
    }

    LaunchedEffect(Unit) {
        AnalysisUtils.logEvent(AnalysisUtils.SCREEN_SHOW_FILE_LIST)
    }
}

@Composable
fun FileListItem(modifier: Modifier, file: DocFile, viewModel: FileListViewModel) {
    val ctx = LocalContext.current
    ListItem(
        modifier = modifier.clickable(true) {
            //goto preview
            viewModel.preview(ctx, file)
        },
        headlineContent = {
            Text(text = file.name, style = MaterialTheme.typography.titleMedium)
        },
        supportingContent = {
            Text(text = "${file.lastModified}  ${file.size} KB")
        },
        leadingContent = {
            val data = if (file.mimeType == DocumentType.IMAGE) {
                file.uri
            } else {
                file.mimeType.icon
            }
            AsyncImage(
                modifier = Modifier.size(40.dp),
                model = data,
                contentScale = ContentScale.Crop,
                contentDescription = file.name
            )
        },
        trailingContent = {
            // 1. 定义菜单是否展开的状态
            var showMenu by remember { mutableStateOf(false) }
            Box {
                // 2. 更多按钮（三个点）
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.string_more)
                    )
                }
                // 3. 下拉菜单
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false } // 点击外部时关闭
                ) {
                    // 收藏选项
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (file.isFavorite)
                                    stringResource(R.string.action_unfavorite) else stringResource(
                                    R.string.action_favorite
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (file.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showMenu = false
                            viewModel.favorite(file)
                        }
                    )
                    // 分享选项
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_share)) },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            viewModel.share(ctx, file)
                        }
                    )
                    // 删除选项
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.action_delete),
                                color = Color.Red
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red
                            )
                        },
                        onClick = {
                            showMenu = false
                            viewModel.delete(file)
                        }
                    )
                }
            }

        },
    )
    HorizontalDivider()

}