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
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import android.annotation.SuppressLint
import android.hardware.camera2.CameraManager
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Tasks
import java.io.File

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private var volumeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isAlarmRinging = false
    private var toneGenerator: ToneGenerator? = null

    private var strobeJob: Job? = null
    private var locationTimerJob: Job? = null
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var emergencySender: EmergencySender

    private val chargerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_POWER_DISCONNECTED) {
                val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                
                // Bunyikan alarm jika HP terkunci ATAU layar sedang mati (sleep)
                if (keyguardManager.isKeyguardLocked || !powerManager.isInteractive) {
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
        
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        emergencySender = EmergencySender(this)
        
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
            // FALLBACK: Jika sistem gagal memutar Ringtone, gunakan ToneGenerator bawaan mesin
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 1000000) // Suara sirine darurat super keras
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
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
        
        // start strobe
        strobeJob = scope.launch {
            var isTorchOn = false
            while (isActive && isAlarmRinging) {
                try {
                    cameraId?.let { id ->
                        cameraManager.setTorchMode(id, isTorchOn)
                        isTorchOn = !isTorchOn
                    }
                } catch (e: Exception) {
                    // ignore camera access exception if used by another app
                }
                delay(150)
            }
            try {
                cameraId?.let { cameraManager.setTorchMode(it, false) }
            } catch (e: Exception) {}
        }
        
        // start selfie
        val selfieIntent = Intent(this, TransparentCameraActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        try {
            startActivity(selfieIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // start emergency tracker (delay 30s)
        locationTimerJob = scope.launch {
            delay(30_000)
            if (isActive && isAlarmRinging) {
                sendEmergencyAlert()
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
        
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        toneGenerator = null
        
        volumeJob?.cancel()
        strobeJob?.cancel()
        locationTimerJob?.cancel()
        try {
            cameraId?.let { cameraManager.setTorchMode(it, false) }
        } catch (e: Exception) {}
    }
    
    @SuppressLint("MissingPermission")
    private suspend fun sendEmergencyAlert() {
        val prefs = getSharedPreferences("ChargerAlarmPrefs", MODE_PRIVATE)
        val emergencyNumber = prefs.getString("emergency_number", "") ?: ""
        val botToken = prefs.getString("bot_token", "") ?: ""
        val chatId = prefs.getString("chat_id", "") ?: ""
        val enableSms = prefs.getBoolean("enable_sms", true)
        val enableTelegram = prefs.getBoolean("enable_telegram", true)
        
        var locationText = "Location not available"
        try {
            val locationResult = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            )
            val location: Location? = Tasks.await(locationResult)
            if (location != null) {
                locationText = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val message = "🚨 URGENT: Charger Alarm Triggered! Phone might be stolen.\nLocation: $locationText"
        
        // Try SMS
        if (enableSms && emergencyNumber.isNotEmpty()) {
            emergencySender.sendSMS(emergencyNumber, message)
        }
        
        // Try Telegram
        if (enableTelegram && botToken.isNotEmpty() && chatId.isNotEmpty()) {
            val photoPath = prefs.getString("last_intruder_photo", "")
            if (photoPath != null && photoPath.isNotEmpty()) {
                val file = File(photoPath)
                if (file.exists()) {
                    emergencySender.sendTelegramPhoto(botToken, chatId, file, message)
                } else {
                    emergencySender.sendTelegramMessage(botToken, chatId, message)
                }
            } else {
                emergencySender.sendTelegramMessage(botToken, chatId, message)
            }
        }
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
