package com.tayebi.islamiclauncher

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.tayebi.islamiclauncher.data.Settings

/** Background audio: Quran recitation (kept alive by a foreground service) and the adhan. */
class AudioController(private val ctx: Context, private val s: Settings) {
    val player: ExoPlayer = ExoPlayer.Builder(ctx)
        .setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val adhanPlayer: ExoPlayer = ExoPlayer.Builder(ctx)
        .setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_ALARM).setContentType(C.AUDIO_CONTENT_TYPE_SONIFICATION).build(), true
        )
        .build()

    var surah by mutableIntStateOf(0)
    var isPlaying by mutableStateOf(false)
    var adhanPlaying by mutableStateOf(false)
    var posMs by mutableLongStateOf(0L)
    var durMs by mutableLongStateOf(0L)

    /** Return true if playback continued with another item. */
    var onEnded: (() -> Boolean)? = null
    var onError: ((String) -> Unit)? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) startService() else if (!player.playWhenReady) stopService()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    isPlaying = false
                    if (onEnded?.invoke() != true) stopService()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isPlaying = false
                stopService()
                onError?.invoke("audio_error")
            }
        })
        adhanPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                adhanPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) adhanPlaying = false
            }

            override fun onPlayerError(error: PlaybackException) {
                adhanPlaying = false
                fallbackTone()
            }
        })
        applyVolume()
    }

    fun applyVolume() {
        val v = s.volume.value / 100f
        player.volume = v
        adhanPlayer.volume = v
    }

    private fun startService() {
        try {
            ContextCompat.startForegroundService(ctx, Intent(ctx, PlaybackService::class.java))
        } catch (_: Exception) {
        }
    }

    private fun stopService() {
        try {
            ctx.stopService(Intent(ctx, PlaybackService::class.java))
        } catch (_: Exception) {
        }
    }

    fun play(uri: Uri, surahNo: Int) {
        surah = surahNo
        applyVolume()
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
    }

    fun toggle() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun stop() {
        player.stop()
        isPlaying = false
        stopService()
    }

    fun seekTo(ms: Long) = player.seekTo(ms)

    fun poll() {
        if (isPlaying) {
            posMs = player.currentPosition
            durMs = player.duration.coerceAtLeast(0L)
        }
    }

    fun playAdhan() {
        try {
            applyVolume()
            adhanPlayer.setMediaItem(MediaItem.fromUri(s.adhanUrl.value))
            adhanPlayer.prepare()
            adhanPlayer.play()
        } catch (e: Exception) {
            fallbackTone()
        }
    }

    fun stopAdhan() {
        adhanPlayer.stop()
        adhanPlaying = false
    }

    private fun fallbackTone() {
        try {
            RingtoneManager.getRingtone(ctx, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))?.play()
        } catch (_: Exception) {
        }
    }
}
