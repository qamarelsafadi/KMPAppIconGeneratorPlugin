# **KMP App Icon Generator Plugin**

## **Overview**

The **KMP App Icon Generator Plugin** is a Gradle plugin designed to simplify and automate the management of app icon resources across Android and iOS platforms in Kotlin Multiplatform (KMP) projects. This plugin ensures consistent handling of icons for different platforms, streamlining your development process.

## **Features**

- **Cross-Platform Compatibility**: Automatically configures and manages app icons for both Android and iOS platforms.
- **Resource Management**: Handles the removal of duplicate PNG files and ensures that the latest resources are used across all platforms.
- **Customizable**: Easily configurable to suit the specific needs of your project.
- **Integration with Compose Resources**: Supports `composeResources` for centralized image management within KMP projects.
- **Automatic Updates**: Automatically updates and syncs app icons whenever changes are made.

## **Getting Started**

### **Installation**

To use the KMP App Icon Generator Plugin in your project, add the following to your `build.gradle.kts` file:

```kotlin
plugins {
    id("io.github.qamarelsafadi.kmp.app.icon.generator") version "1.0.0"
}
```

Ensure that your project is using a compatible version of Kotlin and Gradle:

- Kotlin version: `1.9.0`
- Gradle version: `8.0+`

### **Project Structure**

Here is the correct project structure using the KMP App Icon Generator Plugin:

```
my-kmp-project/
├── composeApp/
│   └── src/
│       └── commonMain/
│           └── composeResources/
│               └── files/
│                   └── icon/
│                       ├── android/
│                       │   └── mipmap-folders/
│                       └── ios/
│                           └── Assets.xcassets/
│                               └── AppIcon.appiconset/
```

- **`android/`**: Place all your `mipmap` folders here.
- **`ios/`**: Include the `Assets.xcassets` directory with the `AppIcon.appiconset` inside.

### **Tool for Generating Icons**

To create the necessary `Assets.xcassets` for iOS and `mipmap` files for Android, you can use the [AppIcon.co tool](https://www.appicon.co). Simply upload your base icon image, download the generated files, and place them in the respective directories.


### **Tasks**

The plugin provides the following tasks:

- **`generateIcons`**: Processes and generates icons for all configured platforms.

### **Usage**

Once configured, the plugin will automatically handle the generation and placement of app icons for both Android and iOS. You can execute the plugin tasks by running:

```bash
./gradlew generateIcons
```

This will process the icons and place them in the appropriate directories for each platform.


### **Advanced Configuration**

The plugin can be extended or customized to fit the specific needs of your project. For example, you can integrate it with custom tasks or modify the icon generation logic by overriding the default configurations.

## **Contributing**

Contributions are welcome! If you have suggestions for improvements or find a bug, please create an issue or submit a pull request.

## **License**

This plugin is released under the Apache License. See the [LICENSE](https://github.com/qamarelsafadi/KMPAppIconGeneratorPlugin?tab=License-1-ov-file#readme) file for details.
