<div align="center">
  <img src="https://raw.githubusercontent.com/Xnuvers007/chargeralarm/main/app/src/main/res/drawable/ic_launcher.xml" width="120" alt="Charger Alarm Icon" />
  <h1>🛡️ ChargerAlarm</h1>
  <p><strong>Advanced Anti-Theft App to Secure Your Smartphone While Charging!</strong></p>

  <p>
    <a href="https://github.com/Xnuvers007/chargeralarm/actions/workflows/generate-apk.yml"><img src="https://github.com/Xnuvers007/chargeralarm/actions/workflows/generate-apk.yml/badge.svg" alt="Build Status"></a>
    <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT"></a>
    <a href="https://developer.android.com/about/versions/nougat"><img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84.svg?logo=android" alt="Minimum Android Version"></a>
  </p>
  
  <p>
    <i>Read this in other languages:</i><br/>
    <b>🇺🇸 English</b> | <a href="README-id.md">🇮🇩 Bahasa Indonesia</a>
  </p>
</div>

<br/>

## 📑 Table of Contents
- [About The App](#-about-the-app)
- [Key Features](#-key-features)
- [How to Use](#-how-to-use)
- [Full Documentation (Wiki)](#-full-documentation-wiki)
- [Download & Install](#-download--install)
- [Tech Stack](#-tech-stack)
- [References](#-references)
- [License](#-license)

---

## 📱 About The App

**ChargerAlarm** is a high-security Android application designed to protect your device while it's being charged in public spaces. If the charger is unexpectedly disconnected without authorization, the app immediately triggers an emergency siren at maximum volume that cannot be lowered.

---

## ✨ Key Features

- **🚨 Instant Detection:** The alarm sounds in a fraction of a second when the charger is unplugged.
- **🔊 Forced Max Volume:** Prevents thieves from muting the device. The app aggressively locks the media volume at 100%.
- **🎧 Anti-Headset Bypass:** Forces the siren to blast through the phone's main Loudspeaker, completely bypassing any plugged-in wired headsets or connected Bluetooth earbuds.
- **🧟‍♂️ Boot Persistence (Anti-Restart):** If a thief force-restarts the phone, the protection service automatically resurrects and resumes the alarm as soon as the device boots up.
- **🔐 Smart Screen Security:** Reads biometric status (Fingerprint/Face). The alarm will only deactivate if the legitimate owner successfully unlocks the device.
- **🛡️ Anti-Reverse Engineering:** Protected by industry-standard R8 Obfuscation to prevent source code decompilation.
- **🤖 Broad Compatibility:** Supports older devices from Android 7.0 (Nougat) up to the latest Android 16.
- **⚙️ Automated CI/CD:** Encrypted APKs are generated, signed, and distributed automatically via GitHub Actions.

---

## 🚀 How to Use

1. **Plug in** your charger.
2. Open the **ChargerAlarm** app.
3. Tap the **ACTIVATE** button (the screen will turn green).
4. Lock your phone's screen and leave it with peace of mind.
5. If the charger is unplugged by anyone, the alarm will instantly blast!
6. **To stop the alarm:** 
   - **Option A (Easiest):** Simply unlock your phone using your Fingerprint/PIN/Face. The alarm will recognize you and stop automatically.
   - **Option B:** Open the app and tap the **STOP ALARM & DEACTIVATE** button.

---

## 📖 Full Documentation (Wiki)

To dive deeper into the security architecture, memory management (preventing memory leaks), Foreground Service lifecycle, and app persistence design, please visit our official documentation here:

👉 **[Read the Full ChargerAlarm Wiki](https://github.com/Xnuvers007/chargeralarm/wiki)** 👈

---

## 📥 Download & Install

You don't need to manually compile the code! Everything is handled automatically by GitHub bots.

1. Go to the **[Releases](https://github.com/Xnuvers007/chargeralarm/releases)** page in this repository.
2. Download the latest APK file named **`ChargerAlarm-v1.0.X.apk`**.
3. Install the APK on your Android device. *(Future updates can be installed seamlessly on top of existing ones thanks to the Static JKS Signature).*

---

## 🛠️ Tech Stack

- [Kotlin](https://kotlinlang.org/) - The modern, official language for Android development.
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - A modern declarative toolkit for building smooth and reactive native UIs.
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - For asynchronous volume enforcement (100ms loops) without memory bloat.
- [GitHub Actions](https://github.com/features/actions) - For Continuous Integration & automated release deployment.

---

## 📚 References

The research and engineering of this application strictly follow official documentation and Android security best practices:

1. **Android Foreground Services (Security & Background Execution)**
   *Configures the Android OS to prevent killing the alarm service in the background (especially critical on Android 14+).*
   - [Foreground Services Overview - Android Developers](https://developer.android.com/guide/components/foreground-services)
2. **Android AudioAttributes & AudioDeviceInfo (Anti-Headset Routing)**
   *Forces audio routing strictly to the built-in speaker (`TYPE_BUILTIN_SPEAKER`) to foil bypass attempts via headsets/bluetooth.*
   - [AudioAttributes Builder - Android Developers](https://developer.android.com/reference/android/media/AudioAttributes.Builder)
   - [AudioDeviceInfo Reference - Android Developers](https://developer.android.com/reference/android/media/AudioDeviceInfo)
3. **Android Broadcast Exceptions (Disconnect & Boot Detection)**
   *Receives instant signals when power is severed (`ACTION_POWER_DISCONNECTED`) and when the phone finishes restarting (`ACTION_BOOT_COMPLETED`).*
   - [Implicit Broadcast Exceptions - Android Developers](https://developer.android.com/guide/components/broadcast-exceptions)
4. **KeyguardManager (Biometric Lock Detection)**
   *Detects whether the screen is currently locked or if the legitimate user just authenticated (`ACTION_USER_PRESENT`).*
   - [KeyguardManager API - Android Developers](https://developer.android.com/reference/android/app/KeyguardManager)
5. **Shrink, Obfuscate, and Optimize (R8 Compiler)**
   *Protects application source code from reverse engineering and minimizes the final APK size.*
   - [Shrink your code and resources - Android Studio](https://developer.android.com/studio/build/shrink-code)

---

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.

<div align="center">
  <sub>Built with ❤️ by <a href="https://github.com/Xnuvers007">Xnuvers007</a></sub>
</div>
