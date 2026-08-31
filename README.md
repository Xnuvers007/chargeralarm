<div align="center">
  <img src="https://raw.githubusercontent.com/Xnuvers007/chargeralarm/main/app/src/main/res/drawable/ic_launcher.xml" width="120" alt="Charger Alarm Icon" />
  <h1>🛡️ ChargerAlarm</h1>
  <p><strong>Aplikasi Anti-Maling Canggih untuk Melindungi Smartphone Anda Saat Di-Charge!</strong></p>

  <p>
    <a href="https://github.com/Xnuvers007/chargeralarm/actions/workflows/generate-apk.yml"><img src="https://github.com/Xnuvers007/chargeralarm/actions/workflows/generate-apk.yml/badge.svg" alt="Build Status"></a>
    <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT"></a>
    <a href="https://developer.android.com/about/versions/nougat"><img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84.svg?logo=android" alt="Minimum Android Version"></a>
  </p>
</div>

<br/>

## 📑 Daftar Isi (Table of Contents)
- [Tentang Aplikasi](#-tentang-aplikasi)
- [Fitur Utama](#-fitur-utama)
- [Cara Penggunaan](#-cara-penggunaan)
- [Dokumentasi Lengkap (Wiki)](#-dokumentasi-lengkap-wiki)
- [Unduh & Instal](#-unduh--instal)
- [Teknologi (Tech Stack)](#-teknologi-tech-stack)
- [Daftar Pustaka](#-daftar-pustaka-references)
- [Lisensi](#-lisensi)

---

## 📱 Tentang Aplikasi

**ChargerAlarm** adalah aplikasi Android berbasis keamanan tinggi yang dirancang untuk menjaga perangkat Anda saat sedang diisi daya (di-*charge*) di tempat umum. Jika pengisi daya dicabut tanpa izin, aplikasi akan segera memicu sirine darurat dengan volume maksimum yang tidak dapat dikecilkan.

---

## ✨ Fitur Utama

- **🚨 Deteksi Instan:** Alarm berbunyi tepat sepersekian detik setelah charger dicabut.
- **🔊 Anti-Bungkam (Volume Paksa):** Mencegah pencuri mengecilkan volume. Aplikasi secara agresif mengunci volume di tingkat 100%.
- **🎧 Anti-Headset Bypass:** Memaksa suara sirine keluar dari *Loudspeaker* utama HP, mengabaikan segala jenis *headset* atau *earphone Bluetooth* yang dicolokkan oleh pencuri.
- **🧟‍♂️ Persistensi Reboot (Anti-Restart):** Jika pencuri mematikan paksa (Restart) HP Anda, alarm akan otomatis bangkit dan berbunyi kembali setelah HP menyala.
- **🔐 Keamanan Layar Pintar:** Membaca status biometrik (Sidik Jari/Wajah). Alarm hanya akan berhenti jika pemilik sah berhasil membuka kunci (*Unlock*) HP.
- **🛡️ Anti-Reverse Engineering:** Dilindungi oleh R8 Obfuscation standar industri untuk mencegah pembongkaran kode (dekompilasi).
- **🤖 Kompatibilitas Luas:** Mendukung perangkat jadul Android 7.0 (Nougat) hingga sistem operasi terbaru Android 16.
- **⚙️ CI/CD Otomatis:** APK terenkripsi dihasilkan dan didistribusikan secara otomatis melalui GitHub Actions.

---

## 🚀 Cara Penggunaan

1. **Colokkan** *charger* ke HP Anda.
2. Buka aplikasi **ChargerAlarm**.
3. Ketuk tombol **ACTIVATE** (layar akan berubah menjadi hijau).
4. Matikan/kunci layar HP Anda dan tinggalkan dengan tenang.
5. Jika *charger* dicabut oleh seseorang, alarm akan langsung menggelegar!
6. **Untuk mematikan alarm:** 
   - **Opsi A (Paling Mudah):** Cukup buka kunci HP Anda menggunakan Sidik Jari/PIN/Wajah. Alarm akan mendeteksi pemiliknya dan mati secara otomatis.
   - **Opsi B:** Buka aplikasi dan tekan tombol **STOP ALARM & DEACTIVATE**.

---

## 📖 Dokumentasi Lengkap (Wiki)

Untuk mempelajari lebih dalam mengenai arsitektur keamanan, manajemen memori (mencegah *Memory Leak*), siklus hidup *Foreground Service*, dan desain persistensi aplikasi, silakan kunjungi dokumentasi resmi kami di sini:

👉 **[Baca ChargerAlarm Wiki Lengkap](https://github.com/Xnuvers007/chargeralarm/wiki)** 👈

---

## 📥 Unduh & Instal

Anda tidak perlu melakukan *compile* kode secara manual! Semua proses dilakukan otomatis oleh robot GitHub.

1. Buka halaman **[Releases](https://github.com/Xnuvers007/chargeralarm/releases)** di repositori ini.
2. Unduh file APK terbaru bernama **`ChargerAlarm-v1.0.X.apk`**.
3. *Install* APK tersebut di HP Android Anda. *(Pembaruan versi di masa depan dapat langsung ditimpa/install berkat implementasi Static Signature JKS).*

---

## 🛠️ Teknologi (Tech Stack)

- [Kotlin](https://kotlinlang.org/) - Bahasa pemrograman modern dan resmi untuk pengembangan Android.
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - *Toolkit* deklaratif modern untuk membangun UI Android yang mulus dan reaktif.
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Untuk penegakan volume asinkron (pemantauan 100ms) tanpa membebani memori.
- [GitHub Actions](https://github.com/features/actions) - Untuk *Continuous Integration* & perilisan otomatis.

---

## 📚 Daftar Pustaka (References)

Penelitian dan perancangan aplikasi ini merujuk pada dokumentasi resmi dan standar keamanan pengembangan Android:

1. **Android Foreground Services (Keamanan & Background Execution)**
   *Mengatur agar sistem Android tidak membunuh layanan alarm ketika berjalan di latar belakang (terutama pada Android 14+).*
   - [Foreground Services Overview - Android Developers](https://developer.android.com/guide/components/foreground-services)
2. **Android AudioAttributes & AudioDeviceInfo (Anti-Headset Routing)**
   *Memaksa jalur audio (routing) menembus speaker utama bawaan (`TYPE_BUILTIN_SPEAKER`) untuk menggagalkan bypass melalui headset/bluetooth.*
   - [AudioAttributes Builder - Android Developers](https://developer.android.com/reference/android/media/AudioAttributes.Builder)
   - [AudioDeviceInfo Reference - Android Developers](https://developer.android.com/reference/android/media/AudioDeviceInfo)
3. **Android Broadcast Exceptions (Deteksi Cabut Charger & Boot)**
   *Menerima sinyal instan saat aliran listrik terputus (`ACTION_POWER_DISCONNECTED`) dan saat HP selesai dinyalakan ulang (`ACTION_BOOT_COMPLETED`).*
   - [Implicit Broadcast Exceptions - Android Developers](https://developer.android.com/guide/components/broadcast-exceptions)
4. **KeyguardManager (Deteksi Kunci Layar Biometrik)**
   *Mendeteksi apakah layar sedang terkunci atau pengguna sah (pemilik sidik jari) baru saja membuka layar (`ACTION_USER_PRESENT`).*
   - [KeyguardManager API - Android Developers](https://developer.android.com/reference/android/app/KeyguardManager)
5. **Shrink, Obfuscate, and Optimize (R8 Compiler)**
   *Melindungi kode sumber aplikasi dari rekayasa balik (reverse engineering) dan merampingkan ukuran APK.*
   - [Shrink your code and resources - Android Studio](https://developer.android.com/studio/build/shrink-code)

---

## 📝 Lisensi

Didistribusikan di bawah Lisensi MIT. Lihat file `LICENSE` untuk informasi lebih lanjut.

<div align="center">
  <sub>Dibangun dengan ❤️ oleh <a href="https://github.com/Xnuvers007">Xnuvers007</a></sub>
</div>
