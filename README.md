# KMP App Icon Generator Plugin

## Overview

The **KMP App Icon Generator Plugin** is a Gradle plugin designed to simplify and automate the management of app icon resources across Android and iOS platforms in Kotlin Multiplatform (KMP) projects. This plugin ensures consistent handling of icons for different platforms, streamlining your development process by generating all necessary densities and formats from a single source or a set of source images.

## Features

- **Cross-Platform Compatibility**: Automatically generates and manages app icons for both Android and iOS platforms.
- **Android Adaptive Icon Support**: Full support for Android adaptive icons, including foreground and background layers, and automatic `ic_launcher.xml` generation.
- **iOS `Contents.json` Generation**: Automatically generates the `Contents.json` file for iOS app icon sets, ensuring all icon metadata is correctly configured.
- **Flexible Icon Naming**: Supports primary, foreground, and background source icons (e.g., `icon.svg`, `icon_foreground.svg`, `icon_background.svg`).
- **Configurable Inputs**: Allows configuration of source icon names and the default background color for Android adaptive icons.
- **Platform Toggling**: Provides options to enable or disable icon generation for Android or iOS platforms individually.
- **SVG and PNG Support**: Accepts both SVG and PNG files as source icons. SVGs are recommended for scalability.
- **High-Quality Image Processing**: Employs enhanced image processing techniques to ensure high-quality, clear icons across all densities.
- **Integration with Compose Resources**: Leverages `composeApp/src/commonMain/composeResources/drawable` for centralized icon management.
- **Automatic Build Integration**: The `generateIcons` task is automatically hooked into Android and iOS build processes.

## Getting Started

### Installation

To use the KMP App Icon Generator Plugin in your project, add the following to your project-level `build.gradle.kts` file (usually in the `plugins` block of the root project or the KMP module):

```kotlin
plugins {
    id("io.github.qamarelsafadi.kmp.app.icon.generator") version "1.2.6"
}
```

Ensure that your project is using a compatible version of Kotlin and Gradle. This plugin is typically updated to support recent versions.

### Providing Your Icons

The plugin looks for your source icon images in the following directory:
`my-kmp-project/composeApp/src/commonMain/composeResources/drawable/`

You can provide icons in a few ways:

1.  **Basic Icon (Single Source for all purposes):**
    *   `icon.png` or `icon.svg`
    *   This single icon will be used for standard Android icons, iOS icons, and as the foreground for Android adaptive icons if specific adaptive icon files are not provided.

2.  **Android Adaptive Icons (Recommended for Android):**
    *   **Foreground Layer:** `icon_foreground.png` (or `.svg`)
    *   **Background Layer:** `icon_background.png` (or `.svg`)
    *   If `icon_foreground` is not found, the plugin will fall back to using `icon` as the foreground.
    *   If `icon_background` (image) is not found, the plugin will use the `defaultAdaptiveBackgroundColor` value (see Configuration Options) to generate a solid color background for the adaptive icon.

**Note:** All these filenames (`icon`, `icon_foreground`, `icon_background`) are configurable via the plugin extension. See "Configuration Options" below. SVG format is recommended for best quality.

### Configuration Options

You can configure the plugin by adding an `appIconGenerator` block to your module's `build.gradle.kts` file (usually the KMP app module):

```kotlin
appIconGenerator {
    // Name of the main source icon file (without extension). Used for iOS, and as a fallback for Android.
    sourceIconName = "my_company_icon"

    // Name of the Android adaptive foreground icon file.
    sourceIconForegroundName = "adaptive_fg"

    // Name of the Android adaptive background icon file.
    sourceIconBackgroundName = "adaptive_bg"

    // Hex color code for the Android adaptive icon background if no background image is provided.
    defaultAdaptiveBackgroundColor = "#1A237E" // Example: Dark Blue

    // Set to false to disable Android icon generation.
    outputAndroid = true

    // Set to false to disable iOS icon generation.
    outputIos = true
}
```

**Available Options:**

*   `sourceIconName`
    *   **Type:** `String`
    *   **Default:** `"icon"`
    *   **Description:** The base name (without extension) of your primary source icon. This is used for iOS icons and as a fallback for Android's foreground if `sourceIconForegroundName` is not found.
*   `sourceIconForegroundName`
    *   **Type:** `String`
    *   **Default:** `"icon_foreground"`
    *   **Description:** The base name for the Android adaptive icon's foreground layer.
*   `sourceIconBackgroundName`
    *   **Type:** `String`
    *   **Default:** `"icon_background"`
    *   **Description:** The base name for the Android adaptive icon's background layer (can be an image or SVG).
*   `defaultAdaptiveBackgroundColor`
    *   **Type:** `String` (Hex color code)
    *   **Default:** `"#FFFFFF"` (White)
    *   **Description:** The hex color code used for the background of Android adaptive icons if `sourceIconBackgroundName` image is not found.
*   `outputAndroid`
    *   **Type:** `Boolean`
    *   **Default:** `true`
    *   **Description:** Set to `false` if you want to disable all Android icon generation.
*   `outputIos`
    *   **Type:** `Boolean`
    *   **Default:** `true`
    *   **Description:** Set to `false` if you want to disable all iOS icon generation.

### Tasks and Usage

The plugin provides the `generateIcons` task.

-   **Automatic Execution:** The `generateIcons` task is automatically integrated into the standard build process. Simply building or running your Android or iOS app (e.g., `./gradlew assembleDebug` for Android, or building from Xcode for iOS) will trigger the icon generation if necessary.
-   **Manual Execution:** You can also run the task manually:
    ```bash
    ./gradlew generateIcons
    ```
    This will process your source icons based on your configuration and generate:
    -   For Android:
        -   `ic_launcher.png` (legacy) and `ic_launcher_round.png` in various `mipmap-Xdpi` folders.
        -   `ic_launcher_foreground.png` (adaptive layer) in various `drawable-Xdpi` folders.
        -   `ic_launcher_background.png` (adaptive layer, if an image is provided) in various `drawable-Xdpi` folders.
        -   `mipmap-anydpi-v26/ic_launcher.xml` (adaptive icon XML).
        -   `values/ic_launcher_background_colors.xml` (if a color background is used for adaptive icons).
    -   For iOS:
        -   All necessary icon sizes (e.g., `icon-20@2x.png`, `icon-1024.png`, etc.) within the `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/` directory.
        -   A `Contents.json` file within the same `AppIcon.appiconset` directory, correctly cataloging all generated iOS icons.

## Contributing

Contributions are welcome! If you have suggestions for improvements, find a bug, or want to add a new feature, please create an issue or submit a pull request on the project's GitHub repository.

## License

This plugin is released under the Apache License. See the LICENSE file for details.
