package com.ytdlp.downloader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class DownloadItem(
    val id: String,
    val title: String,
    val filePath: String,
    val sizeLabel: String = "",
    val isCompleted: Boolean = true
)

data class UiState(
    val url: String = "",
    val status: String = "",
    val isWorking: Boolean = false,
    val lastFile: String? = null,
    val error: String? = null,
    val videoTitle: String? = null,
    val clipboardUrl: String? = null,
    val showQuickDownload: Boolean = false,
    val downloads: List<DownloadItem> = emptyList()
)

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun setUrl(url: String) {
        _state.value = _state.value.copy(url = url, error = null)
    }

    fun fetchInfo() {
        val url = _state.value.url.trim()
        if (url.isBlank()) {
            _state.value = _state.value.copy(error = "Enter a URL first")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isWorking = true, status = "Fetching info…",
                error = null, videoTitle = null
            )
            try {
                val title = withContext(Dispatchers.IO) {
                    val py = Python.getInstance()
                    py.getModule("ytdlp_wrapper").callAttr("get_title", url).toString()
                }
                _state.value = _state.value.copy(isWorking = false, status = "", videoTitle = title)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(
                    isWorking = false, status = "",
                    error = "Could not fetch info: ${t.message}"
                )
            }
        }
    }

    fun download(outputDir: String) {
        val url = _state.value.url.trim()
        if (url.isBlank()) {
            _state.value = _state.value.copy(error = "Enter a URL first")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isWorking = true,
                status = "Downloading…",
                error = null,
                lastFile = null
            )
            try {
                val resultPath = withContext(Dispatchers.IO) {
                    val py = Python.getInstance()
                    py.getModule("ytdlp_wrapper").callAttr("download", url, outputDir).toString()
                }
                _state.value = _state.value.copy(
                    isWorking = false,
                    status    = "Done",
                    lastFile  = resultPath
                )
                // Re-scan the directory so the new file appears in Downloads tab
                refreshDownloads(outputDir)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(
                    isWorking = false, status = "",
                    error = t.message ?: "Unknown error"
                )
            }
        }
    }

    fun removeDownload(id: String) {
        // Delete from disk so it doesn't reappear on next refresh
        try { File(id).delete() } catch (e: Exception) { /* ignore */ }
        _state.value = _state.value.copy(
            downloads = _state.value.downloads.filter { it.id != id }
        )
    }

    /**
     * Scan the given directory for video files and rebuild the downloads list.
     * Called on app start and whenever MainActivity resumes so previously-downloaded
     * files survive process death.
     */
    fun refreshDownloads(downloadDir: String) {
        val dir = File(downloadDir)
        if (!dir.exists() || !dir.isDirectory) return
        val items = dir.listFiles { file ->
            file.isFile && file.extension.lowercase() in VIDEO_EXTENSIONS
        }?.sortedByDescending { it.lastModified() }?.map { file ->
            DownloadItem(
                id        = file.absolutePath,
                title     = file.name,
                filePath  = file.absolutePath,
                sizeLabel = formatSize(file.length()),
                isCompleted = true
            )
        } ?: emptyList()
        _state.value = _state.value.copy(downloads = items)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun onClipboardUrlDetected(url: String) {
        if (url != _state.value.url.trim()) {
            _state.value = _state.value.copy(clipboardUrl = url, showQuickDownload = true)
        }
    }

    fun useClipboardUrl() {
        val url = _state.value.clipboardUrl ?: return
        _state.value = _state.value.copy(
            url = url, clipboardUrl = null,
            showQuickDownload = false, error = null
        )
    }

    fun dismissClipboardBanner() {
        _state.value = _state.value.copy(clipboardUrl = null, showQuickDownload = false)
    }

    private fun formatSize(bytes: Long): String = when {
        bytes <= 0          -> ""
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024L * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
        else -> "${"%.2f".format(bytes / (1024.0 * 1024 * 1024))} GB"
    }

    companion object {
        private val VIDEO_EXTENSIONS = setOf(
            "mp4", "webm", "mkv", "mov", "m4v", "avi", "flv", "3gp", "ts"
        )
    }
}
