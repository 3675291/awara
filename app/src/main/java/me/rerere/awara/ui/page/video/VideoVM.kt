package me.rerere.awara.ui.page.video

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.rerere.awara.data.entity.Video
import me.rerere.awara.data.entity.VideoFile
import me.rerere.awara.data.repo.MediaRepo
import me.rerere.awara.data.source.onError
import me.rerere.awara.data.source.onException
import me.rerere.awara.data.source.onSuccess
import me.rerere.awara.data.source.runAPICatching

private const val TAG = "VideoVM"

class VideoVM(
    savedStateHandle: SavedStateHandle,
    private val mediaRepo: MediaRepo
) : ViewModel() {
    val id = checkNotNull(savedStateHandle.get<String>("id"))
    var state by mutableStateOf(VideoState())
        private set
    val events = MutableSharedFlow<VideoEvent>()

    init {
        getVideo()
    }

    private fun getVideo() {
        viewModelScope.launch {
            state = state.copy(loading = true)
            runAPICatching {
                val video = mediaRepo.getVideo(id)
                val urls = mediaRepo.parseVideoUrl(video)
                video to urls
            }.onSuccess {
                state = state.copy(video = it.first, urls = it.second)
                events.emit(VideoEvent.UrlLoaded(it.second))
            }.onError {
                Log.w(TAG, "getVideo: $it", )
            }.onException {
                Log.w(TAG, "getVideo: ${it.exception}", )
            }
            state = state.copy(loading = false)
        }
    }

    data class VideoState(
        val loading: Boolean = false,
        val video: Video? = null,
        val urls: List<VideoFile> = emptyList()
    )

    sealed class VideoEvent {
        class UrlLoaded(val urls: List<VideoFile>) : VideoEvent()
    }
}