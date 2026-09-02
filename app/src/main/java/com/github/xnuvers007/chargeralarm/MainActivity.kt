package com.github.xnuvers007.chargeralarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.ui.window.Dialog


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result handled automatically or ignored
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val sharedPref = getSharedPreferences("ChargerAlarmPrefs", Context.MODE_PRIVATE)
        val isActiveInitial = sharedPref.getBoolean("isActive", false)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    primary = Color(0xFF00E676),
                    error = Color(0xFFFF1744)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChargerAlarmApp(
                        isActiveInitial = isActiveInitial,
                        onActivate = { 
                            sharedPref.edit().putBoolean("isActive", true).apply()
                            startAlarmService() 
                        },
                        onDeactivate = { 
                            sharedPref.edit().putBoolean("isActive", false).apply()
                            stopAlarmService() 
                        }
                    )
                }
            }
        }
    }

    private fun startAlarmService() {
        val serviceIntent = Intent(this, AlarmService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopAlarmService() {
        val serviceIntent = Intent(this, AlarmService::class.java)
        stopService(serviceIntent)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChargerAlarmApp(isActiveInitial: Boolean, onActivate: () -> Unit, onDeactivate: () -> Unit) {
    val context = LocalContext.current
    var isActive by remember { mutableStateOf(isActiveInitial) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
        
        val initialSharedPref = context.getSharedPreferences("ChargerAlarmPrefs", Context.MODE_PRIVATE)
        if (initialSharedPref.getString("emergency_number", "")?.isEmpty() == true && 
            initialSharedPref.getString("bot_token", "")?.isEmpty() == true) {
            showSettings = true
        }
    }

    var showSettings by remember { mutableStateOf(false) }
    val sharedPref = context.getSharedPreferences("ChargerAlarmPrefs", Context.MODE_PRIVATE)
    var emergencyNumber by remember { mutableStateOf(sharedPref.getString("emergency_number", "") ?: "") }
    var botToken by remember { mutableStateOf(sharedPref.getString("bot_token", "") ?: "") }
    var chatId by remember { mutableStateOf(sharedPref.getString("chat_id", "") ?: "") }
    var enableSms by remember { mutableStateOf(sharedPref.getBoolean("enable_sms", true)) }
    var enableTelegram by remember { mutableStateOf(sharedPref.getBoolean("enable_telegram", true)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val sharedPref = context.getSharedPreferences("ChargerAlarmPrefs", Context.MODE_PRIVATE)
                isActive = sharedPref.getBoolean("isActive", false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val bgColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF003314) else Color(0xFF330000),
        animationSpec = tween(500),
        label = "BgColorAnimation"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        animationSpec = tween(500),
        label = "ButtonColorAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = if (isActive) stringResource(id = R.string.protection_active) else stringResource(id = R.string.protection_off),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color(0xFF00E676) else Color(0xFFFF1744),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = {
                    isActive = !isActive
                    if (isActive) {
                        onActivate()
                    } else {
                        onDeactivate()
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                modifier = Modifier
                    .size(220.dp)
                    .shadow(24.dp, CircleShape)
            ) {
                Text(
                    text = if (isActive) stringResource(id = R.string.stop_alarm) else stringResource(id = R.string.activate),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { showSettings = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("⚙️ Pengaturan (SMS & Telegram)")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(id = R.string.instructions_title),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.instructions_body),
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }

    if (showSettings) {
        Dialog(onDismissRequest = { showSettings = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                    Text("⚙️ Pengaturan Keamanan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("1. SMS Darurat (Jika Pulsa Ada)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        Switch(checked = enableSms, onCheckedChange = { enableSms = it })
                    }
                    
                    if (enableSms) {
                        Text("Aplikasi akan mengirim SMS berisi lokasi HP ke nomor ini jika charger dicabut maling.", fontSize = 12.sp, color = Color.LightGray, lineHeight = 16.sp)
                        
                        OutlinedTextField(
                            value = emergencyNumber,
                            onValueChange = { emergencyNumber = it },
                            label = { Text("Nomor HP Keluarga/Teman") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("2. Telegram Darurat (Gratis)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        Switch(checked = enableTelegram, onCheckedChange = { enableTelegram = it })
                    }
                    
                    if (enableTelegram) {
                        Text("Lebih canggih dari SMS! Kirim FOTO WAJAH pencuri & titik lokasi ke Telegram Anda.\n\nCara mendapatkan Token:", fontSize = 12.sp, color = Color.LightGray, lineHeight = 16.sp)
                        Text("- Buka aplikasi Telegram, cari bot bernama @BotFather\n- Ketik /newbot dan ikuti petunjuknya untuk membuat bot baru\n- Salin teks panjang (HTTP API Token) yang diberikan BotFather ke sini:", fontSize = 12.sp, color = Color.White, modifier = Modifier.padding(top = 4.dp), lineHeight = 16.sp)
                        
                        OutlinedTextField(
                            value = botToken,
                            onValueChange = { botToken = it },
                            label = { Text("Token Bot Telegram (Paste di sini)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Cara mendapatkan Chat ID:\n- Cari bot bernama @userinfobot di Telegram lalu tekan START.\n- Salin angka 'Id' Anda ke sini:", fontSize = 12.sp, color = Color.White, lineHeight = 16.sp)
                        
                        OutlinedTextField(
                            value = chatId,
                            onValueChange = { chatId = it },
                            label = { Text("Telegram Chat ID Anda") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showSettings = false }) {
                            Text("Nanti Saja")
                        }
                        Button(onClick = { 
                            sharedPref.edit()
                                .putString("emergency_number", emergencyNumber)
                                .putString("bot_token", botToken)
                                .putString("chat_id", chatId)
                                .putBoolean("enable_sms", enableSms)
                                .putBoolean("enable_telegram", enableTelegram)
                                .apply()
                            showSettings = false 
                        }) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}
