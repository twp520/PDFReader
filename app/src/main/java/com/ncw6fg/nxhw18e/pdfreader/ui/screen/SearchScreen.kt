package com.ncw6fg.nxhw18e.pdfreader.ui.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.money.AnalysisUtils
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.AdDialog
import com.ncw6fg.nxhw18e.pdfreader.ui.theme.fillMax
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.FileListViewModel
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.SearchViewModel
import com.ncw6fg.nxhw18e.pdfreader.ui.vm.SimpleViewModel

/**
 * create by colin
 * 2026/2/21
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(back: () -> Unit, searchViewModel: SearchViewModel = viewModel()) {
    val fileListViewModel = viewModel<FileListViewModel>()
    val files = searchViewModel.searchResult.collectAsStateWithLifecycle()
    val keyword = searchViewModel.keyword.collectAsStateWithLifecycle()
    val simpleViewModel = viewModel<SimpleViewModel>()
    val showAd = simpleViewModel.showAdLoading.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    Scaffold(
        modifier = fillMax,
    ) { innerPadding ->
        Column(fillMax.padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    activity?.let {
                        simpleViewModel.showAD(
                            it,
                            AnalysisUtils.FROM_BACK_INTER,
                            finish = back
                        )
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = keyword.value,
                    onValueChange = {
                        searchViewModel.search(it)
                    },
                    label = { Text(text = stringResource(id = R.string.search_title)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "search"
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
            }

            LazyColumn {
                items(files.value, key = { it.path }) {
                    FileListItem(
                        modifier = Modifier.animateItem(),
                        file = it,
                        viewModel = fileListViewModel
                    )
                }
            }
        }
        if (showAd.value) {
            AdDialog()
        }
    }
}