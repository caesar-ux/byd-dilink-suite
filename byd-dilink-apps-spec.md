# BYD DiLink Head Unit Apps — Full Specification

## Overview
4 standalone Android apps designed to run DIRECTLY on the BYD Destroyer 05 (2025) DiLink infotainment head unit. No phone, no OBD adapter, no internet required. Sideloaded via USB.

**Target Device:** BYD DiLink (Qualcomm QCM6125), 12.8" rotatable screen (portrait 1920x1080, landscape 1080x1920)
**Tech Stack:** Kotlin, Jetpack Compose, Material 3, minSDK 28, targetSDK 34
**Constraints:**
- NO Google Play Services (DiLink Chinese version doesn't have them)
- NO internet dependency (everything works offline)
- GPS available (built into head unit)
- USB storage accessible (for media files)
- Large touch targets (driving context, min 56dp)
- Must support BOTH portrait and landscape (screen rotates)
- Dark theme by default (reduce driver distraction)

## Project Structure
```
byd-dilink-suite/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   └── libs.versions.toml
├── core/
│   ├── ui/                       # :core:ui — Shared theme, components
│   └── data/                     # :core:data — Room DB, preferences
├── app-parking/                  # :app-parking — Parking Assistant
├── app-dashboard/                # :app-dashboard — Drive Mode Dashboard
├── app-maintenance/              # :app-maintenance — Maintenance Tracker
└── app-media/                    # :app-media — Offline Media Player
```

## Module: core:ui — DiLink Design System

### Color Palette (Dark-first, automotive)
```kotlin
// Primary accent: Electric cyan (BYD-inspired)
val DiLinkCyan = Color(0xFF00BCD4)
val DiLinkCyanDark = Color(0xFF0097A7)
val DiLinkCyanLight = Color(0xFFB2EBF2)

// Background: Deep dark for OLED-like appearance
val DiLinkBackground = Color(0xFF0A0A0A)
val DiLinkSurface = Color(0xFF1C1C1C)
val DiLinkSurfaceVariant = Color(0xFF2A2A2A)
val DiLinkSurfaceElevated = Color(0xFF333333)

// Text
val DiLinkTextPrimary = Color(0xFFEEEEEE)
val DiLinkTextSecondary = Color(0xFFAAAAAA)
val DiLinkTextMuted = Color(0xFF666666)

// Status
val StatusGreen = Color(0xFF4CAF50)
val StatusYellow = Color(0xFFFFC107)
val StatusRed = Color(0xFFFF5252)
val StatusOrange = Color(0xFFFF9800)
val StatusBlue = Color(0xFF42A5F5)

// Accent palette for variety across apps
val ParkingBlue = Color(0xFF2196F3)
val DashboardGreen = Color(0xFF66BB6A)
val MaintenanceAmber = Color(0xFFFFB300)
val MediaPurple = Color(0xFFAB47BC)
```

### Typography
- System sans-serif only (no custom fonts — head unit may not have them)
- Extra large titles (28-36sp) for readability at arm's length
- Body text minimum 18sp
- Monospace for numbers (speed, time, distance)

### Shared Components
- `LargeIconButton` — 72dp+ touch target button with icon + label, ripple feedback
- `MetricDisplay` — Large number + unit + label (for dashboard/stats)
- `SectionCard` — Elevated card with header and content
- `ConfirmDialog` — Large-button confirmation dialog for driving safety
- `AdaptiveLayout` — Detects portrait vs landscape and switches layout accordingly
- `TopBarWithBack` — Simple top bar with back arrow and title

## Module: core:data — Shared Storage

### Room Database (shared across apps)
Database name: "byd_dilink_db"

### DataStore
- Shared preference keys per app (prefixed)
- Unit preferences (km/mi, L/gal, °C/°F)

---

## App 1: Parking Assistant (:app-parking)

### Features
Helps you remember where you parked and manage parking time.

### Screens

#### 1. Main / Park Now Screen
- Big "SAVE PARKING" button (full-width, 80dp height)
- When tapped: saves current GPS coordinates + timestamp
- Shows currently saved parking:
  - Address (reverse geocoded if possible, otherwise raw lat/lon)
  - Time parked
  - Distance from current location
  - Compass direction to car

#### 2. Active Parking View
- **Timer**: Elapsed time since parking (large, center)
- **Parking Timer**: Optional countdown (for metered parking) — set 30min/1h/2h/custom
- **Alarm**: Sound alert 5 minutes before parking expires
- **Compass**: Arrow pointing toward saved car location
  - Uses device magnetic sensor + GPS
  - Shows distance in meters/km
- **Map placeholder**: Simple Canvas-based relative position indicator (no Google Maps needed)
  - Shows car as a pin, you as a dot, with a line between them
  - Rotates based on compass heading
- **Navigate**: Button opens any installed navigation app with car coordinates via geo: intent
- **Clear Parking**: Removes saved location (with confirmation)

#### 3. Favorite Locations
- List of saved favorite spots (home, office, mall, etc.)
- Each entry: name, GPS coordinates, notes
- Add/Edit/Delete
- "Navigate To" button for each

#### 4. Parking History
- List of all past parkings (last 100)
- Date, time, duration, location
- Swipe to delete individual entries

### Data Model
```kotlin
@Entity
data class ParkingRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val parkedAt: Long,           // timestamp
    val clearedAt: Long?,         // when user "found" the car
    val timerDurationMin: Int?,   // parking meter duration, null if none
    val notes: String?
)

@Entity
data class FavoriteLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String?
)
```

### Permissions
- ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION

---

## App 2: Drive Mode Dashboard (:app-dashboard)

### Features
A custom digital dashboard that uses GPS sensors (no OBD needed).

### Screen Layout — Landscape Mode:
```
┌────────────────────────────────────────────┐
│  GPS Speed        │  Heading Compass       │
│   ╭───────╮       │     N                  │
│   │  87   │       │  W  ◉  E              │
│   │ km/h  │       │     S                  │
│   ╰───────╯       │  Bearing: 247° WSW    │
│───────────────────┤────────────────────────│
│ Trip A             │ Trip B                │
│ Dist: 45.3 km     │ Dist: 1,247.8 km      │
│ Time: 0:42:15     │ Time: 18:35:12        │
│ Avg:  64.7 km/h   │ Avg:  67.2 km/h       │
│ Max:  127 km/h    │ Max:  135 km/h        │
│───────────────────┴────────────────────────│
│ Alt: 342m │ Sat: 12 │ Acc: ±3m │ G: 0.2g  │
└────────────────────────────────────────────┘
```

### Screen Layout — Portrait Mode:
```
┌──────────────────────┐
│    GPS Speed         │
│    ╭─────────╮       │
│    │   87    │       │
│    │  km/h   │       │
│    ╰─────────╯       │
│    Max: 127 km/h     │
│──────────────────────│
│    Compass           │
│       N              │
│    W  ◉  E          │
│       S              │
│   247° WSW           │
│──────────────────────│
│  Trip A  │  Trip B   │
│  45.3km  │  1247km   │
│  0:42:15 │  18:35    │
│  64.7avg │  67.2avg  │
│──────────────────────│
│ Alt:342m  Sat:12     │
│ Acc:±3m   G:0.2g     │
└──────────────────────┘
```

### Features
- **GPS Speedometer**: Large, center, real-time from LocationManager (updates every 1 second)
  - Digital display with monospace font
  - Max speed tracking (resettable)
  - Speed unit toggle: km/h or mph
  - Color coding: green < 60, white 60-120, yellow 120-140, red > 140

- **Compass**: 
  - Circular compass rose drawn with Canvas
  - Current heading from GPS bearing (when moving) or SensorManager (TYPE_ROTATION_VECTOR) when stationary
  - Cardinal direction label (N, NE, E, SE, S, SW, W, NW)

- **Dual Trip Computers** (A and B):
  - Distance (accumulated from GPS position changes)
  - Elapsed time
  - Average speed
  - Maximum speed
  - Individual reset buttons for each trip

- **Altitude**: From GPS provider (meters above sea level)

- **Satellite Info**: Number of GPS satellites in fix

- **GPS Accuracy**: Reported accuracy in meters

- **G-Force Meter**:
  - Uses accelerometer (SensorManager, TYPE_ACCELEROMETER)
  - Shows lateral + longitudinal G-force
  - Small real-time graph (Canvas-based, last 30 seconds)
  - Peak G-force tracking

### Settings
- Speed unit (km/h / mph)
- Distance unit (km / mi)
- Altitude unit (m / ft)
- Speed warning threshold
- Keep screen on toggle
- GPS update interval (1s / 2s / 5s)

### Data Model
```kotlin
data class TripData(
    val distanceKm: Double = 0.0,
    val elapsedMs: Long = 0L,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val isRunning: Boolean = false
)

data class DashboardState(
    val speedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val heading: Float = 0f,
    val altitude: Double = 0.0,
    val satellites: Int = 0,
    val accuracy: Float = 0f,
    val gForceX: Float = 0f,   // lateral
    val gForceY: Float = 0f,   // longitudinal
    val peakG: Float = 0f,
    val tripA: TripData = TripData(),
    val tripB: TripData = TripData()
)
```

### Permissions
- ACCESS_FINE_LOCATION

---

## App 3: Maintenance Tracker (:app-maintenance)

### Features
Track all vehicle maintenance items with km-based and date-based reminders.

### Screens

#### 1. Overview Dashboard
- Grid/List of maintenance categories, each showing:
  - Icon + Name
  - Last service: date + km
  - Next due: date or km (whichever comes first)
  - Status: ✅ OK (green), ⚠️ Soon (yellow <500km or <30 days), 🔴 Overdue (red)
- Summary card at top: "X items need attention"

#### 2. Maintenance Categories (pre-configured):
| Category | Default Interval |
|----------|-----------------|
| Engine Oil | Every 7,500 km or 6 months |
| Oil Filter | Every 7,500 km or 6 months |
| Air Filter | Every 15,000 km or 12 months |
| Cabin Air Filter | Every 15,000 km or 12 months |
| Brake Fluid | Every 40,000 km or 2 years |
| Brake Pads (Front) | Every 30,000 km |
| Brake Pads (Rear) | Every 50,000 km |
| Coolant | Every 40,000 km or 2 years |
| Transmission Fluid | Every 60,000 km or 4 years |
| Spark Plugs | Every 30,000 km or 2 years |
| Tire Rotation | Every 10,000 km or 6 months |
| Battery (12V) | Every 3 years |
| HV Battery Check | Every 12 months |
| Wiper Blades | Every 12 months |
| Wheel Alignment | Every 20,000 km or 12 months |

Users can add custom categories too.

#### 3. Service Log
- For each category: list of all past services
- Each entry: date performed, km at service, cost (optional), shop name (optional), notes
- Add new service entry form

#### 4. Add/Edit Service Entry
- Select category (dropdown)
- Date (date picker, defaults to today)
- Current odometer (km) — manual entry with numeric keyboard
- Cost (optional, in local currency)
- Shop/Mechanic name (optional)
- Notes (optional)
- Save button

#### 5. Vehicle Profile
- Vehicle name: "BYD Destroyer 05"
- Year: 2025
- Current odometer (manually updated — used as baseline for "next due" calculations)
- VIN (optional)
- License plate (optional)
- Purchase date

#### 6. Reminders / Alerts
- When app opens, check all items
- Items within 500km or 30 days of due: yellow warning
- Overdue items: red alert card at top of overview
- Optional: set notification reminder (local notification, no internet)

### Data Model
```kotlin
@Entity
data class MaintenanceCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,       // material icon name
    val intervalKm: Int?,       // null if date-only
    val intervalMonths: Int?,   // null if km-only
    val isCustom: Boolean = false,
    val sortOrder: Int = 0
)

@Entity
data class ServiceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val datePerformed: Long,    // timestamp
    val odometerKm: Int,
    val cost: Double?,
    val shopName: String?,
    val notes: String?
)

@Entity
data class VehicleProfile(
    @PrimaryKey val id: Int = 1,  // single vehicle
    val name: String = "BYD Destroyer 05",
    val year: Int = 2025,
    val currentOdometerKm: Int = 0,
    val lastOdometerUpdate: Long = 0,
    val vin: String? = null,
    val licensePlate: String? = null,
    val purchaseDate: Long? = null
)
```

### Permissions
- None required (fully local)

---

## App 4: Offline Media Player (:app-media)

### Features
Music and video player designed for the car head unit. Reads from USB drives or internal storage.

### Screens

#### 1. Now Playing (Main Screen)
**Landscape Layout:**
```
┌──────────────────────────────────────────┐
│ ◄ ▌▌ ► 🔀 🔁   Vol ████████░░  🔊     │
│──────────────────────────────────────────│
│                                          │
│   ♪ Song Title                          │
│   Artist Name — Album Name              │
│                                          │
│  ─────●──────────────────────  3:24/5:01│
│                                          │
│  ◄◄     ▌▌     ►►     │  Queue (3/15)  │
│                                          │
└──────────────────────────────────────────┘
```

- Large playback controls (prev, play/pause, next) — min 72dp buttons
- Seek bar with current/total time
- Volume control bar
- Shuffle and repeat toggles
- Song info: title, artist, album (from metadata)
- Album art if available (extracted from file)
- Queue indicator showing position in playlist

#### 2. File Browser
- Browse storage volumes (internal, USB drives)
- Folder navigation with breadcrumb trail
- File list showing:
  - Audio files: .mp3, .flac, .wav, .ogg, .m4a, .aac, .wma
  - Video files: .mp4, .mkv, .avi, .mov, .webm
  - Folders (with file count inside)
- Tap file to play immediately
- Long-press to add to queue
- "Play All" button for current folder
- Sort by: name, date, size

#### 3. Queue / Playlist
- Current playback queue
- Drag to reorder
- Swipe to remove
- Clear all button
- Save queue as playlist (name it)
- Load saved playlist

#### 4. Video Player
- Full-screen video playback
- On-screen controls (tap to show/hide):
  - Play/pause, seek bar, volume
  - 10s skip forward/backward buttons
  - Aspect ratio toggle (fit/fill/16:9/4:3)
  - Brightness slider (left side gesture)
  - Volume slider (right side gesture)
- Landscape orientation for video

#### 5. Equalizer
- 5-band graphic EQ with sliders
- Presets: Flat, Rock, Pop, Jazz, Classical, Bass Boost, Vocal, Custom
- Bass boost toggle
- Virtualizer toggle
- Save custom preset

#### 6. Settings
- Default browse path
- Resume playback on launch
- Audio focus handling (pause when nav speaks)
- File types to show/hide
- Equalizer enable/disable

### Technical Implementation
- Uses Android MediaPlayer or ExoPlayer (bundled, no Google dependency) for playback
- MediaMetadataRetriever for extracting song info + album art
- StorageVolume API for detecting USB drives
- AudioManager for volume control
- AudioEffect / Equalizer / BassBoost / Virtualizer for EQ
- Foreground service for background playback
- MediaSession for hardware button support (steering wheel controls)
- No streaming, no internet, fully offline

### Data Model
```kotlin
@Entity
data class PlaylistEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistName: String,
    val filePath: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val sortOrder: Int
)

data class MediaFile(
    val path: String,
    val name: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val albumArt: Bitmap?,
    val isVideo: Boolean,
    val sizeBytes: Long,
    val lastModified: Long
)

data class EqPreset(
    val name: String,
    val bands: List<Int>,  // 5 bands, values in millibels
    val bassBoost: Int,
    val virtualizer: Int
)
```

### Permissions
- READ_EXTERNAL_STORAGE (or READ_MEDIA_AUDIO + READ_MEDIA_VIDEO for API 33+)
- FOREGROUND_SERVICE (for background playback)
- WAKE_LOCK

---

## Build Notes
- Each app is a separate APK (sideloaded individually via USB "Third Party Apps" folder)
- No Google Play Services dependencies anywhere
- No internet permissions (except optional for reverse geocoding in parking app — make it optional/graceful)
- All data stored locally in Room + DataStore
- Support both orientations (portrait 1080x1920 and landscape 1920x1080) using adaptive layouts
- Dark theme mandatory — no light theme option (driver distraction)
- Minimum touch target: 56dp everywhere
- Use system NavigationBar handling compatible with DiLink (may not have standard Android nav bar)
