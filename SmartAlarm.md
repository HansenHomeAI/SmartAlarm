# SmartAlarm: Dedicated Bedside Alarm Clock using a Samsung A54

## Architecture Overview

**Tech Stack:**

- Language: Kotlin
- Build: Gradle 8.x with Android Gradle Plugin 8.x
- Target: API 33 (Android 13) primary, API 34 (Android 14) compatible
- Testing: AVD API 34 + Samsung A54 physical device
- TTS: SherpaTTS primary, Android TextToSpeech fallback
- Database: Room for to-do list persistence
- UI: Jetpack Compose for declarative landscape UI

## Phase 1: Development Environment Setup

### 1.1 Verify/Install Required Tools

**Files to create:** None (system setup only)

Verify installation on MacBook M3 Pro:

- OpenJDK 17 ARM64 (via Homebrew: `brew install openjdk@17`)
- Android Platform Tools (via Homebrew: `brew install android-platform-tools`)
- Android SDK with API 33 & 34
- AVD Manager with Samsung A54-like profile (API 34, 1080x2340, arm64-v8a)

### 1.2 Initialize Gradle Project

**Files to create:**

- `build.gradle.kts` (project-level)
- `app/build.gradle.kts` (app-level)
- `settings.gradle.kts`
- `gradle.properties`
- `gradle/wrapper/gradle-wrapper.properties`

Key configurations:

```kotlin
// app/build.gradle.kts highlights
android {
    compileSdk = 34
    defaultConfig {
        minSdk = 29  // Android 10 minimum for modern features
        targetSdk = 34
    }
    buildFeatures {
        compose = true
    }
}
dependencies {
    // Jetpack Compose for UI
    // Room for database
    // Coroutines for async operations
}
```

## Phase 2: Core Alarm Functionality

### 2.1 Alarm Scheduling System

**Files to create:**

- `app/src/main/java/com/smartalarm/alarm/AlarmManager.kt`
- `app/src/main/java/com/smartalarm/alarm/AlarmReceiver.kt`
- `app/src/main/java/com/smartalarm/alarm/AlarmScheduler.kt`

**Key implementation details:**

- Use `AlarmManager.setExactAndAllowWhileIdle()` for API 31+ exact alarm requirement
- Request `SCHEDULE_EXACT_ALARM` permission (runtime permission on API 33+)
- Handle Doze mode with `setAlarmClock()` for high-priority alarms
- Implement `BroadcastReceiver` to trigger alarm UI
- Store alarm time in SharedPreferences or Room database

### 2.2 Snooze Functionality

**Files to create:**

- `app/src/main/java/com/smartalarm/alarm/SnoozeManager.kt`

- Fixed 9-minute snooze interval
- Cancel original alarm, reschedule for current_time + 9 minutes

### 2.3 Alarm UI Activity

**Files to create:**

- `app/src/main/java/com/smartalarm/ui/AlarmTriggerActivity.kt`
- `app/src/main/java/com/smartalarm/ui/AlarmTriggerScreen.kt` (Compose)

**Critical lock screen bypass implementation:**

```kotlin
// In onCreate() of AlarmTriggerActivity
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
    setShowWhenLocked(true)
    setTurnScreenOn(true)
    window.addFlags(
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
    )
    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    keyguardManager.requestDismissKeyguard(this, null)
} else {
    window.addFlags(
        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
    )
}
```

**Manifest permissions:**

```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
```

## Phase 3: To-Do List with TTS

### 3.1 To-Do Data Layer

**Files to create:**

- `app/src/main/java/com/smartalarm/data/TodoEntity.kt`
- `app/src/main/java/com/smartalarm/data/TodoDao.kt`
- `app/src/main/java/com/smartalarm/data/TodoDatabase.kt`
- `app/src/main/java/com/smartalarm/data/TodoRepository.kt`

Room database schema:

```kotlin
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAt: Long,
    val isCompleted: Boolean = false,
    val order: Int
)
```

### 3.2 SherpaTTS Integration (Primary)

**Files to create:**

- `app/src/main/java/com/smartalarm/tts/TtsEngine.kt` (interface)
- `app/src/main/java/com/smartalarm/tts/SherpaTtsEngine.kt`
- `app/src/main/java/com/smartalarm/tts/AndroidTtsEngine.kt` (fallback)
- `app/src/main/java/com/smartalarm/tts/TtsManager.kt`

**Integration approach:**

1. Add SherpaTTS dependency (if available via Maven/GitHub releases)
2. Bundle lightweight ONNX voice model (~10-20MB) in `assets/`
3. Initialize SherpaTTS with model path
4. Implement fallback detection: if SherpaTTS init fails, switch to Android TTS

**Dependencies to add:**

```kotlin
// Option 1: If SherpaTTS has published artifact
implementation("org.sherpaTTS:android:x.x.x")

// Option 2: Manual integration
// Download SherpaTTS AAR from F-Droid project
// Place in libs/ and add: implementation(files("libs/sherpa-tts.aar"))
```

**Fallback logic:**

```kotlin
class TtsManager(context: Context) {
    private val engine: TtsEngine = try {
        SherpaTtsEngine(context).also { 
            it.initialize() 
            Log.i("TTS", "Using SherpaTTS")
        }
    } catch (e: Exception) {
        Log.w("TTS", "SherpaTTS failed, using Android TTS", e)
        AndroidTtsEngine(context)
    }
}
```

### 3.3 To-Do UI

**Files to create:**

- `app/src/main/java/com/smartalarm/ui/TodoListScreen.kt` (Compose)
- `app/src/main/java/com/smartalarm/ui/TodoViewModel.kt`

- Add/edit/delete to-do items
- Button to "Read All Todos" using TTS
- Display in landscape orientation with large, red text

## Phase 4: Main Clock Display

### 4.1 Launcher Activity (Auto-Launch on Boot)

**Files to create:**

- `app/src/main/java/com/smartalarm/MainActivity.kt`
- `app/src/main/java/com/smartalarm/ui/ClockScreen.kt` (Compose)

**Manifest configuration:**

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:screenOrientation="landscape"
    android:theme="@style/Theme.SmartAlarm">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

**Key behaviors:**

- Lock to landscape orientation
- Full-screen immersive mode (hide status/nav bars)
- Red-on-black color scheme (RGB: #FF0000 text, #000000 background)
- Display current time in large font (inspired by Apple's bedside mode)
- Display next alarm time
- Quick access to: set alarm, view to-dos, settings

### 4.2 Red Pixel Design Implementation

**Files to create:**

- `app/src/main/java/com/smartalarm/ui/theme/Color.kt`
- `app/src/main/java/com/smartalarm/ui/theme/Theme.kt`
```kotlin
// Color.kt
val RedPrimary = Color(0xFFFF0000)
val BlackBackground = Color(0xFF000000)
val RedSecondary = Color(0xFFCC0000)

// Theme.kt - Force dark theme with red accents
private val SmartAlarmColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = BlackBackground,
    background = BlackBackground,
    onBackground = RedPrimary,
    surface = BlackBackground,
    onSurface = RedPrimary
)
```


### 4.3 Immersive Mode & Screen Management

**Implementation in MainActivity (UPDATED for modern API):**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Keep screen on always
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    
    // Force landscape orientation
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    
    // Modern immersive mode (API 30+) - supports Samsung gestures
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.apply {
            hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        // Fallback for API 29
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
    }
}
```

**Critical Note:** Samsung gestures work normally with `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` - users can swipe from edges to access navigation, then bars auto-hide.

### 4.4 10-Second Auto-Dim with Tap-to-Wake

**Files to create:**

- `app/src/main/java/com/smartalarm/ui/ScreenDimManager.kt`

**Implementation approach:**

```kotlin
class ScreenDimManager(private val activity: Activity) {
    private val handler = Handler(Looper.getMainLooper())
    private var isDimmed = false
    
    private val dimRunnable = Runnable {
        // Reduce brightness to near-zero
        activity.window.attributes = activity.window.attributes.apply {
            screenBrightness = 0.01f
        }
        isDimmed = true
    }
    
    fun resetTimer() {
        handler.removeCallbacks(dimRunnable)
        if (isDimmed) {
            // Restore brightness
            activity.window.attributes = activity.window.attributes.apply {
                screenBrightness = 0.3f // Moderate brightness
            }
            isDimmed = false
        }
        handler.postDelayed(dimRunnable, 10_000) // 10 seconds
    }
    
    fun cleanup() {
        handler.removeCallbacks(dimRunnable)
    }
}

// In Compose UI:
Box(modifier = Modifier
    .fillMaxSize()
    .pointerInput(Unit) {
        detectTapGestures { screenDimManager.resetTimer() }
    }
) {
    // Clock UI content
}
```

### 4.5 Burn-In Prevention

**Files to create:**

- `app/src/main/java/com/smartalarm/ui/BurnInPreventionManager.kt`

**Strategy:**

- Shift clock position by ±5 pixels every 5 minutes
- Use `offset` modifier in Compose
- Only shift when screen is not dimmed
- Subtle movement prevents static image retention on OLED

## Phase 5: Boot & Background Services

### 5.1 Boot Receiver

**Files to create:**

- `app/src/main/java/com/smartalarm/BootReceiver.kt`
```kotlin
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Launch MainActivity
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launchIntent)
        }
    }
}
```


**Manifest registration:**

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<receiver
    android:name=".BootReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

**Samsung-specific requirement:**

- Guide user to disable battery optimization for SmartAlarm
- Settings → Apps → SmartAlarm → Battery → Unrestricted
- Without this, Samsung One UI may block BOOT_COMPLETED

### 5.2 Foreground Service (Optional)

**Purpose:** Keep app alive and ensure alarm triggers reliably

**Files to create:**

- `app/src/main/java/com/smartalarm/AlarmForegroundService.kt`

- Start foreground service from MainActivity
- Display persistent notification (required by Android)
- Use minimal notification with red icon for consistency

## Phase 6: Settings & Configuration

### 6.1 Settings Screen

**Files to create:**

- `app/src/main/java/com/smartalarm/ui/SettingsScreen.kt`

**Settings to include:**

- Alarm time picker
- Alarm sound selection
- TTS voice selection (if multiple available)
- Screen brightness level (constant vs adaptive)
- Auto-dim timeout (default 10s, adjustable 5-30s)
- Battery optimization prompt
- Exit to system launcher button

### 6.2 First-Run Setup

**Files to create:**

- `app/src/main/java/com/smartalarm/ui/OnboardingScreen.kt`

**Onboarding flow:**

1. Welcome screen explaining the app purpose
2. Request permissions (SCHEDULE_EXACT_ALARM, POST_NOTIFICATIONS, etc.)
3. Prompt to set as default launcher
4. Guide to disable battery optimization
5. Set initial alarm time

## Phase 7: Testing Strategy

### 7.1 Emulator Testing (AVD API 34)

- Test core alarm scheduling
- Test UI in landscape mode
- Test TTS functionality
- Test immersive mode and screen dimming
- Test boot receiver (restart emulator)

### 7.2 Samsung A54 Physical Device Testing

**Critical tests:**

1. **Lock screen bypass:** Verify alarm shows over lock screen
2. **Boot auto-launch:** Restart phone, confirm app launches
3. **Battery optimization:** Test with/without exemption
4. **SherpaTTS:** Confirm voice quality and performance
5. **10-second dim:** Verify timing and tap-to-wake
6. **Burn-in prevention:** Run overnight, check pixel shift
7. **Red-only pixels:** Use screen capture to verify no blue/green channels
8. **One UI compatibility:** Check for Samsung-specific UI conflicts

### 7.3 Long-Term Testing

- Run for 7+ consecutive days
- Monitor battery drain
- Check for memory leaks
- Verify alarm reliability (set multiple test alarms)
- Check OLED screen for burn-in signs

## Phase 8: Build & Deployment

### 8.1 APK Generation

```bash
# Debug build for testing
./gradlew assembleDebug

# Install to connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Release build (for final deployment)
./gradlew assembleRelease
```

### 8.2 Sideloading to Samsung A54

```bash
# Enable USB debugging on A54
# Connect via USB
adb devices
adb install -r app-debug.apk
```

## Risk Mitigation Summary

### High-Risk Items & Mitigations

| Risk | Probability | Mitigation |

|------|-------------|------------|

| SherpaTTS integration fails | High (60%) | Android TTS fallback pre-built |

| Samsung blocks BOOT_COMPLETED | Medium (40%) | User guide for battery optimization |

| Lock screen bypass fails | Low (20%) | Use modern API (`setShowWhenLocked`) + physical device testing |

| Screen burn-in | Medium (30%) | Pixel shifting + auto-dim + user warnings |

| Battery drain | Medium (35%) | Aggressive dimming, measure actual usage |

### Success Criteria

- Alarm triggers reliably 99%+ of the time
- App auto-launches on boot (after user exempts from battery optimization)
- Display uses only red pixels (RGB values: R=255, G=0, B=0)
- TTS reads to-do items clearly (either SherpaTTS or Android TTS)
- Screen dims after 10 seconds, wakes on tap
- Battery lasts 2+ days with continuous use (A54's 5000mAh battery)

## Development Timeline Estimate

- **Phase 1-2:** 8-12 hours (setup + core alarm)
- **Phase 3:** 10-15 hours (to-do + TTS integration)
- **Phase 4:** 12-18 hours (main UI + screen management)
- **Phase 5:** 4-6 hours (boot receiver + services)
- **Phase 6:** 6-8 hours (settings + onboarding)
- **Phase 7:** 10-20 hours (testing + iteration)
- **Phase 8:** 2-4 hours (build + deployment)

**Total:** 52-83 hours of focused development

## Future Enhancements (Post-MVP)

1. **True Kiosk Mode:** Provision device as Device Owner via ADB
2. **Weather Display:** Integrate weather API for bedside info
3. **Smart Wake:** Gentle alarm during light sleep phase (requires sensors)
4. **Voice Commands:** Wake word detection for hands-free control
5. **Multi-Device Sync:** Sync to-dos across devices via cloud

## Technical Notes

- **Kotlin Coroutines:** Use for database operations and TTS
- **State Management:** Use ViewModel + StateFlow for reactive UI
- **Error Handling:** Comprehensive try-catch blocks around TTS and alarm scheduling
- **Logging:** Use Timber for structured logging during development
- **Code Organization:** Clean Architecture with separation of data/domain/presentation layers