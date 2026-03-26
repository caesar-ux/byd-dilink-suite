# Build Summary: app-controls + app-hud Modules

## :app-controls — Quick Controls Panel (13 source files)

### Architecture
- **ControlsOverlayService** — Foreground service using WindowManager to render a ComposeView overlay
- Implements LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner for Compose-in-Service support
- Hilt dependency injection via @AndroidEntryPoint

### Files Created
| File | Description | Lines |
|------|-------------|-------|
| `ControlsApplication.kt` | Hilt Application class | 8 |
| `ControlsOverlayService.kt` | Foreground service with WindowManager overlay, notification, position persistence | 277 |
| `FloatingControlsView.kt` | Full Compose UI: FloatingButton (draggable), CompactPanel (single row), ExpandedPanel (grid) | 486 |
| `ControlsActivity.kt` | Main activity: overlay permission request, service toggle, Compose Navigation to settings | 344 |
| `SettingsScreen.kt` | Configure favorite number, default nav app, volume sliders | 261 |
| `ControlsViewModel.kt` | MVVM state: combines settings + local + timer flows with nested combine() | 346 |
| `ControlsSettingsRepository.kt` | DataStore persistence: favorite number, nav app, volumes, overlay position, compact mode | 111 |
| `BrightnessHelper.kt` | System brightness control: Day (255), Night (51), Auto modes | 106 |
| `VolumeHelper.kt` | AudioManager: media/nav/call volume control with presets | 108 |
| `MediaControlHelper.kt` | MediaSession/KeyEvent dispatch: play/pause, next, previous, now-playing info | 125 |
| `IntentUtils.kt` | Launch Waze, Google Maps, dialer, Play Store; flashlight toggle via CameraManager | 103 |
| `TimerManager.kt` | Countdown timer with StateFlow, preset cycling (1/3/5/10/15/30 min) | 106 |
| `ControlsModule.kt` | Hilt DI module providing all singletons | 58 |

### Permissions
- SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE
- POST_NOTIFICATIONS, CAMERA, FLASHLIGHT, WRITE_SETTINGS, CALL_PHONE

### Key Features
- Draggable overlay saves position to DataStore on every move
- Compact mode (4 buttons) vs expanded grid (all controls)
- Spring animations for expand/collapse
- Foreground notification with "Stop" action
- Runtime overlay permission check with guided Settings redirect

---

## :app-hud — HUD Dashboard Overlay (10 source files)

### Architecture
- **HudActivity** — Full-screen immersive Activity, landscape locked, KEEP_SCREEN_ON
- Three display modes cycled by tap; double-tap opens settings overlay
- **ObdDataProvider** bridges core:obd with demo simulation for standalone testing

### Files Created
| File | Description | Lines |
|------|-------------|-------|
| `HudApplication.kt` | Hilt Application class | 8 |
| `HudActivity.kt` | Full-screen immersive Activity with tap/double-tap gesture handling | 140 |
| `MinimalHudScreen.kt` | Large speed (120sp monospace), battery SOC bar, drive mode, clock | 161 |
| `StandardHudScreen.kt` | RPM arc gauge + speed + battery SOC + coolant + fuel + drive mode + range + clock | 379 |
| `NightHudScreen.kt` | Red/amber night vision palette, 0.85 alpha dimming, extra info row | 208 |
| `SpeedWarningOverlay.kt` | Flashing red border (500ms cycle) using infinite transition animation | 53 |
| `HudSettingsScreen.kt` | Speed warning slider (60-250 km/h), mode selector, auto-dim toggle | 234 |
| `HudViewModel.kt` | Combines OBD data + local state + settings; range estimation; clock updates | 237 |
| `ObdDataProvider.kt` | Priority-based OBD polling (speed@250ms, others@1s); demo simulation | 208 |
| `HudSettingsRepository.kt` | DataStore: speed warning limit, default mode, auto-dim, units | 98 |
| `HudModule.kt` | Hilt DI module | 31 |

### Permissions
- Bluetooth (BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_CONNECT, BLUETOOTH_SCAN)
- ACCESS_FINE_LOCATION, WAKE_LOCK, WRITE_SETTINGS

### Key Features
- Tabular monospace numbers prevent digit jumping
- Pure black (#000000) background for OLED efficiency
- Night mode uses only red (#FF1744) and amber (#FF6F00) to preserve scotopic vision
- Speed warning: configurable limit (default 120 km/h), flashing red border animation
- RPM arc gauge with color coding (teal/yellow/red based on RPM range)
- Drive mode detection: EV (cyan), Hybrid (green), Fuel (orange) using RPM + battery current
- Range estimation using BYD Destroyer 05 specs (18.3 kWh battery, 48L tank)
- Clock updated every second via coroutine

---

## Build Configuration
- Both modules use `com.android.application` plugin (produce independent APKs)
- Dependencies on `:core:model`, `:core:ui`, `:core:data` (app-controls), plus `:core:obd` (app-hud)
- Compose compiler extension version from version catalog
- R8/ProGuard enabled for release builds
- Hilt + KSP for dependency injection

## Fixes Applied
- Fixed `settings.gradle.kts`: `dependencyResolution` → `dependencyResolutionManagement`
- Used nested `combine()` calls (max 5 params each) instead of single 14-param combine to avoid type-unsafe vararg overload
