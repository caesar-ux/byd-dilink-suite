package com.byd.dilink.media.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byd.dilink.media.data.FolderItem
import com.byd.dilink.media.data.MediaScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortMode {
    NAME_ASC, NAME_DESC, DATE_NEWEST, DATE_OLDEST, SIZE_LARGEST
}

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val mediaScanner: MediaScanner
) : ViewModel() {

    private val _storageVolumes = MutableStateFlow<List<FolderItem>>(emptyList())
    val storageVolumes: StateFlow<List<FolderItem>> = _storageVolumes.asStateFlow()

    private val _currentPath = MutableStateFlow<String?>(null)
    val currentPath: StateFlow<String?> = _currentPath.asStateFlow()

    private val _fileList = MutableStateFlow<List<Any>>(emptyList())
    val fileList: StateFlow<List<Any>> = _fileList.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.NAME_ASC)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val pathStack = mutableListOf<String>()

    init {
        loadStorageVolumes()
    }

    private fun loadStorageVolumes() {
        viewModelScope.launch {
            val volumes = mediaScanner.getStorageVolumes()
            _storageVolumes.value = volumes
            // Show storage volumes as initial view
            _fileList.value = volumes
        }
    }

    fun navigateTo(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentPath.value?.let { pathStack.add(it) }
            _currentPath.value = path
            loadDirectory(path)
            _isLoading.value = false
        }
    }

    /**
     * Navigate up one level. Returns true if navigated up, false if at root.
     */
    fun navigateUp(): Boolean {
        return if (pathStack.isNotEmpty()) {
            val previousPath = pathStack.removeAt(pathStack.lastIndex)
            _currentPath.value = previousPath
            viewModelScope.launch {
                _isLoading.value = true
                loadDirectory(previousPath)
                _isLoading.value = false
            }
            true
        } else if (_currentPath.value != null) {
            // Return to storage volume selection
            _currentPath.value = null
            _fileList.value = _storageVolumes.value
            true
        } else {
            false
        }
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        val current = _currentPath.value
        if (current != null) {
            viewModelScope.launch {
                loadDirectory(current)
            }
        }
    }

    fun refresh() {
        val current = _currentPath.value
        if (current != null) {
            viewModelScope.launch {
                _isLoading.value = true
                loadDirectory(current)
                _isLoading.value = false
            }
        } else {
            loadStorageVolumes()
        }
    }

    private suspend fun loadDirectory(path: String) {
        val items = mediaScanner.listDirectory(path)
        _fileList.value = applySorting(items)
    }

    private fun applySorting(items: List<Any>): List<Any> {
        val folders = items.filterIsInstance<FolderItem>()
        val files = items.filterIsInstance<com.byd.dilink.media.data.MediaFile>()

        val sortedFiles = when (_sortMode.value) {
            SortMode.NAME_ASC -> files.sortedBy { it.displayTitle.lowercase() }
            SortMode.NAME_DESC -> files.sortedByDescending { it.displayTitle.lowercase() }
            SortMode.DATE_NEWEST -> files.sortedByDescending { it.lastModified }
            SortMode.DATE_OLDEST -> files.sortedBy { it.lastModified }
            SortMode.SIZE_LARGEST -> files.sortedByDescending { it.sizeBytes }
        }

        return folders + sortedFiles
    }
}
