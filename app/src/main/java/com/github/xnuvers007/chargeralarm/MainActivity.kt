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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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

@Composable
fun ChargerAlarmApp(isActiveInitial: Boolean, onActivate: () -> Unit, onDeactivate: () -> Unit) {
    val context = LocalContext.current
    var isActive by remember { mutableStateOf(isActiveInitial) }
    val lifecycleOwner = LocalLifecycleOwner.current

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
                text = if (isActive) "PROTECTION ACTIVE" else "PROTECTION OFF",
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
                    text = if (isActive) "STOP ALARM\n&\nDEACTIVATE" else "ACTIVATE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Instructions:",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "1. Plug in your charger.\n2. Tap ACTIVATE to enable protection.\n3. If the charger is unplugged, a loud alarm will sound.\n4. To stop the alarm or disable protection, open this app and tap STOP ALARM.",
                        color = Color.LightGray,
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}
