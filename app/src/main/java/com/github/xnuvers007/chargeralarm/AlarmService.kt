package com.github.xnuvers007.chargeralarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.app.KeyguardManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private var volumeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isAlarmRinging = false

    private val chargerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_POWER_DISCONNECTED) {
                val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                // Hanya bunyikan alarm jika HP dalam keadaan terkunci
                if (keyguardManager.isKeyguardLocked) {
                    triggerAlarm()
                }
            }
        }
    }

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) {
                // Phone successfully unlocked! 
                // Matikan suara alarm, tapi JANGAN matikan service agar tetap aktif untuk pengecasan berikutnya.
                stopAlarm()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        startForeground(1, buildNotification())
        registerReceiver(chargerReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
        registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(chargerReceiver)
        unregisterReceiver(unlockReceiver)
        stopAlarm()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun triggerAlarm() {
        if (isAlarmRinging) return
        isAlarmRinging = true

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmService, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                
                // Paksa suara keluar dari speaker HP bawaan, abaikan headset/bluetooth
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                    for (device in devices) {
                        if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                            setPreferredDevice(device)
                            break
                        }
                    }
                }
                
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        volumeJob = scope.launch {
            while (isActive && isAlarmRinging) {
                try {
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
                } catch (e: Exception) {
                    // Ignore volume errors
                }
                delay(100) // Force max volume every 100ms
            }
        }
    }

    private fun stopAlarm() {
        isAlarmRinging = false
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        volumeJob?.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CHARGER_ALARM_CHANNEL",
                "Charger Alarm Protection",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "CHARGER_ALARM_CHANNEL")
            .setContentTitle("Anti-Theft Protection Active")
            .setContentText("Alarm will ring if charger is unplugged")
            .setSmallIcon(android.R.drawable.ic_secure) // built-in Android icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
