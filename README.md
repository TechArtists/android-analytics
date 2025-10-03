# TAAnalytics for Android

Android port of the iOS TAAnalytics library - an opinionated analytics framework wrapper that abstracts underlying analytics platforms.

## Phase 1: Core Foundation ✅ COMPLETE

The simplest working implementation with basic event tracking.

## Phase 2: Advanced Core Features ✅ COMPLETE

Event buffering, app lifecycle tracking, and install detection are now implemented.

### Phase 1 + 2 Implemented Components

#### 1. **Models** (`taanalytics/src/main/java/agency/techartists/taanalytics/models/`)
- ✅ `AnalyticsParameterValue.kt` - Type-safe parameter values (String, Int, Long, Double, Float, Boolean)
- ✅ `EventAnalyticsModel.kt` - Type-safe event name wrapper with internal/external flag
- ✅ `UserPropertyAnalyticsModel.kt` - Type-safe user property name wrapper

#### 2. **Core Analytics** (`taanalytics/src/main/java/agency/techartists/taanalytics/core/`)
- ✅ `TAAnalytics.kt` - Main analytics class with:
  - `track(event, params, logCondition)` - Track events
  - `set(userProperty, value)` - Set user properties
  - `get(userProperty)` - Get user properties from local storage
  - SharedPreferences integration for persistence
  - **Phase 2**: Async adaptor initialization with timeout
  - **Phase 2**: First open detection and tracking
  - **Phase 2**: App version update detection
  - **Phase 2**: OS version update detection
  - **Phase 2**: Cold launch counting
- ✅ `TAAnalyticsConfig.kt` - Configuration with adaptors, prefixes, filters, and install properties
- ✅ `EventLogCondition.kt` - Enum for log frequency control
- ✅ **Phase 2**: `EventBuffer.kt` - Queue events until adaptors are ready
- ✅ **Phase 2**: `AppLifecycleObserver.kt` - Automatic APP_OPEN/APP_CLOSE tracking

#### 3. **Adaptor System** (`taanalytics/src/main/java/agency/techartists/taanalytics/adaptor/`)
- ✅ `AnalyticsAdaptor.kt` - Interface for analytics platform adaptors
- ✅ `LogcatAnalyticsAdaptor.kt` - Simple implementation that logs to Logcat

#### 4. **Constants** (`taanalytics/src/main/java/agency/techartists/taanalytics/constants/`)
- ✅ `DefaultConstants.kt` - Standard event and user property definitions

#### 5. **Utils** (`taanalytics/src/main/java/agency/techartists/taanalytics/utils/`)
- ✅ **Phase 2**: `InstallUserPropertiesCalculator.kt` - Calculate device info at first open

### Usage Example

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var analytics: TAAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configure TAAnalytics
        val config = TAAnalyticsConfig(
            analyticsVersion = "1.0",
            adaptors = listOf(LogcatAnalyticsAdaptor()),
            sharedPreferences = getSharedPreferences("TAAnalytics", MODE_PRIVATE)
        )

        // 2. Initialize analytics
        analytics = TAAnalytics(applicationContext, config)

        // 3. Start analytics
        // Phase 2: First open, app lifecycle, and version updates are tracked automatically!
        lifecycleScope.launch {
            analytics.start()
        }
    }

    private fun trackCustomEvent() {
        // Track custom event with parameters
        analytics.track(
            event = EventAnalyticsModel("button_clicked"),
            params = mapOf(
                "button_name" to "submit".toAnalyticsValue(),
                "count" to 1.toAnalyticsValue()
            )
        )
    }

    private fun setUserProperty() {
        // Set a user property
        analytics.set(
            userProperty = UserPropertyAnalyticsModel("user_tier"),
            value = "premium"
        )
    }
}
```

### Key Features

**Phase 1:**
✅ **Type-safe event tracking** - Compile-time guarantees for event and property names
✅ **Log conditions** - Control frequency: always, once per lifetime, once per session
✅ **Multi-adaptor support** - Send events to multiple analytics platforms simultaneously
✅ **Event/property prefixing** - Separate prefixes for internal vs manual events
✅ **Event filtering** - Conditionally send events based on custom logic
✅ **SharedPreferences persistence** - Store user properties and log conditions locally
✅ **Install type detection** - Automatically detect Play Store, Debug, or Release builds

**Phase 2:**
✅ **Event buffer** - Queue events during adaptor initialization, flush when ready
✅ **Async adaptor initialization** - Initialize adaptors in parallel with configurable timeout
✅ **App lifecycle tracking** - Automatic APP_OPEN/APP_CLOSE events via ProcessLifecycleOwner
✅ **First open detection** - Automatically track first app open ever
✅ **App version updates** - Detect and track version/build changes
✅ **OS version updates** - Detect and track OS version changes
✅ **Install user properties** - Capture device info at install time (date, version, OS, root status, UI theme)
✅ **Cold launch counting** - Track number of cold launches

### Standard Events Included

- `OUR_FIRST_OPEN` - First app open ever
- `UI_VIEW_SHOW` - Generic view shown
- `UI_BUTTON_TAP` - Generic button tap
- `APP_OPEN` / `APP_CLOSE` - App lifecycle
- `ERROR` / `ERROR_CORRECTED` - Error tracking
- `APP_VERSION_UPDATE` / `OS_VERSION_UPDATE` - Version changes
- `ENGAGEMENT` / `ENGAGEMENT_PRIMARY` - User engagement
- `ONBOARDING_ENTER` / `ONBOARDING_EXIT` - Onboarding flow
- `PAYWALL_*` - Paywall events
- `SUBSCRIPTION_*` - Subscription events

### Standard User Properties Included

- `ANALYTICS_VERSION` - Analytics schema version
- `INSTALL_DATE` / `INSTALL_VERSION` / `INSTALL_OS_VERSION` - Install-time data
- `INSTALL_IS_ROOTED` - Root detection at install
- `APP_COLD_LAUNCH_COUNT` / `APP_OPEN_COUNT` - Usage counters
- `SUBSCRIPTION` / `SUBSCRIPTION_INTRO_OFFER` - Subscription data
- `LAST_VIEW_SHOW` - Last viewed screen

## Running the Example

1. Open the project in Android Studio
2. Run the `example` module
3. Tap the buttons to track events
4. Check Logcat with filter `TAAnalytics` to see the logs

## Next Phases (Not Yet Implemented)

### Phase 3: UI Tracking & Compose Integration
- UI protocol extensions
- View analytics models
- Compose modifiers for automatic tracking
- Navigation integration

### Phase 4: Advanced Features
- Stuck UI detection
- User ID synchronization
- Permission tracking
- Enhanced error tracking

### Phase 5: External Adaptors & Testing
- Firebase Analytics adaptor
- Testing infrastructure
- Mock analytics
- Unit test support

## Architecture

```
TAAnalytics
    ├── Models (type-safe wrappers)
    ├── Core (main analytics class)
    ├── Adaptor (platform abstractions)
    │   └── LogcatAnalyticsAdaptor
    └── Constants (standard events/properties)
```

## Comparison with iOS Implementation

| Feature | iOS | Android |
|---------|-----|---------|
| Basic event tracking | ✅ | ✅ Phase 1 |
| User properties | ✅ | ✅ Phase 1 |
| Log conditions | ✅ | ✅ Phase 1 |
| Multi-adaptor support | ✅ | ✅ Phase 1 |
| Event/property prefixing | ✅ | ✅ Phase 1 |
| Event filtering | ✅ | ✅ Phase 1 |
| Event buffer | ✅ | ✅ Phase 2 |
| App lifecycle tracking | ✅ | ✅ Phase 2 |
| Install detection | ✅ | ✅ Phase 2 |
| App/OS version updates | ✅ | ✅ Phase 2 |
| Install user properties | ✅ | ✅ Phase 2 |
| UI tracking | ✅ | ⏳ Phase 3 |
| Stuck UI detection | ✅ | ⏳ Phase 4 |
| Firebase adaptor | ✅ | ⏳ Phase 5 |

## License

MIT License - Copyright (c) 2024 Tech Artists Agency SRL
