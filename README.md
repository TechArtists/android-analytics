# TAAnalytics for Android

An opinionated analytics framework wrapper that abstracts underlying analytics platforms (Firebase, MixPanel, etc.) while providing several key benefits:

1. **Opinionated event structure** - Standardized event naming instead of ad-hoc names like `foo_clicked`, `tap_bar`, `baz`
2. **Multi-adaptor support** - Send events to multiple analytics platforms simultaneously with a unified interface
3. **Built-in validation** - Checks and workarounds for common implementation bugs:
   - Automatic trimming of event names & property keys/values
   - Warning for reserved event names
   - Type validation to prevent unsupported parameter types

## Analytics Adaptors

When initializing `TAAnalytics`, pass an array of adaptors that consume events and user property changes. These adaptors forward data to underlying analytics platforms.

Adaptors implement the `AnalyticsAdaptor` interface with mechanisms for platform-specific character limits, type conversions, and more.

### Available Adaptors

| Adaptor Name | Details | Location |
| --- | --- | --- |
| LogcatAnalyticsAdaptor | Log events to Android Logcat | Inside this library |
| FirebaseAnalyticsAdaptor | https://firebase.google.com | `firebase-adaptor` module |
| MixPanelAnalyticsAdaptor | https://mixpanel.com | `mixpanel-adaptor` module |
| AppsFlyerAnalyticsAdaptor | https://appsflyer.com | `appsflyer-adaptor` module |

### Future Adaptors
- Amplitude
- Heap
- Segment
- Adjust

### Multi-Adaptor Example

```kotlin
val analytics = TAAnalytics(
    context = applicationContext,
    config = TAAnalyticsConfig(
        analyticsVersion = "1.0",
        adaptors = listOf(
          LogcatAnalyticsAdaptor(),
          FirebaseAnalyticsAdaptor(Firebase.analytics),
          MixpanelAnalyticsAdaptor(
            MixpanelAPI.getInstance(
              applicationContext,
              "YOUR_KEY_HERE",
              true
            )
          ),
          AppsFlyerAnalyticsAdaptor(
            applicationContext,
            AppsFlyerLib.getInstance()
          )
        ),
        sharedPreferences = getSharedPreferences("TAAnalytics", MODE_PRIVATE)
    )
)

lifecycleScope.launch {
    analytics.start()
}

// Track events
analytics.track(EventAnalyticsModel.FIRST_OPEN)
analytics.track(EventAnalyticsModel.PURCHASE, mapOf(
    "product_id" to "premium_yearly".toAnalyticsValue(),
    "price" to 99.99.toAnalyticsValue()
))
```

## Event Structure

Use custom events or leverage predefined standard events. Using common events across apps makes cross-app analysis easier for data teams.

### Custom Events

`EventAnalyticsModel` models all events, and `AnalyticsBaseParameterValue` provides compile-time guarantees that parameters will reach the underlying platform.

The main tracking function:
```kotlin
fun track(
    event: EventAnalyticsModel,
    params: Map<String, AnalyticsBaseParameterValue>? = null,
    logCondition: EventLogCondition = EventLogCondition.LOG_ALWAYS
)
```

`EventLogCondition` controls frequency:
- `LOG_ALWAYS` - Send every time
- `LOG_ONLY_ONCE_PER_APP_SESSION` - Once per cold launch
- `LOG_ONLY_ONCE_PER_LIFETIME` - Only first time ever

Example:
```kotlin
// Define custom events
object CustomEvents {
    val MY_FIRST_OPEN = EventAnalyticsModel("my_first_open")
    val MY_COLD_APP_LAUNCH = EventAnalyticsModel("my_cold_app_launch")
    val MY_APP_FOREGROUND = EventAnalyticsModel("my_app_foreground")
}

// In your Activity/ViewModel
fun onAppForeground() {
    // Sent every foreground
    analytics.track(CustomEvents.MY_APP_FOREGROUND, mapOf("hello" to "world".toAnalyticsValue()))

    // Sent once per app session
    analytics.track(CustomEvents.MY_COLD_APP_LAUNCH, logCondition = EventLogCondition.LOG_ONLY_ONCE_PER_APP_SESSION)

    // Sent once per lifetime
    analytics.track(CustomEvents.MY_FIRST_OPEN, logCondition = EventLogCondition.LOG_ONLY_ONCE_PER_LIFETIME)
}
```

### Custom User Properties

`UserPropertyAnalyticsModel` models user properties with automatic length validation:

```kotlin
object CustomProperties {
    val FAVORITE_SPORTS_TEAM = UserPropertyAnalyticsModel("favorite_sports_team")
}

analytics.set(CustomProperties.FAVORITE_SPORTS_TEAM, "Mars")
```

User properties are also saved to SharedPreferences for runtime access:
```kotlin
val team = analytics.get(CustomProperties.FAVORITE_SPORTS_TEAM)
```

## Standard Events

### UI Interactions

Instead of many events like `foo_clicked`, `shown_bar`, this library uses 2 generic events with rich parameters:

#### `ui_view_show`
| Parameter | Type | Description |
| --- | --- | --- |
| `name` | String | View name |
| `type` | String? | View type/state (e.g., "no_permissions") |
| `funnel_name` | String? | Business funnel name (e.g., "onboarding") |
| `funnel_step` | Int? | Step index in funnel |
| `funnel_step_is_optional` | Boolean? | If step can be skipped |
| `funnel_step_is_final` | Boolean? | If this is the last step |
| `secondary_name` | String? | Transient UI name (popup, label) |
| `secondary_type` | String? | Transient UI type |

#### `ui_button_tap`
| Parameter | Type | Description |
| --- | --- | --- |
| `name` | String | Symbolic button name (not localized text) |
| `extra` | String? | Additional context |
| `order` | Int? | Button order in list (1-based) |
| `view_name` | String | Parent view name |
| `view_type` | String? | Parent view type |
| `funnel_*` | Various | Same funnel params as view |
| `secondary_view_*` | String? | If tapped on secondary view |

Example with Compose:
```kotlin
// Track view show
val homeView = ViewAnalyticsModel("home", type = "main")
Column(
    modifier = Modifier.trackViewShow(analytics, homeView)
) {
    // UI content
}

// Track button tap
Button(
    onClick = {
        trackButtonTap(analytics, "subscribe", homeView, extra = "premium")
    }
) {
    Text("Subscribe")
}

// Track with funnel
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

### Stuck UI Detection

Track when transient views take too long to load:

```kotlin
val splashView = ViewAnalyticsModel("splash")
analytics.track(splashView, stuckTimeout = 5000L) // 5 seconds

// After 5s without transitioning:
// Sends: error reason="stuck on ui_view_show", duration=5.0, view_name="splash"

// When finally transitions (within 30s):
// Sends: error_corrected reason="stuck on ui_view_show", duration=7.0
```

### Error Tracking

```kotlin
// Simple error
analytics.trackErrorEvent("network_timeout")

// Error with exception
try {
    // operation
} catch (e: Exception) {
    analytics.trackErrorEvent("payment_failed", error = e)
}

// Track correction
analytics.trackErrorCorrected("network_timeout")
```

### Permission Tracking

Standardized permission tracking via `ui_view_show`:

```kotlin
// Show permission request
analytics.trackPermissionScreenShow(TAPermissionType.PUSH_NOTIFICATIONS)

// Track response
analytics.trackPermissionButtonTap(allowed = true, TAPermissionType.PUSH_NOTIFICATIONS)

// Custom permission
analytics.trackPermissionScreenShow("bluetooth")
analytics.trackPermissionButtonTap(allowed = false, "bluetooth")
```

### Automatically Collected Events

| Event | Parameters | Description |
| --- | --- | --- |
| `app_version_update` | `from_version`, `from_build`, `to_version`, `to_build` | Version upgrade detected |
| `os_version_update` | `from_version`, `to_version` | OS upgrade detected |
| `app_open` | `is_cold_launch` | App foreground |
| `app_close` | `view_*`, `funnel_*` | App background with last view info |

### Automatically Set User Properties

| Property | Value | Description |
| --- | --- | --- |
| `analytics_version` | String | Analytics standard version |
| `app_open_count` | Int | Total foreground count |
| `app_cold_launch_count` | Int | Cold start count |

### Install User Properties

Set once at install, prefixed with `install_`:

| Property | Value | Description |
| --- | --- | --- |
| `install_date` | String | ISO 8601 format (YYYY-MM-DD) |
| `install_version` | String | App version at install |
| `install_os_version` | String | OS version at install |
| `install_is_rooted` | Boolean | Root detection at install |
| `install_ui_appearance` | String | Theme: light, dark, unspecified |

Configure which properties to track:
```kotlin
TAAnalyticsConfig(
    analyticsVersion = "1.0",
    adaptors = listOf(...),
    sharedPreferences = prefs,
    installUserProperties = listOf(
        UserProperties.INSTALL_DATE,
        UserProperties.INSTALL_VERSION,
        UserProperties.INSTALL_OS_VERSION,
        UserProperties.INSTALL_IS_ROOTED,
        UserProperties.INSTALL_UI_APPEARANCE
    )
)
```

### Other Standard Events

**Lifecycle:** `onboarding_enter`, `onboarding_exit`, `account_signup_enter`, `account_signup_exit`

**Paywall:** `paywall_enter`, `paywall_purchase_tap`, `paywall_exit`

**Subscriptions:** `subscription_start_intro`, `subscription_start_paid_regular`, `subscription_start_restore`

**Engagement:** `engagement`, `engagement_primary`

See [constants/DefaultConstants.kt](taanalytics/src/main/java/agency/techartists/taanalytics/constants/DefaultConstants.kt) for complete list.

## Configuration

### Event Prefixing

Separate prefixes for automatic vs manual events:

```kotlin
TAAnalyticsConfig(
    analyticsVersion = "1.0",
    adaptors = listOf(...),
    sharedPreferences = prefs,
    automaticallyTrackedEventsPrefixConfig = TAAnalyticsConfig.PrefixConfig(
        eventPrefix = "auto_",
        userPropertyPrefix = "auto_"
    ),
    manuallyTrackedEventsPrefixConfig = TAAnalyticsConfig.PrefixConfig(
        eventPrefix = "manual_",
        userPropertyPrefix = "manual_"
    )
)
```

### Event Filtering

Conditionally send events:

```kotlin
TAAnalyticsConfig(
    analyticsVersion = "1.0",
    adaptors = listOf(...),
    sharedPreferences = prefs,
    trackEventFilter = { event, params ->
        // Don't send debug events in production
        !event.rawValue.contains("debug") || BuildConfig.DEBUG
    }
)
```

### User ID Synchronization

```kotlin
// Set user ID (propagated to all adaptors)
analytics.userID = "user_12345"

// Get user ID
val userId = analytics.userID

// Get platform pseudo ID (e.g., Firebase App Instance ID)
val pseudoId = analytics.userPseudoID

// Clear user ID
analytics.userID = null
```

## Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("agency.techartists:taanalytics:0.1.0")

    // Optional adaptors
    implementation("agency.techartists:taanalytics-firebase:0.1.0")
    implementation("agency.techartists:taanalytics-mixpanel:0.1.0")
    implementation("agency.techartists:taanalytics-appsflyer:0.1.0")
}
```

## License

MIT License - Copyright (c) 2025 Tech Artists Agency SRL
