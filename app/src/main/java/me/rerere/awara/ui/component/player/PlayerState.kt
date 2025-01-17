package me.rerere.awara.ui.component.player

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer

private const val TAG = "PlayerState"

@Composable
fun rememberPlayerState(
    builder: (Context) -> ExoPlayer = {
        ExoPlayer.Builder(it).build()
    }
): PlayerState {
    val context = LocalContext.current
    val state by remember {
        mutableStateOf(PlayerState(builder(context)))
    }
    DisposableEffect(context) {
        val playbackListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                state.state = when(playbackState) {
                    Player.STATE_IDLE -> PlayerState.State.IDLE
                    Player.STATE_BUFFERING -> PlayerState.State.BUFFERING
                    Player.STATE_READY -> PlayerState.State.READY
                    Player.STATE_ENDED -> PlayerState.State.ENDED
                    else -> PlayerState.State.IDLE.also {
                        Log.w(TAG, "onPlaybackStateChanged: unknown state $playbackState")
                    }
                }
                if(playbackState == Player.STATE_READY){
                    state.duration = state.player.duration
                    state.currentMediaItem = state.player.currentMediaItem
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                state.playing = isPlaying
                state.duration = state.player.duration
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)
                state.loading = isLoading
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                state.videoSize = videoSize
                state.duration = state.player.duration
            }
        }
        state.player.addListener(playbackListener)
        state.playing = state.player.isPlaying
        state.state = when(state.player.playbackState) {
            Player.STATE_IDLE -> PlayerState.State.IDLE
            Player.STATE_BUFFERING -> PlayerState.State.BUFFERING
            Player.STATE_READY -> PlayerState.State.READY
            Player.STATE_ENDED -> PlayerState.State.ENDED
            else -> PlayerState.State.IDLE.also {
                Log.w(TAG, "onPlaybackStateChanged: unknown state ${state.player.playbackState}")
            }
        }
        state.videoSize = state.player.videoSize
        state.duration = state.player.duration
        Log.i(TAG, "rememberPlayerState: Init")
        onDispose {
            state.player.run {
                removeListener(playbackListener)
                release()
            }
            Log.i(TAG, "rememberPlayerState: Dispose")
        }
    }
    return state
}

class PlayerState(val player: Player) {
    var playing by mutableStateOf(false)
    var state by mutableStateOf(State.IDLE)
    var loading by mutableStateOf(false)

    var currentMediaItem by mutableStateOf<MediaItem?>(null)
    var videoSize by mutableStateOf(VideoSize.UNKNOWN)
    var duration by mutableStateOf(0L)

    fun seekTo(newPosition: Long) {
        player.seekTo(newPosition.coerceIn(0..duration))
    }
    
    enum class State {
        IDLE,
        BUFFERING,
        READY,
        ENDED
    }
}