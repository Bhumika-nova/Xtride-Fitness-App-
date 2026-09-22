XTRIDE — Android Fitness Tracker

Run. Walk. Lift. Tracked entirely by your phone

It is designed to track running, walking, hiking, and gym workouts without requiring a smartwatch, fitness band, or other wearable device.

The app uses the hardware already available in a smartphone, including GPS, barometric pressure sensors, and motion accelerometers, to provide activity tracking and workout analytics.

KEY FEATURES

Live Outdoor Activity Tracking

• Run, Walk, and Hike tracking
• Real-time GPS route tracking
• Live route visualization on a map
• Phone-in-pocket cadence tracking using the accelerometer
• Steps-per-minute (SPM) tracking for running
• Elevation gain measurement using the phone's barometric pressure sensor
• Instant kilometer splits
• Ability to switch between Walk, Run, and Hike activities

Gym and Strength Tracking

• Quick workout logging without extensive typing
• Stepper controls for quickly entering weights, sets, and repetitions
• Automatic workout volume calculation using Sets × Reps × Weight
• Hybrid workouts that combine outdoor activities with gym sessions

Visual Analytics and Performance Tracking

• Interactive 7-day activity and mileage chart
• Daily activity visualization
• Daily goal indicator
• Workout notes and duration for individual days
• Total distance tracking
• Average running pace
• Total elevation climbed
• Total gym weight lifted
• Filtering by All, Run, Walk, and Gym activities

Personal Records

xtride tracks personal milestones and displays them as achievement records.

• Fastest 5K
• Longest outdoor journey
• Maximum Squat weight
• Maximum Bench Press weight
• Peak elevation

Daily Activity and Streak Tracking

• Active streak counter
• Increasing visual intensity based on daily step count

Offline and Private

• Works without an internet connection
• Workout data and history are stored locally on the device
• Routes and workout records are stored using Room and SQLite
• Workout saving is designed to work without loading delays or network dependency
• GPS routes and health data remain on the device and are not uploaded or sold to third party advertising networks

TECHNOLOGY STACK

Language:
Kotlin 2.0

Asynchronous Programming:
Coroutines
Flow
StateFlow

UI:
Jetpack Compose
Material 3

Architecture:
Clean Architecture
MVI / MVVM
Unidirectional Data Flow

Local Storage:
Room Database
SQLite

Location:
Google Play Services FusedLocationProviderClient
Background Foreground Service

Device Sensors:
Android SensorManager
TYPE_PRESSURE for elevation tracking
TYPE_ACCELEROMETER for cadence tracking

Dependency Injection:
Dagger Hilt
