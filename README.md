<div align="center">
  <h1>🛡️ ChargerAlarm</h1>
  <p><strong>Never worry about phone theft while charging in public spaces again!</strong></p>

  <p>
    <a href="https://github.com/Xnuvers007/chargeralarm/actions/workflows/generate-apk.yml"><img src="https://github.com/Xnuvers007/chargeralarm/actions/workflows/generate-apk.yml/badge.svg" alt="Build Status"></a>
    <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT"></a>
  </p>
</div>

<br/>

## 📱 About The App

**ChargerAlarm** is a robust Android application designed to secure your device when you leave it plugged in. If the charger is unexpectedly disconnected, the app immediately triggers an unstoppable, maximum-volume alarm.

### 🌟 Key Features
- **🚨 Instant Detection:** Triggers immediately when power is disconnected.
- **🔊 Forced Max Volume:** Actively prevents thieves from lowering the volume. The alarm stays at 100% volume aggressively.
- **🔋 Background Service:** Keeps running silently even when the app is swiped away or the screen is locked.
- **🎨 Modern UI/UX:** Built with Jetpack Compose featuring a sleek, responsive, and animated user interface.
- **🤖 Broad Compatibility:** Supports Android 7.0 (Nougat) up to the latest Android 16.
- **⚙️ CI/CD Automation:** Fully automated APK generation via GitHub Actions.

---

## 📸 Interface

*The interface features smooth color transitions (Green for Active, Deep Red for Off) with large, accessible buttons.*

---

## 🚀 How to Use

1. **Plug in** your charger.
2. Open the **ChargerAlarm** app.
3. Tap the **ACTIVATE** button. The screen will turn green.
4. Lock your phone and walk away with peace of mind.
5. If the charger is unplugged, the alarm will sound loudly!
6. To disable the alarm, you must unlock your phone, open the app, and tap **STOP ALARM & DEACTIVATE**.

---

## 🛠️ Built With
- [Kotlin](https://kotlinlang.org/) - First-class and official programming language for Android development.
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern toolkit for building native Android UI.
- [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - For asynchronous background volume enforcement.
- [GitHub Actions](https://github.com/features/actions) - For continuous integration and automated APK building.

---

## 📥 Download & Install

You don't need to build this locally! Every change pushed to the `main` branch automatically generates a new APK.

1. Go to the [Actions tab](../../actions) in this repository.
2. Click on the latest successful workflow run for **Generate APK**.
3. Scroll down to the **Artifacts** section.
4. Download the `ChargerAlarm-APK` zip file.
5. Extract the `.apk` and install it on your Android device.

---

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.

<div align="center">
  <sub>Built with ❤️ by <a href="https://github.com/Xnuvers007">Xnuvers007</a></sub>
</div>
