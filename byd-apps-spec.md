# BYD Destroyer 05 Android App Suite — Full Specification

## Overview
A multi-module Android project containing 4 car-focused apps for the BYD Destroyer 05 (2025 DM-i hybrid). Apps run on the user's Xiaomi 15 phone and are designed for use via Android Auto (Headunit Reloaded) mirrored to the car's head unit.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Gradle (Kotlin DSL), min SDK 26, target SDK 34.

## Project Structure
```
byd-car-suite/
├── build.gradle.kts              # Root build file
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   └── libs.versions.toml        # Version catalog
├── core/
│   ├── obd/                      # :core:obd — OBD-II communication layer
│   ├── model/                    # :core:model — Shared data models
│   ├── ui/                       # :core:ui — Shared theme, components
│   └── data/                     # :core:data — Room DB, preferences
├── app-diagnostics/              # :app-diagnostics — OBD-II Diagnostics app
├── app-trip-tracker/             # :app-trip-tracker — Trip & Fuel Tracker
├── app-controls/                 # :app-controls — Quick Controls Panel
└── app-hud/                      # :app-hud — HUD Dashboard Overlay
```

## Module: core:obd — OBD-II Communication Layer

### Bluetooth Connection Manager
- Scan for ELM327 devices via Bluetooth Classic (SPP UUID: 00001101-0000-1000-8000-00805F9B34FB)
- Connect/disconnect lifecycle management
- Auto-reconnect with exponential backoff
- Connection state as StateFlow

### ELM327 Protocol Handler
- Initialization sequence for BYD vehicles:
  ```
  ATZ        // Reset
  ATE0       // Echo off
  ATD        // Defaults
  ATD0       // Defaults
  ATE0       // Echo off
  ATH1       // Headers on
  ATSP6      // Protocol ISO 15765-4 CAN (11/500)
  ATM0       // Memory off
  ATS0       // Spaces off
  ATAT1      // Adaptive timing
  ATAL       // Allow long messages
  STCSEGT1   // CAN segmented transport
  ATST96     // Timeout
  ATSH7E4    // Set header for BYD
  ```
- Command queue with priority system
- Response parsing (handle multiline, errors like "NO DATA", "CAN ERROR")

### BYD-Specific PIDs
Based on community research (https://github.com/loryanstrant/BYD-PID-list), plus standard OBD-II Mode 01 PIDs:

**Standard OBD-II (Mode 01):**
| PID | Name | Formula | Unit |
|-----|------|---------|------|
| 010C | Engine RPM | ((A*256)+B)/4 | rpm |
| 010D | Vehicle Speed | A | km/h |
| 0105 | Coolant Temp | A - 40 | °C |
| 010F | Intake Air Temp | A - 40 | °C |
| 0111 | Throttle Position | (A*100)/255 | % |
| 012F | Fuel Level | (A*100)/255 | % |
| 0146 | Ambient Temp | A - 40 | °C |
| 0104 | Engine Load | (A*100)/255 | % |
| 0110 | MAF Rate | ((A*256)+B)/100 | g/s |
| 0142 | Control Module Voltage | ((A*256)+B)/1000 | V |

**BYD-Specific Extended PIDs (Mode 22):**
| PID | Name | Formula | Unit | Init Header |
|-----|------|---------|------|-------------|
| 221FFC1 | Battery SOC | ((B5*256)+B4)/100 | % | ATSH7E7 |
| 220032 | Battery Temperature | B4-40 | °C | ATSH7E7 |
| 220008 | Battery Voltage | B4 + B5*25 | V | ATSH7E7 |
| 220009 | Battery Current | ((A+B*256)-5000)/10 | A | ATSH7E7 |
| 22000B1 | Charge Cycles | B4 + B5*256 | count | ATSH7E7 |
| 2200111 | Total Charged kWh | B4+(B5*256) | kWh | ATSH7E7 |
| 2200121 | Total Discharged kWh | B4+(B5*256) | kWh | ATSH7E7 |

### Data Classes
```kotlin
data class ObdReading(
    val pid: String,
    val name: String,
    val value: Double,
    val unit: String,
    val timestamp: Long
)

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Scanning : ConnectionState()
    data class Connecting(val deviceName: String) : ConnectionState()
    data class Connected(val deviceName: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
```

## Module: core:ui — Car Theme & Design System

### Color Palette (Dark-first, car-optimized)
```kotlin
// Primary: Deep teal (inspired by BYD brand)
val BydTeal = Color(0xFF20808D)
val BydTealDark = Color(0xFF1B474D)
val BydTealLight = Color(0xFFBCE2E7)

// Background: Very dark, low distraction
val CarBackground = Color(0xFF0D0D0D)
val CarSurface = Color(0xFF1A1A1A)
val CarSurfaceVariant = Color(0xFF242424)

// Text
val CarTextPrimary = Color(0xFFE0E0E0)
val CarTextSecondary = Color(0xFF9E9E9E)
val CarTextAccent = Color(0xFF4F98A3)

// Status colors
val StatusGreen = Color(0xFF4CAF50)
val StatusYellow = Color(0xFFFFC107)
val StatusRed = Color(0xFFEF5350)
val StatusOrange = Color(0xFFFF9800)

// Gauge colors
val GaugeElectric = Color(0xFF00BCD4)  // Cyan for electric mode
val GaugeFuel = Color(0xFFFF9800)      // Orange for fuel mode
val GaugeHybrid = Color(0xFF8BC34A)    // Green for hybrid mode
```

### Typography
- Use system sans-serif (works everywhere)
- Large readability-focused sizes for driving context
- HUD mode: extra large, minimal info

### Shared Components
- `GaugeArc` — Circular gauge composable (speed, RPM, SOC)
- `MetricCard` — Card showing a single value with label, icon, and optional trend
- `StatusIndicator` — Color-coded dot/bar for connection, battery, warnings
- `ConnectionBanner` — Persistent top banner showing BT connection state
- `CarButton` — Large touch-target button (min 48dp) for driving use

## Module: core:data — Local Storage

### Room Database
```kotlin
@Entity
data class TripRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val distanceKm: Double,
    val fuelUsedL: Double,
    val electricUsedKwh: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val startOdometer: Double,
    val endOdometer: Double?,
    val driveMode: String // "EV", "Hybrid", "Fuel"
)

@Entity
data class DiagnosticSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val readings: String // JSON serialized Map<String, Double>
)
```

### DataStore Preferences
- Last connected device MAC
- Preferred units (km/mi, L/gal, °C/°F)
- HUD layout preference
- Auto-connect on launch toggle

## App 1: OBD-II Diagnostics (:app-diagnostics)

### Screens
1. **Dashboard** — Grid of live gauges:
   - Battery SOC (large arc gauge, center)
   - Engine RPM (arc gauge)
   - Coolant Temperature (vertical bar)
   - Vehicle Speed (large number)
   - Battery Voltage & Current (cards)
   - Battery Temperature (card)
   - Throttle Position (horizontal bar)
   - Engine Load (horizontal bar)

2. **DTC Scanner** — Read & clear diagnostic trouble codes
   - Button to scan all ECUs
   - List of codes with description
   - Clear codes button (with confirmation)

3. **Live Data** — Scrollable list of ALL available PIDs with real-time values
   - Toggle individual PIDs on/off
   - Logging to CSV (start/stop recording)

4. **Connection** — Bluetooth device picker
   - Scan for devices
   - Paired devices list
   - Connection status and protocol info

### Architecture
- MVVM with ViewModel + StateFlow
- Repository pattern for OBD data
- Coroutine-based polling (configurable interval: 250ms-2s)

## App 2: Trip & Fuel Tracker (:app-trip-tracker)

### Screens
1. **Active Trip** — Current trip dashboard:
   - Elapsed time
   - Distance covered (from speed integration)
   - Current fuel consumption (L/100km)
   - Current electric consumption (kWh/100km)
   - Average speed
   - Cost estimate (configurable fuel/electricity prices)
   - Start/Stop/Pause trip buttons

2. **Trip History** — List of past trips:
   - Date, distance, duration
   - Fuel vs electric breakdown
   - Cost per trip
   - Tap to see detailed trip stats

3. **Statistics** — Aggregated data:
   - Weekly/Monthly/All-time charts
   - Total distance, fuel, electricity
   - Average efficiency trends
   - EV vs Hybrid vs Fuel mode breakdown (pie chart)
   - Cost savings from electric driving

4. **Settings** — Fuel price, electricity price per kWh, units

### DM-i Hybrid Logic
The BYD DM-i system operates in 3 modes:
- **EV Mode**: Battery SOC > threshold, engine RPM = 0
- **Hybrid Mode**: Engine running + battery assisting
- **Fuel Mode**: Engine running, battery not charging

Detection logic: Check engine RPM (010C) and battery current (220009):
- RPM = 0 → EV mode
- RPM > 0 && current < 0 (discharging) → Hybrid mode
- RPM > 0 && current >= 0 → Fuel mode

## App 3: Quick Controls Panel (:app-controls)

### Features
- **Floating overlay** (uses SYSTEM_ALERT_WINDOW permission)
- **Collapsible**: Small floating button that expands to a grid
- **Preset Buttons:**
  - Screen brightness: Day/Night/Auto
  - Quick call favorites (via intent)
  - Navigation shortcuts (Waze, Google Maps)
  - Music controls (play/pause/next/prev via MediaSession)
  - Timer/Stopwatch
  - Flashlight toggle
  - Volume presets (Media/Nav/Call)

### Implementation
- Foreground Service for overlay
- WindowManager for floating view
- Draggable position (saves last position)
- Compact mode (single row) vs expanded grid
- Uses Compose for overlay UI via ComposeView in WindowManager

## App 4: HUD Dashboard Overlay (:app-hud)

### Features
- Full-screen overlay optimized for heads-up viewing
- **Minimal mode**: Speed + Battery SOC only (2 large numbers)
- **Standard mode**: Speed + RPM + Battery + Coolant Temp + Drive Mode
- **Night mode**: Ultra-dim, red/amber only (preserves night vision)

### Layouts
1. **Minimal HUD:**
```
┌─────────────────────────┐
│                         │
│       87 km/h           │  ← Very large, center
│       ██████░░ 72%      │  ← Battery bar + percentage
│                         │
│   EV Mode    18:45      │  ← Drive mode + clock
└─────────────────────────┘
```

2. **Standard HUD:**
```
┌─────────────────────────┐
│  RPM ◠◡ 1200   87 km/h │
│                         │
│  ⚡ 72%    🌡️ 45°C      │
│  ⛽ 85%    ⚙ Hybrid     │
│                         │
│  Range: 680km   18:45   │
└─────────────────────────┘
```

### Special Features
- Screen-on lock (KEEP_SCREEN_ON flag)
- Tap to cycle between minimal/standard/night
- Landscape orientation lock
- OBD polling optimized for minimal latency on speed reading
- Speed warning: color flash when exceeding configurable limit
- Large touch targets for mode switching while driving

## Build Configuration Notes
- Each app module produces its own APK
- Shared modules are Android libraries
- ProGuard/R8 for release builds
- Bluetooth permissions: BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_CONNECT, BLUETOOTH_SCAN, ACCESS_FINE_LOCATION
- Overlay permission: SYSTEM_ALERT_WINDOW (for controls and HUD)
- Foreground service permission for persistent overlay
