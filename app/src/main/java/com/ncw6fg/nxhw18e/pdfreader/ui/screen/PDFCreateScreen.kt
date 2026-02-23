package com.ncw6fg.nxhw18e.pdfreader.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.CommonTopBar
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.ReorderableColumn
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.PdfCreateViewModel

/**
 * create by colin
 * 2026/2/19
 */
// ... existing code ...

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreatePdfScreen(
    onBackClick: () -> Unit,
) {
    val viewModel = viewModel<PdfCreateViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        viewModel.addImages(uris)
    }

    Scaffold(
        topBar = {
            CommonTopBar(title = stringResource(R.string.main_item_create), goBack = onBackClick)
        },
        bottomBar = {
            // 底部创建按钮
            Button(
                onClick = {
                    viewModel.startCreatePDF(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                enabled = uiState.createButtonEnable,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.start_create_pdf))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- 顶部配置区 ---
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = uiState.fileName,
                        onValueChange = { viewModel.updateFileName(it) },
                        label = { Text(text = stringResource(R.string.file_name)) },
                        placeholder = { Text(stringResource(R.string.file_name_holder)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Text(
                                ".pdf ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            pickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_image))
                    }
                }
            }

            // --- 中间列表区 ---
            Text(
                text = stringResource(R.string.selected_images_tip, uiState.selectedImages.size),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Box(modifier = Modifier.weight(1f)) {
                ReorderableColumn(
                    items = uiState.selectedImages,
                    itemContent = { item, isDragging, index ->
                        ImageItem(item, index, isDragging) {
                            viewModel.removeImage(it)
                        }
                    },
                    onMove = { form, to ->
                        viewModel.moveImage(form, to)
                    }
                )
            }
        }
        // Loading 弹窗
        if (uiState.isGenerating) {
            PdfLoadingDialog(uiState.currentPage, uiState.totalPages)
        }
    }
}

@Composable
private fun ImageItem(
    uri: Uri,
    index: Int,
    isDragging: Boolean,
    onDelete: (uri: Uri) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 拖拽标记 Icon
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "sort",
                tint = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // 2. 缩略图
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            // 3. 页面描述
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.string_image_index, index + 1),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // 4. 删除按钮
            IconButton(onClick = {
                onDelete(uri)
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PdfLoadingDialog(
    currentPage: Int,
    totalPages: Int
) {
    // 计算百分比 (0.0 到 1.0)
    val progress = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.pdf_creating),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 进度条动画更加丝滑
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.pdf_creating_progress, currentPage, totalPages),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}




