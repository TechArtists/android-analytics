# TAAnalytics for Android

Android port of the iOS TAAnalytics library - an opinionated analytics framework wrapper that abstracts underlying analytics platforms.

## Phase 1: Core Foundation ✅ COMPLETE

The simplest working implementation with basic event tracking.

## Phase 2: Advanced Core Features ✅ COMPLETE

Event buffering, app lifecycle tracking, and install detection are now implemented.

## Phase 3: UI Tracking & Compose Integration ✅ COMPLETE

Automatic UI event tracking with Jetpack Compose support.

## Phase 4: Advanced Features & Error Tracking ✅ COMPLETE

Stuck UI detection, error tracking, user ID synchronization, and permission tracking helpers.

## Phase 5: Testing Infrastructure ✅ COMPLETE

Mock analytics, test adaptors, and comprehensive testing documentation.

### Phase 1 + 2 + 3 + 4 + 5 Implemented Components

#### 1. **Models** (`taanalytics/src/main/java/agency/techartists/taanalytics/models/`)
- ✅ `AnalyticsParameterValue.kt` - Type-safe parameter values (String, Int, Long, Double, Float, Boolean)
- ✅ `EventAnalyticsModel.kt` - Type-safe event name wrapper with internal/external flag
- ✅ `UserPropertyAnalyticsModel.kt` - Type-safe user property name wrapper
- ✅ **Phase 3**: `ViewAnalyticsModel.kt` - View/screen tracking with funnel support
- ✅ **Phase 3**: `SecondaryViewAnalyticsModel.kt` - Transient UI elements (dialogs, popups)

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
  - **Phase 4**: Stuck UI manager for timeout detection
- ✅ `TAAnalyticsConfig.kt` - Configuration with adaptors, prefixes, filters, and install properties
- ✅ `EventLogCondition.kt` - Enum for log frequency control
- ✅ **Phase 2**: `EventBuffer.kt` - Queue events until adaptors are ready
- ✅ **Phase 2**: `AppLifecycleObserver.kt` - Automatic APP_OPEN/APP_CLOSE tracking
- ✅ **Phase 3**: `TAAnalytics+UI.kt` - UI tracking extension (view show, button tap, funnel tracking)
- ✅ **Phase 4**: `TAAnalytics+Error.kt` - Error tracking extension
- ✅ **Phase 4**: `TAAnalytics+Permission.kt` - Permission tracking helpers
- ✅ **Phase 4**: `TAAnalytics+UserIDs.kt` - User ID synchronization
- ✅ **Phase 4**: `StuckUIManager.kt` - Stuck UI detection with correction tracking

#### 3. **Adaptor System** (`taanalytics/src/main/java/agency/techartists/taanalytics/adaptor/`)
- ✅ `AnalyticsAdaptor.kt` - Interface for analytics platform adaptors
- ✅ **Phase 4**: Optional adaptor protocols for user ID support:
  - `AnalyticsAdaptorWithReadOnlyUserPseudoID` - Read platform-generated ID
  - `AnalyticsAdaptorWithWriteOnlyUserID` - Set user ID (write-only)
  - `AnalyticsAdaptorWithReadWriteUserID` - Both read and write user ID
- ✅ `LogcatAnalyticsAdaptor.kt` - Simple implementation that logs to Logcat

#### 4. **Constants** (`taanalytics/src/main/java/agency/techartists/taanalytics/constants/`)
- ✅ `DefaultConstants.kt` - Standard event and user property definitions

#### 5. **Utils** (`taanalytics/src/main/java/agency/techartists/taanalytics/utils/`)
- ✅ **Phase 2**: `InstallUserPropertiesCalculator.kt` - Calculate device info at first open

#### 6. **Compose Integration** (`taanalytics/src/main/java/agency/techartists/taanalytics/compose/`)
- ✅ **Phase 3**: `AnalyticsModifiers.kt` - Jetpack Compose modifiers and helpers:
  - `Modifier.trackViewShow()` - Automatic view show tracking
  - `trackButtonTap()` - Helper function for button tap tracking
  - `Modifier.onFirstComposition()` - Run action on first composition

#### 7. **Testing** (`taanalytics/src/test/java/agency/techartists/taanalytics/`)
- ✅ **Phase 5**: `mock/MockTAAnalytics.kt` - Lightweight mock for unit testing
- ✅ **Phase 5**: `mock/TestAnalyticsAdaptor.kt` - Test adaptor for event verification
- ✅ **Phase 5**: `TESTING.md` - Comprehensive testing guide with examples

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

**Phase 3:**
✅ **View tracking** - ViewAnalyticsModel with funnel step support
✅ **Secondary views** - Track transient UI (dialogs, popups, tooltips)
✅ **Button tap tracking** - Context-aware button tracking with view info
✅ **Compose modifiers** - Automatic tracking via Jetpack Compose modifiers
✅ **Funnel tracking** - Multi-step user flow tracking with optional/final step flags

**Phase 4:**
✅ **Error tracking** - trackErrorEvent() and trackErrorCorrected() with exception details
✅ **Stuck UI detection** - Automatic timeout detection with correction tracking
✅ **User ID sync** - userID and userPseudoID properties synced across adaptors
✅ **Permission tracking** - Standardized permission request tracking helpers

**Phase 5:**
✅ **Mock analytics** - MockTAAnalytics for simple unit testing
✅ **Test adaptor** - TestAnalyticsAdaptor for integration testing
✅ **Testing guide** - Comprehensive TESTING.md with examples
✅ **Test dependencies** - JUnit, Coroutines Test configured

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

## Phase 3 Usage Examples

### View Tracking with Compose

```kotlin
// Define a view model
val homeView = ViewAnalyticsModel("home", type = "main")

// Track view show automatically
Column(
    modifier = Modifier.trackViewShow(analytics, homeView)
) {
    // Your UI content
}

// Track funnel steps
val onboardingStep1 = ViewAnalyticsModel(
    name = "onboarding_step_1",
    type = "onboarding",
    funnelStep = AnalyticsViewFunnelStepDetails(
        funnelName = "onboarding",
        step = 1,
        isOptionalStep = false,
        isFinalStep = false
    )
)
analytics.track(onboardingStep1)
```

### Button Tracking

```kotlin
Button(
    onClick = {
        trackButtonTap(analytics, "subscribe", homeView, extra = "premium")
    }
) {
    Text("Subscribe")
}

// With list index (0-based, sent as 1-based "order")
Button(
    onClick = {
        trackButtonTap(analytics, "select_item", homeView, index = 2) // order=3
    }
) {
    Text("Item #3")
}
```

## Phase 4 Usage Examples

### Error Tracking

```kotlin
// Simple error
analytics.trackErrorEvent(
    reason = "network_timeout",
    extraParams = mapOf("endpoint" to "/api/users".toAnalyticsValue())
)

// Error with exception
try {
    // Some operation
} catch (e: Exception) {
    analytics.trackErrorEvent(
        reason = "payment_failed",
        error = e,
        extraParams = mapOf("amount" to 9.99.toAnalyticsValue())
    )
}

// Track correction
analytics.trackErrorCorrected(
    reason = "network_timeout",
    extraParams = mapOf("retry_count" to 3.toAnalyticsValue())
)
```

### Stuck UI Detection

```kotlin
// Track a loading screen with 5-second timeout
val loadingView = ViewAnalyticsModel("loading", type = "splash")
analytics.track(loadingView, stuckTimeout = 5000L)

// If view doesn't transition within 5s:
// - Sends: error reason="stuck on ui_view_show", duration=5.0, view_name="loading", view_type="splash"
//
// If it transitions within 30s after stuck:
// - Sends: error_corrected reason="stuck on ui_view_show", duration=<total_time>
```

### Permission Tracking

```kotlin
// Show permission request
analytics.trackPermissionScreenShow(TAPermissionType.PUSH_NOTIFICATIONS)

// Track user response
analytics.trackPermissionButtonTap(
    allowed = true,
    TAPermissionType.PUSH_NOTIFICATIONS
)

// Track custom status
analytics.trackPermissionButtonTap(
    status = "already_granted",
    TAPermissionType.LOCATION
)

// Custom permission type
analytics.trackPermissionScreenShow("bluetooth")
analytics.trackPermissionButtonTap(allowed = false, "bluetooth")
```

### User ID Synchronization

```kotlin
// Set user ID (propagated to all adaptors)
analytics.userID = "user_12345"

// Get user ID (from first read-write adaptor)
val currentUserID = analytics.userID

// Get platform pseudo ID (e.g., Firebase App Instance ID)
val pseudoID = analytics.userPseudoID

// Clear user ID
analytics.userID = null
```

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run tests for a specific module
./gradlew :taanalytics:test
./gradlew :firebase-adaptor:test

# Run specific test class
./gradlew test --tests "TAAnalyticsBasicTests"

# Run tests with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### Test Files

The library includes comprehensive unit tests:

- **TAAnalyticsBasicTests** (15 tests) - Core functionality: track, set, get, findEvents, clear, userID
- **TAAnalyticsUITests** (12 tests) - UI tracking: view show, button tap, funnel steps
- **TAAnalyticsErrorTests** (15 tests) - Error tracking and permission tracking

Total: **42 unit tests** covering all major functionality.

### Quick Start

```kotlin
import agency.techartists.taanalytics.mock.MockTAAnalytics
import org.junit.Test
import org.junit.Assert.*

class MyFeatureTest {
    @Test
    fun `button click tracks event`() {
        val mockAnalytics = MockTAAnalytics()
        val feature = MyFeature(mockAnalytics)

        feature.onButtonClicked()

        assertEquals(1, mockAnalytics.eventsSent.size)
        val (event, _) = mockAnalytics.eventsSent[0]
        assertEquals("button_clicked", event.rawValue)
    }
}
```

## Firebase Analytics Adaptor

Firebase Analytics integration is available in the `firebase-adaptor` module.

See [firebase-adaptor/README.md](firebase-adaptor/README.md) for detailed setup instructions.

### Quick Setup

```kotlin
import agency.techartists.taanalytics.firebase.FirebaseAnalyticsAdaptor
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

// Get Firebase Analytics instance
val firebaseAnalytics = Firebase.analytics

// Create Firebase adaptor
val firebaseAdaptor = FirebaseAnalyticsAdaptor(firebaseAnalytics)

// Configure TAAnalytics with Firebase
val config = TAAnalyticsConfig(
    analyticsVersion = "1.0",
    adaptors = listOf(firebaseAdaptor),
    sharedPreferences = getSharedPreferences("TAAnalytics", MODE_PRIVATE)
)

val analytics = TAAnalytics(applicationContext, config)
lifecycleScope.launch {
    analytics.start()
}
```

### Features

- ✅ **Automatic Firebase limits handling** - Event names, user properties, and parameters automatically trimmed
- ✅ **Type conversion** - TAAnalytics types converted to Firebase-compatible types
- ✅ **User ID sync** - User IDs automatically propagated to Firebase
- ✅ **Pseudo ID support** - Access Firebase App Instance ID via `analytics.userPseudoID`
- ✅ **Multi-adaptor support** - Use Firebase alongside other analytics platforms

## External Adaptors

### Available
- ✅ **Firebase Analytics** - `firebase-adaptor` module (included)

### Future
- Amplitude adaptor
- MixPanel adaptor
- Custom platform adaptors

## Architecture

```
TAAnalytics
    ├── Models (type-safe wrappers)
    ├── Core (main analytics class)
    ├── Adaptor (platform abstractions)
    │   ├── LogcatAnalyticsAdaptor
    │   └── FirebaseAnalyticsAdaptor (firebase-adaptor module)
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
| UI tracking | ✅ | ✅ Phase 3 |
| View/button tracking | ✅ | ✅ Phase 3 |
| Funnel tracking | ✅ | ✅ Phase 3 |
| Compose integration | ✅ | ✅ Phase 3 |
| Error tracking | ✅ | ✅ Phase 4 |
| Stuck UI detection | ✅ | ✅ Phase 4 |
| Permission tracking | ✅ | ✅ Phase 4 |
| User ID sync | ✅ | ✅ Phase 4 |
| Mock analytics | ✅ | ✅ Phase 5 |
| Test infrastructure | ✅ | ✅ Phase 5 |
| Firebase adaptor | ✅ | ✅ Phase 5 |

## License

MIT License - Copyright (c) 2024 Tech Artists Agency SRL
