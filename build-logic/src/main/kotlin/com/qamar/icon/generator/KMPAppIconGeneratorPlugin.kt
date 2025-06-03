package com.qamar.icon.generator

import com.android.build.api.variant.AndroidComponentsExtension
import com.kitfox.svg.SVGDiagram
import com.kitfox.svg.SVGUniverse
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.nio.file.Files
import java.nio.file.StandardCopyOption

// Extension Data Class for Configuration
open class AppIconGeneratorExtension {
    var sourceIconName: String = "icon"
    var sourceIconForegroundName: String = "icon_foreground"
    var sourceIconBackgroundName: String = "icon_background"
    var defaultAdaptiveBackgroundColor: String = "#FFFFFF" // Hex color string
    var outputAndroid: Boolean = true
    var outputIos: Boolean = true
}

// Data class for iOS icon specifications
data class IosIconSpec(
    val pointSizeFull: String, // e.g., "20x20"
    val idiom: String,
    val scale: String,
    val filename: String,
    val actualSize: Int, // in pixels
    val role: String? = null, // Optional: e.g., "notification", "settings", "spotlight", "app"
    val subtype: String? = null // Optional: e.g., "7" for iPad 7th gen compatibility for 76pt@2x
)

class KMPAppIconGeneratorPlugin : Plugin<Project> {

    // Comprehensive list of iOS icon specifications
    private val iosIconSpecs = listOf(
        IosIconSpec("20x20", "iphone", "2x", "icon-20@2x.png", 40, role = "notification"),
        IosIconSpec("20x20", "iphone", "3x", "icon-20@3x.png", 60, role = "notification"),
        IosIconSpec("29x29", "iphone", "1x", "icon-29.png", 29, role = "settings"),
        IosIconSpec("29x29", "iphone", "2x", "icon-29@2x.png", 58, role = "settings"),
        IosIconSpec("29x29", "iphone", "3x", "icon-29@3x.png", 87, role = "settings"),
        IosIconSpec("40x40", "iphone", "2x", "icon-40@2x.png", 80, role = "spotlight"),
        IosIconSpec("40x40", "iphone", "3x", "icon-40@3x.png", 120, role = "spotlight"),
        IosIconSpec("60x60", "iphone", "2x", "icon-60@2x.png", 120, role = "app"),
        IosIconSpec("60x60", "iphone", "3x", "icon-60@3x.png", 180, role = "app"),
        IosIconSpec("20x20", "ipad", "1x", "icon-20-ipad.png", 20, role = "notification"),
        IosIconSpec("20x20", "ipad", "2x", "icon-20@2x-ipad.png", 40, role = "notification"),
        IosIconSpec("29x29", "ipad", "1x", "icon-29-ipad.png", 29, role = "settings"),
        IosIconSpec("29x29", "ipad", "2x", "icon-29@2x-ipad.png", 58, role = "settings"),
        IosIconSpec("40x40", "ipad", "1x", "icon-40-ipad.png", 40, role = "spotlight"),
        IosIconSpec("40x40", "ipad", "2x", "icon-40@2x-ipad.png", 80, role = "spotlight"),
        IosIconSpec("76x76", "ipad", "1x", "icon-76.png", 76, role = "app"),
        IosIconSpec("76x76", "ipad", "2x", "icon-76@2x.png", 152, role = "app"),
        IosIconSpec("83.5x83.5", "ipad", "2x", "icon-83.5@2x.png", 167, role = "app", subtype = "pro"),
        IosIconSpec("1024x1024", "ios-marketing", "1x", "icon-1024.png", 1024)
    )

    override fun apply(project: Project) {
        // Register the extension
        val config = project.extensions.create("appIconGenerator", AppIconGeneratorExtension::class.java)

        val generateIconsTaskProvider = project.tasks.register("generateIcons") {
            group = "KMPAppIconGeneratorPlugin"
            description = "Generates Android and iOS icons from a single source image."

            doLast {
                // Retrieve the extension instance inside doLast to use configured values
                // Using project.extensions.getByType to ensure it's configured or defaults are used.
                // However, direct use of 'config' registered above should work as Gradle wires it up.
                // For safety, can re-fetch or ensure 'config' is treated as the source of truth.
                // val currentConfig = project.extensions.findByType(AppIconGeneratorExtension::class.java) ?: AppIconGeneratorExtension()
                // Using 'config' directly as it's captured by the lambda.

                val commonResourcesPath = "${project.rootDir}/composeApp/src/commonMain/composeResources/drawable"
                val androidResDir = "${project.rootDir}/composeApp/src/androidMain/res"
                val iosAppIconSetDir = File("${project.rootDir}/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset")

                // Determine foreground source image using configuration
                val foregroundSourceFile = sequenceOf(
                    File("$commonResourcesPath/${config.sourceIconForegroundName}.svg"),
                    File("$commonResourcesPath/${config.sourceIconForegroundName}.png"),
                    File("$commonResourcesPath/${config.sourceIconName}.svg"),
                    File("$commonResourcesPath/${config.sourceIconName}.png")
                ).firstOrNull { it.exists() }
                    ?: throw IllegalArgumentException("No source foreground image file found at $commonResourcesPath using configured names ('${config.sourceIconForegroundName}' or '${config.sourceIconName}')")

                // Determine background source image using configuration (optional)
                val backgroundSourceFile = sequenceOf(
                    File("$commonResourcesPath/${config.sourceIconBackgroundName}.svg"),
                    File("$commonResourcesPath/${config.sourceIconBackgroundName}.png")
                ).firstOrNull { it.exists() }

                val fgTempOutputName = "ic_launcher_foreground_temp.png" // Temporary name, not from config
                val foregroundImageToUse: File = if (foregroundSourceFile.extension == "svg") {
                    convertSvgToPng(foregroundSourceFile, outputName = fgTempOutputName)
                } else {
                    foregroundSourceFile
                }

                var backgroundImageToUse: File? = null
                val bgTempOutputName = "ic_launcher_background_temp.png" // Temporary name
                if (backgroundSourceFile != null) {
                    backgroundImageToUse = if (backgroundSourceFile.extension == "svg") {
                        convertSvgToPng(backgroundSourceFile, outputName = bgTempOutputName)
                    } else {
                        backgroundSourceFile
                    }
                }

                val backgroundIsColor = backgroundImageToUse == null
                // Use configured default background color
                val effectiveBackgroundColor = config.defaultAdaptiveBackgroundColor
                val androidBackgroundPathOrColor: String = if (backgroundIsColor) {
                    effectiveBackgroundColor
                } else {
                    "@drawable/ic_launcher_background"
                }

                if (config.outputAndroid) {
                    project.logger.lifecycle("Generating Android icons...")
                    val androidLegacyMipmapResolutions = mapOf(
                        "mipmap-mdpi" to 48, "mipmap-hdpi" to 72, "mipmap-xhdpi" to 96,
                        "mipmap-xxhdpi" to 144, "mipmap-xxxhdpi" to 192
                    )
                    val androidDrawableResolutions = mapOf(
                        "drawable-mdpi" to 108, "drawable-hdpi" to 162, "drawable-xhdpi" to 216,
                        "drawable-xxhdpi" to 324, "drawable-xxxhdpi" to 432
                    )

                    fun removeFileIfExists(file: File) {
                        if (file.exists()) file.delete()
                    }

                    androidLegacyMipmapResolutions.keys.forEach { dirName ->
                        val dir = File("$androidResDir/$dirName")
                        if (dir.exists()) {
                            removeFileIfExists(File(dir, "ic_launcher.png"))
                            removeFileIfExists(File(dir, "ic_launcher_round.png"))
                            removeFileIfExists(File(dir, "ic_launcher_foreground.png"))
                        }
                    }
                    androidDrawableResolutions.keys.forEach { dirName ->
                        val dir = File("$androidResDir/$dirName")
                        if (dir.exists()) {
                            removeFileIfExists(File(dir, "ic_launcher_foreground.png"))
                            removeFileIfExists(File(dir, "ic_launcher_background.png"))
                        }
                    }
                    File("$androidResDir/mipmap-anydpi-v26").apply { if (exists()) removeFileIfExists(File(this, "ic_launcher.xml")) }
                    File("$androidResDir/values").apply { if (exists()) removeFileIfExists(File(this, "ic_launcher_background_colors.xml")) }

                    androidLegacyMipmapResolutions.forEach { (folder, size) ->
                        File("$androidResDir/$folder").apply { if (!exists()) mkdirs() }
                            .also { outputDir ->
                                resizeAndSaveImage(foregroundImageToUse, size, size, File(outputDir, "ic_launcher.png"))
                                resizeAndSaveImage(foregroundImageToUse, size, size, File(outputDir, "ic_launcher_round.png"), true)
                            }
                    }

                    androidDrawableResolutions.forEach { (folder, size) ->
                         File("$androidResDir/$folder").apply { if (!exists()) mkdirs() }
                            .also { outputDir ->
                                resizeAndSaveImage(foregroundImageToUse, size, size, File(outputDir, "ic_launcher_foreground.png"))
                            }
                    }

                    if (backgroundImageToUse != null) {
                        androidDrawableResolutions.forEach { (folder, size) ->
                            File("$androidResDir/$folder").apply { if (!exists()) mkdirs() }
                                .also { outputDir ->
                                    resizeAndSaveImage(backgroundImageToUse, size, size, File(outputDir, "ic_launcher_background.png"))
                                }
                        }
                    }

                    generateAdaptiveIconXml(project, androidBackgroundPathOrColor)
                    if (backgroundIsColor) {
                        generateColorXmlIfNeeded(project, effectiveBackgroundColor)
                    }
                } else {
                    project.logger.lifecycle("Skipping Android icon generation as per configuration.")
                }

                if (config.outputIos) {
                    project.logger.lifecycle("Generating iOS icons...")
                    if (!iosAppIconSetDir.exists()) {
                        iosAppIconSetDir.mkdirs()
                    }
                    iosAppIconSetDir.listFiles()?.forEach { file ->
                        if (file.extension == "png" || file.name == "Contents.json") {
                            file.delete()
                        }
                    }
                    iosIconSpecs.forEach { spec ->
                        resizeAndSaveImage(foregroundImageToUse, spec.actualSize, spec.actualSize, File(iosAppIconSetDir, spec.filename))
                    }
                    generateContentsJson(project, iosAppIconSetDir, iosIconSpecs)
                } else {
                     project.logger.lifecycle("Skipping iOS icon generation as per configuration.")
                }

                // Clean up temporary files
                if (foregroundSourceFile.extension == "svg") {
                    File(foregroundSourceFile.parent, fgTempOutputName).delete()
                }
                if (backgroundSourceFile?.extension == "svg" && backgroundImageToUse != null) {
                    File(backgroundSourceFile.parent, bgTempOutputName).delete()
                }
            }
        }
        project.pluginManager.withPlugin("com.android.application") {
            project.extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
                project.tasks.configureEach {
                    if (name == "assemble${variant.name.capitalizeFirstChar()}") {
                        dependsOn(generateIconsTaskProvider)
                    }
                }
            }
        }
        project.plugins.withType(org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper::class.java) {
            project.extensions.findByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java)?.targets
                ?.withType(KotlinNativeTarget::class.java)?.configureEach {
                    binaries.all { linkTask.dependsOn(generateIconsTaskProvider) }
                }
        }
    }

    private fun resizeAndSaveImage(inputFile: File, width: Int, height: Int, outputFile: File, isRounded: Boolean = false) {
        val originalImage: BufferedImage = ImageIO.read(inputFile)
        val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = resizedImage.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        graphics.drawImage(originalImage, 0, 0, width, height, null)

        if (isRounded) {
            val mask = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g2 = mask.createGraphics()
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.fill(Ellipse2D.Double(0.0, 0.0, width.toDouble(), height.toDouble()))
            g2.dispose()
            graphics.composite = AlphaComposite.DstIn
            graphics.drawImage(mask, 0, 0, null)
        }
        graphics.dispose()
        ImageIO.write(resizedImage, "png", outputFile)
    }

    private fun convertSvgToPng(svgFile: File, targetWidth: Int = 1024, targetHeight: Int = 1024, outputName: String = "icon.png"): File {
        val svgUniverse = SVGUniverse()
        val diagram: SVGDiagram = svgUniverse.getDiagram(svgFile.toURI())
        val originalWidth = diagram.width
        val originalHeight = diagram.height

        val highResWidth = targetWidth * 2
        val highResHeight = targetHeight * 2
        val bufferedImage = BufferedImage(highResWidth, highResHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = bufferedImage.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

        val scaleFactor = minOf(highResWidth / originalWidth, highResHeight / originalHeight)
        val xOffset = (highResWidth - originalWidth * scaleFactor) / 2
        val yOffset = (highResHeight - originalHeight * scaleFactor) / 2
        val transform = AffineTransform()
        transform.translate(xOffset.toDouble(), yOffset.toDouble())
        transform.scale(scaleFactor.toDouble(), scaleFactor.toDouble())
        graphics.transform = transform
        diagram.render(graphics)
        graphics.dispose()

        val scaledImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val g2 = scaledImage.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.drawImage(bufferedImage, 0, 0, targetWidth, targetHeight, null)
        g2.dispose()

        val outputFile = File(svgFile.parent, outputName)
        ImageIO.write(scaledImage, "png", outputFile)
        return outputFile
    }

    private fun generateAdaptiveIconXml(project: Project, backgroundPathOrColor: String) {
        val adaptiveIconXmlDir = File("${project.rootDir}/composeApp/src/androidMain/res/mipmap-anydpi-v26")
        if (!adaptiveIconXmlDir.exists()) adaptiveIconXmlDir.mkdirs()
        val adaptiveIconFile = File(adaptiveIconXmlDir, "ic_launcher.xml")
        val backgroundDrawable: String = if (backgroundPathOrColor.startsWith("#")) "@color/ic_launcher_background_color" else "@drawable/ic_launcher_background"
        val xmlContent = """
            <?xml version="1.0" encoding="utf-8"?>
            <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
                <background android:drawable="$backgroundDrawable"/>
                <foreground android:drawable="@drawable/ic_launcher_foreground"/>
            </adaptive-icon>
        """.trimIndent()
        adaptiveIconFile.writeText(xmlContent)
    }

    private fun generateColorXmlIfNeeded(project: Project, backgroundColor: String) {
        if (!backgroundColor.startsWith("#")) return
        val valuesDir = File("${project.rootDir}/composeApp/src/androidMain/res/values")
        if (!valuesDir.exists()) valuesDir.mkdirs()
        val colorsFile = File(valuesDir, "ic_launcher_background_colors.xml")
        val xmlContent = """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <color name="ic_launcher_background_color">$backgroundColor</color>
            </resources>
        """.trimIndent()
        colorsFile.writeText(xmlContent)
    }

    private fun generateContentsJson(project: Project, iosAppIconSetDir: File, iconSpecs: List<IosIconSpec>) {
        val imagesArray = iconSpecs.map { spec ->
            mutableMapOf(
                "size" to spec.pointSizeFull,
                "idiom" to spec.idiom,
                "filename" to spec.filename,
                "scale" to spec.scale
            ).apply {
                spec.role?.let { put("role", it) }
                spec.subtype?.let { put("subtype", it) }
            }
        }
        val contentsJson = mapOf("images" to imagesArray, "info" to mapOf("version" to 1, "author" to "KMPAppIconGeneratorPlugin"))
        val jsonString = StringBuilder("{\n  \"images\": [\n")
        imagesArray.forEachIndexed { index, imageMap ->
            jsonString.append("    {\n")
            imageMap.entries.forEachIndexed { entryIndex, entry ->
                val value = entry.value.replace("\"", "\\\"")
                jsonString.append("      \"${entry.key}\": \"${value}\"${if (entryIndex < imageMap.entries.size - 1) "," else ""}\n")
            }
            jsonString.append("    }${if (index < imagesArray.size - 1) "," else ""}\n")
        }
        jsonString.append("  ],\n  \"info\": {\n    \"version\": 1,\n    \"author\": \"KMPAppIconGeneratorPlugin\"\n  }\n}\n")
        File(iosAppIconSetDir, "Contents.json").writeText(jsonString.toString())
    }

    private fun String.capitalizeFirstChar(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
