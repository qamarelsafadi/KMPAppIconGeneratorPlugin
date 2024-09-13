# **KMP App Icon Generator Plugin**

## **Overview**

The **KMP App Icon Generator Plugin** is a Gradle plugin designed to simplify and automate the management of app icon resources across Android and iOS platforms in Kotlin Multiplatform (KMP) projects. This plugin ensures consistent handling of icons for different platforms, streamlining your development process.

## **Features**

- **Cross-Platform Compatibility**: Automatically configures and manages app icons for both Android and iOS platforms.
- **Integration with Compose Resources**: Supports `composeResources` for centralized image management within KMP projects.
- **Automatic Updates**: Automatically updates and syncs app icons whenever changes are made.
- **Foreground and Round Icons**: Generates both `ic_launcher_foreground` and `ic_launcher_round` icons for Android.

## **Getting Started**

### **Installation**

To use the KMP App Icon Generator Plugin in your project, add the following to your `build.gradle.kts` file:

```kotlin
plugins {
    id("io.github.qamarelsafadi.kmp.app.icon.generator") version "1.2.6"
}
```

Ensure that your project is using a compatible version of Kotlin and Gradle:

- Kotlin version: `1.9.0`
- Gradle version: `8.0+`

### **Project Structure**

For the plugin to function correctly, **you must place your base icon image as `icon.png` or `icon.svg` in the following directory exactly as specified**:

```
my-kmp-project/
├── composeApp/
│   └── src/
│       └── commonMain/
│           └── composeResources/
│               └── drawable/
│                   └── icon.png
|                        or 
|                   └── icon.svg

```

- **`drawable/`**: Ensure that your file is named `icon.png` or `icon.svg` and placed in this directory. This file will be used to generate all necessary app icon resources.

### **Tasks**

The plugin provides the following task:

- **`generateIcons`**: Processes and generates icons for all configured platforms.

### **Usage**

**JUST RUN YOUR ANDROID OR IOS APP AND THE TASK WILL RUN AUTOMATICALLY!**

or 

Once configured, the plugin will automatically handle the generation and placement of app icons for both Android and iOS. To execute the plugin tasks, run:

```bash
./gradlew generateIcons
```




This will process the `icon.png` file and generate:
- `ic_launcher_foreground.png`
- `ic_launcher_round.png` for Android
- Necessary icons for iOS within `Assets.xcassets`

### **Advanced Configuration**

The plugin can be extended or customized to fit the specific needs of your project. For example, you can integrate it with custom tasks or modify the icon generation logic by overriding the default configurations.

## **Contributing**

Contributions are welcome! If you have suggestions for improvements or find a bug, please create an issue or submit a pull request.

## **License**

This plugin is released under the Apache License. See the [LICENSE](https://github.com/qamarelsafadi/KMPAppIconGeneratorPlugin?tab=License-1-ov-file#readme) file for details.

