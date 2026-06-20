# Dark Mode Design

## Overview

Add dark mode support to SyncTouch using Material Design 3 `darkColorScheme`, triggered by system setting with optional manual override in settings.

## Decisions

| Decision | Choice |
|---|---|
| Toggle mechanism | Follow system by default, manual override in settings (Follow System / Light / Dark) |
| Color scheme | M3 `darkColorScheme()` auto-generated from seed color `#315CFF` |
| Drawable colors | Unchanged — keep existing hardcoded light colors in drawable XMLs |
| Approach | `values-night/` resource qualifier + `AppCompatDelegate.setDefaultNightMode()` |

## Architecture

```
App Launch
  → MainActivity.onCreate()
    → Read SharedPreferences "theme_mode"
    → AppCompatDelegate.setDefaultNightMode()
      → MODE_NIGHT_FOLLOW_SYSTEM (default)
      → MODE_NIGHT_NO (light)
      → MODE_NIGHT_YES (dark)
    → Android auto-selects values/ or values-night/ resources
    → Renders corresponding theme
```

Theme base: `Theme.Material3.DayNight.NoActionBar` (changed from `Theme.Material3.Light.NoActionBar`).

## Files to Create

### `res/values-night/colors.xml`

Dark M3 color roles auto-generated from seed `#315CFF`:

| Role | Value | Notes |
|---|---|---|
| primary | `#859EFF` | Light blue |
| on_primary | `#07164E` | Dark navy |
| primary_container | `#07164E` | Dark blue container |
| on_primary_container | `#DCE3FF` | Light blue text |
| secondary | `#C1C9DD` | Light slate |
| on_secondary | `#2B334B` | Dark slate |
| secondary_container | `#414A62` | Mid slate container |
| on_secondary_container | `#DCE3F9` | Light slate text |
| tertiary | `#FFB0D0` | Light pink |
| on_tertiary | `#611A48` | Dark magenta |
| tertiary_container | `#7C315C` | Mid magenta container |
| on_tertiary_container | `#FFD8EC` | Light pink text |
| error | `#FFB4AB` | Light red |
| on_error | `#690005` | Dark red |
| error_container | `#93000A` | Dark red container |
| on_error_container | `#FFB4AB` | Light red text |
| surface | `#121318` | Near-black |
| on_surface | `#E3E2E7` | Light gray text |
| surface_variant | `#44464F` | Dark gray surface |
| on_surface_variant | `#C4C6D0` | Light gray variant text |
| outline | `#8E9099` | Mid gray |
| outline_variant | `#44464F` | Dark gray |
| background | `#121318` | Near-black (same as surface) |
| on_background | `#E3E2E7` | Light gray text |

### `res/values-night/themes.xml`

```xml
<style name="Theme.Claudekeyboard" parent="Theme.Material3.DayNight.NoActionBar">
    <!-- darkColorScheme specified via colors.xml roles -->
</style>
```

## Files to Modify

### `res/values/themes.xml`

- Change parent from `Theme.Material3.Light.NoActionBar` to `Theme.Material3.DayNight.NoActionBar`

### `res/layout/content_settings.xml`

Add new section before closing `</LinearLayout>`:

```
Section Header: "外观" (textAppearanceTitleMedium, colorOnSurfaceVariant)
Card (MaterialCardView, cornerRadius 22dp, home_card bg, home_card_stroke border):
  LinearLayout (horizontal, paddingHorizontal 16dp, paddingVertical 8dp):
    TextView: "主题模式" (textAppearanceBodyLarge, colorOnSurface)
    MaterialButtonToggleGroup (singleSelection, 3 buttons):
      MaterialButton: "跟随系统"
      MaterialButton: "浅色"
      MaterialButton: "深色"
```

### `MainActivity.kt`

In `onCreate()`, before `setContentView`:
```kotlin
val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
val themeMode = prefs.getString("theme_mode", "MODE_SYSTEM") ?: "MODE_SYSTEM"
AppCompatDelegate.setDefaultNightMode(when (themeMode) {
    "MODE_LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
    "MODE_DARK" -> AppCompatDelegate.MODE_NIGHT_YES
    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
})
```

In `setupSettingsPage()`, add:
```kotlin
val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggle_theme_mode)
val themeMode = prefs.getString("theme_mode", "MODE_SYSTEM") ?: "MODE_SYSTEM"
// Set initial checked button based on themeMode
toggleGroup.check(when (themeMode) {
    "MODE_LIGHT" -> R.id.btn_theme_light
    "MODE_DARK" -> R.id.btn_theme_dark
    else -> R.id.btn_theme_system
})
toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
    if (!isChecked) return@addOnButtonCheckedListener
    val mode = when (checkedId) {
        R.id.btn_theme_light -> "MODE_LIGHT"
        R.id.btn_theme_dark -> "MODE_DARK"
        else -> "MODE_SYSTEM"
    }
    prefs.edit().putString("theme_mode", mode).apply()
    AppCompatDelegate.setDefaultNightMode(when (mode) {
        "MODE_LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
        "MODE_DARK" -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    })
}
```

## Files NOT Modified

- All `res/drawable/bg_*.xml` — keep hardcoded light colors
- `res/values/colors.xml` — light colors unchanged
- All Fragments, Adapters, ViewModels
- `AndroidManifest.xml`

## Strings to Add

| Key | Chinese | English |
|---|---|---|
| `settings_section_appearance` | 外观 | Appearance |
| `settings_theme_mode` | 主题模式 | Theme Mode |
| `settings_theme_system` | 跟随系统 | Follow System |
| `settings_theme_light` | 浅色 | Light |
| `settings_theme_dark` | 深色 | Dark |