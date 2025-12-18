package com.example.menuannam

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.Player.STATE_READY
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Saves audio data to internal storage
 */
fun saveAudioToInternalStorage(context: Context, audioData: ByteArray, filename: String) {
    val file = File(context.filesDir, filename)
    FileOutputStream(file).use { fos ->
        fos.write(audioData)
    }
}

/**
 * Generates audio for a word, saves it to internal storage, and returns the file path
 */
suspend fun generateAndSaveAudio(
    context: Context,
    networkService: NetworkService,
    word: String,
    email: String,
    token: String,
    changeMessage: (String) -> Unit
): String? {
    return withContext(Dispatchers.IO) {
        try {
            // Call API to generate audio
            val response = networkService.generateAudio(
                request = AudioRequest(word = word, email = email, token = token)
            )
            
            if (response.code == 200) {
                // Decode Base64 audio data
                val audioBytes = Base64.decode(response.message, Base64.DEFAULT)
                
                // Generate filename (e.g., "word_hello.mp3")
                val filename = "${word}_audio.mp3"
                
                // Save to internal storage
                saveAudioToInternalStorage(context, audioBytes, filename)
                
                // Return the file path
                val file = File(context.filesDir, filename)
                file.absolutePath
            } else {
                changeMessage("Failed to generate audio: ${response.message}")
                null
            }
        } catch (e: Exception) {
            changeMessage("Error generating audio: ${e.message}")
            null
        }
    }
}

/**
 * Creates and configures an ExoPlayer instance for audio playback
 */
fun createAudioPlayer(
    context: Context,
    audioFilePath: String,
    changeMessage: (String) -> Unit
): ExoPlayer {
    val file = File(context.filesDir, audioFilePath)
    
    if (!file.exists()) {
        changeMessage("Audio file not found")
        throw IllegalArgumentException("Audio file does not exist: $audioFilePath")
    }
    
    val filePath = file.absolutePath
    // Create a Uri from the file path
    val uri = filePath.toUri()
    // Build the MediaItem
    val mediaItem = MediaItem.fromUri(uri)
    // Build the Player
    val player = ExoPlayer.Builder(context).build()
    
    // Add listener for playback state changes
    player.addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                STATE_BUFFERING -> {
                    // Player is buffering, show a loading indicator if desired
                    changeMessage("Buffering...")
                }
                STATE_READY -> {
                    // Player is prepared and ready to play
                    changeMessage("Ready")
                }
                STATE_ENDED -> {
                    // Playback has finished
                    player.release()
                    changeMessage("Finished")
                }
                STATE_IDLE -> {
                    // Player is idle, e.g., after release or error
                }
            }
        }
    })
    
    // Set the media item to the player and prepare
    player.setMediaItem(mediaItem)
    // Prepare the player.
    player.prepare()
    // Start the playback.
    player.play()
    
    return player
}

/**
 * Plays audio for a word by generating it, saving it, and playing with ExoPlayer
 */
suspend fun playWordAudio(
    context: Context,
    networkService: NetworkService,
    word: String,
    email: String,
    token: String,
    changeMessage: (String) -> Unit,
    onPlayerReady: (ExoPlayer) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            changeMessage("Generating audio...")
            
            // Generate and save audio
            val audioFilePath = generateAndSaveAudio(
                context = context,
                networkService = networkService,
                word = word,
                email = email,
                token = token,
                changeMessage = changeMessage
            )
            
            if (audioFilePath != null) {
                // Switch to Main thread to create player and play
                withContext(Dispatchers.Main) {
                    try {
                        // Extract just the filename from the full path
                        val filename = File(audioFilePath).name
                        val player = createAudioPlayer(
                            context = context,
                            audioFilePath = filename,
                            changeMessage = changeMessage
                        )
                        
                        // Player already starts playing in createAudioPlayer
                        onPlayerReady(player)
                    } catch (e: Exception) {
                        changeMessage("Error playing audio: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            changeMessage("Error: ${e.message}")
        }
    }
}

