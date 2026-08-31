# ChargerAlarm Wiki

Welcome to the official documentation for **ChargerAlarm**. This document explains the inner workings, security mechanisms, memory management, and overall architecture of the app.

## 1. System Architecture

ChargerAlarm relies on a few core Android components that work together asynchronously:

- **MainActivity**: The user interface, built purely in **Jetpack Compose**. It handles permissions (POST_NOTIFICATIONS for Android 13+) and saves the alarm state using `SharedPreferences`.
- **AlarmService**: A `Foreground Service`. Since Android 8.0 (Oreo), background services are heavily restricted to save battery. By making it a Foreground Service, the Android OS treats it as a high-priority task, reducing the likelihood of it being killed by the system.
- **ChargerReceiver**: A dynamically registered `BroadcastReceiver` inside the `AlarmService` that listens for the `android.intent.action.ACTION_POWER_DISCONNECTED` intent.
- **BootReceiver**: A statically registered `BroadcastReceiver` that ensures the app starts up immediately after a reboot.

### Flow Diagram
1. User plugs in phone -> Opens App -> Taps "ACTIVATE".
2. `MainActivity` saves `isActive = true` in `SharedPreferences` and starts `AlarmService`.
3. `AlarmService` shows a persistent notification and registers `ChargerReceiver`.
4. If the charger is unplugged, the OS broadcasts `ACTION_POWER_DISCONNECTED`.
5. `ChargerReceiver` catches the intent and calls `triggerAlarm()`.
6. Alarm sounds and volume is locked to maximum.

---

## 2. Security & Anti-Bypass Mechanisms

ChargerAlarm is designed to be extremely difficult for a thief to bypass.

### A. Volume Lock (Anti-Mute)
When the alarm is triggered, it doesn't just play sound. It launches a Kotlin **Coroutine** (`volumeJob`) that loops every **100 milliseconds**. 
Inside this loop, it actively queries the `AudioManager` for the maximum volume of `STREAM_ALARM` and forces the device to that volume.
- **Result**: If a thief presses the physical "Volume Down" button, the volume will instantly snap back to 100% within a fraction of a second.

### B. Reboot Persistence (Anti-Restart)
A common tactic for thieves is to quickly restart the phone to kill any running alarm apps. 
- **Solution**: We implemented a `BootReceiver` that listens for `BOOT_COMPLETED` and `QUICKBOOT_POWERON`.
- **Mechanism**: When the phone finishes rebooting, the system triggers `BootReceiver`. It reads `SharedPreferences`. If `isActive` was `true` before the reboot, the `BootReceiver` will automatically and silently start the `AlarmService` again in the background.

### C. Auto-Deactivate on Secure Unlock
To provide a seamless experience without sacrificing security, the `AlarmService` registers a receiver for `android.intent.action.USER_PRESENT`. 
- **Mechanism**: The Android OS strictly broadcasts this intent *only* when the user has successfully bypassed the keyguard (e.g., entered the correct PIN, Pattern, or authenticated via Biometrics).
- **Result**: When the phone is unlocked successfully, the service automatically deactivates the alarm and updates the `SharedPreferences` to `isActive = false`, stopping itself without requiring the user to open the app manually. A thief cannot trigger this intent because they cannot unlock the device.

### D. No UI Bypass
The `AlarmService` does not rely on `MainActivity` to be open. A thief cannot simply swipe the app away from the "Recent Apps" screen to stop the alarm. The *only* ways to stop it are by unlocking the phone securely or by passing `false` to the `SharedPreferences` via the "STOP ALARM" button inside the app.

---

## 3. Memory Management & Leak Prevention

Since `AlarmService` can run indefinitely in the background, preventing memory leaks is critical for battery life and device stability.

- **Broadcast Receiver Unregistration**: The `ChargerReceiver` is strictly tied to the lifecycle of the `AlarmService`. It is registered in `onCreate()` and explicitly unregistered in `onDestroy()`. This prevents the classic "Leaked IntentReceiver" exception.
- **Coroutine Scope Cancellation**: The volume-locking mechanism uses a dedicated `CoroutineScope`. In the `onDestroy()` method of the service, `scope.cancel()` is called. This guarantees that if the service is destroyed, the infinite `while(isActive)` loop is terminated immediately, preventing CPU and memory drain.
- **MediaPlayer Release**: Audio playback is handled by `android.media.MediaPlayer`. In Android, holding onto a `MediaPlayer` instance without releasing it causes severe memory leaks and can exhaust the hardware audio decoders. We ensure `mediaPlayer?.release()` is always called, and the reference is set to `null` (`mediaPlayer = null`) to allow the Garbage Collector (GC) to reclaim the memory.
- **No View References in Service**: The Service contains absolutely zero references to UI components (`Views` or `Composables`), avoiding Context leaks.

---

## 4. Development & CI/CD

This project uses **GitHub Actions** for CI/CD automation.

- **Workflow File**: `.github/workflows/generate-apk.yml`
- **Process**: Every push to the `main` branch triggers an Ubuntu runner. It provisions JDK 17 and uses the official `gradle/actions/setup-gradle@v3` action to run `./gradlew assembleDebug`.
- **Result**: It outputs an `app-debug.apk` available in the Artifacts section. This eliminates the need for any local Android Studio setup. You can code directly in the repository and let the cloud build the APK for you.
