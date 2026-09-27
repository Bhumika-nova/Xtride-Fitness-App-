# ⚡ xtride — Offline-First Android Fitness & Outdoor Tracking Engine

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Language-Kotlin%202.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Database-Room%20SQLite-00599C?style=for-the-badge&logo=sqlite&logoColor=white" />
  <img src="https://img.shields.io/badge/Design-Obsidian%20%26%20Crimson-E11D48?style=for-the-badge" />
</p>


## Features

### 1. Hardware Step Counter & Daily Dashboard (`Home`)
- **Real-Time Step Detection**: Integrates hardware `Sensor.TYPE_STEP_COUNTER` and `Sensor.TYPE_STEP_DETECTOR` with zero-battery-drain background listeners.
- **Dynamic Circular Progress Meter**: Visualizes daily step goal completion with live percentage calculations.
- **Daily Telemetry**: Automatically derives distance covered (km), active calories burned (kcal), and active time (minutes) from biometric height/weight ratios.
- **Interactive Goal Editor**: Persistent bottom sheet for customizing daily step targets.
- **Activity Heatmap**: GitHub-style weekly contribution grid showing daily milestone consistency.

### 2. Live Outdoor GPS Engine (`Track`)
- **Multi-Activity Engine**: Seamlessly tracks **Walk**, **Run**, and **Mountain Hike** sessions.
- **Foreground Tracking Service**: Fully compliant with **Android 14+** (`FOREGROUND_SERVICE_TYPE_LOCATION`) to guarantee uninterrupted background GPS updates.
- **Zero-API-Key Map Rendering**: Utilizes high-performance **CyclOSM** vector-grade trail tiles with a custom Obsidian dark matrix color filter.
- **Live Trailing Polyline**: Real-time Crimson glowing route line with custom high-contrast location marker, auto-follow camera, and instant recenter controls.
- **Real-Time Telemetry HUD**:
  - Distance (km)
  - Elapsed Time (`MM:SS` / `HH:MM:SS`)
  - Activity-Specific Pace (`Running`, `Walking`, or `Hiking` in `/km`)
  - Cadence (live Strides Per Minute - SPM)
- **Custom Dark Notification**: Rich foreground notification featuring an interactive `● GPS Active` / `● Paused` pill, real-time telemetry card, and direct **Pause / Resume** & **Finish** controls.
- **Categorized History**: Filter historical sessions by `All`, `Walk`, `Run`, or `Hike` with complete telemetry metrics, date stamps, and deletion options.

###  3. Strength & Resistance Workout Tracker (`Workout`)
- **Exercise Presets**: Quick selection for Squats, Bench Press, Deadlifts, Pull-Ups, Overhead Press, Barbell Rows, plus custom movement entry.
- **Precision Stepper Controls**: Rapid adjustment cards for **Weight (kg)** (with `±2.5kg` and `±5kg` deltas), **Reps**, and **Sets**.
- **Real-Time Volume Calculator**: Instant preview of total session tonnage (`Sets × Reps × Weight`).
- **Offline History Logs**: Detailed log of past strength training sessions grouped chronologically.

### 4. Performance Analytics & Reports (`Analytics`)
- **Time Range Filtering**: Analyze consistency over **7-Day** and **30-Day** rolling windows.
- **Interactive Custom Canvas Chart**: Smooth vertical bar chart visualizing daily activity volume with peak-day highlights.
- **Metric Highlights**: Total steps, daily average, personal best record, and cumulative workout volume.
- **Daily Breakdown List**: Day-by-day record of milestone status and total activity counts.

### 5. Health Profile & Multi-User Isolation (`Profile`)
- **BMI Calculator & Visual Gauge**: Real-time calculation supporting both Metric and Imperial units with standard classification bands (Underweight, Normal, Overweight, Obese).
- **Biometric Profiles**: Stores height, weight, age, gender, and personal step targets.
- **Firebase Authentication**: Email/Password and Google Sign-In with 100% on-device Room isolation mapped directly to the active user's UUID.

---

## Tech Stack & Libraries

| Category | Technology |
|---|---|
| **Language** | Kotlin 2.2 |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Design System** | Custom Obsidian Dark (`#040711`) + Crimson Red (`#E11D48`) |
| **Local Persistence** | Room SQLite (Multi-table schema with Foreign Keys) |
| **Asynchronous Engine**| Kotlin Coroutines & Reactive StateFlow |
| **Location Services** | Google Play Services (`FusedLocationProviderClient`) |
| **Map Rendering** | OSMDroid + CyclOSM Tile Engine |
| **Authentication** | Firebase Authentication & Google One Tap Sign-In |
| **Sensors** | Android Sensor Framework (Step Counter & Accelerometer) |
| **Build System** | Gradle 9.3 + Kotlin Symbol Processing (KSP) |

---
