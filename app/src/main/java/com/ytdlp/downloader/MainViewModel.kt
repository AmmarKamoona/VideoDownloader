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

data class UiState(
    val url: String = "",
    val status: String = "",
    val isWorking: Boolean = false,
    val lastFile: String? = null,
    val error: String? = null,
    val videoTitle: String? = null
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
                isWorking = true,
                status = "Fetching info…",
                error = null,
                videoTitle = null
            )
            try {
                val title = withContext(Dispatchers.IO) {
                    val py = Python.getInstance()
                    val module = py.getModule("ytdlp_wrapper")
                    module.callAttr("get_title", url).toString()
                }
                _state.value = _state.value.copy(
                    isWorking = false,
                    status = "",
                    videoTitle = title
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(
                    isWorking = false,
                    status = "",
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
                status = "Downloading… (this can take a while)",
                error = null,
                lastFile = null
            )
            try {
                val resultPath = withContext(Dispatchers.IO) {
                    val py = Python.getInstance()
                    val module = py.getModule("ytdlp_wrapper")
                    module.callAttr("download", url, outputDir).toString()
                }
                _state.value = _state.value.copy(
                    isWorking = false,
                    status = "Done",
                    lastFile = resultPath
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(
                    isWorking = false,
                    status = "",
                    error = t.message ?: "Unknown error"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
