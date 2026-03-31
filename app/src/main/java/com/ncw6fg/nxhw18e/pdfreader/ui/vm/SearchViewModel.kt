package com.ncw6fg.nxhw18e.pdfreader.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncw6fg.nxhw18e.pdfreader.repo.DocRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * create by colin
 * 2026/2/21
 */

@HiltViewModel
class SearchViewModel @Inject constructor(
    docRepository: DocRepository
) : ViewModel() {

    private val _searchKeyword = MutableStateFlow("")
    val keyword = _searchKeyword.asStateFlow()

    val searchResult = docRepository.allDocFiles.combine(_searchKeyword) { allFiles, keyword ->
        if (keyword.isEmpty()) return@combine emptyList()
        allFiles.filter {
            it.name.contains(keyword)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        emptyList()
    )

    fun search(query: String) {
        _searchKeyword.value = query
    }
}