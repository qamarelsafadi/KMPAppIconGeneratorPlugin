package com.qamar.icon.generator

import com.kitfox.svg.SVGDiagram
import com.kitfox.svg.SVGUniverse
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class KMPAppIconGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("generateIcons") {
            group = "KMPAppIconGeneratorPlugin"
            description = "Generates Android and iOS icons from a single source image."

            doLast {
                val sourceImagePath = "${project.rootDir}/composeApp/src/commonMain/composeResources/drawable/icon"
                val androidResDir = "${project.rootDir}/composeApp/src/androidMain/res"
                val iosResDir = "${project.rootDir}/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"
                // Determine if the source image is SVG or PNG
                val sourceImageFile = when {
                    File("$sourceImagePath.svg").exists() -> File("$sourceImagePath.svg")
                    File("$sourceImagePath.png").exists() -> File("$sourceImagePath.png")
                    else -> throw IllegalArgumentException("No source image file found at $sourceImagePath with supported extensions (svg or png)")
                }

                val imageFileToUse: File = if (sourceImageFile.extension == "svg") {
                    convertSvgToPng(sourceImageFile)
                } else {
                    sourceImageFile
                }

                // Resize and generate Android icons
                val androidResolutions = mapOf(
                    "mipmap-mdpi" to 48,
                    "mipmap-hdpi" to 72,
                    "mipmap-xhdpi" to 96,
                    "mipmap-xxhdpi" to 144,
                    "mipmap-xxxhdpi" to 192
                )
                // Function to remove existing PNG files
                fun removePngFiles(directory: File) {
                    directory.listFiles()?.filter { it.extension == "png" || it.extension =="webp" }?.forEach { file ->
                        file.delete()
                    }
                }

                // Remove existing PNG files from Android resource directories
                val androidDirectories = listOf("mipmap-mdpi", "mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi")
                androidDirectories.forEach { dir ->
                    val fullPath = File("$androidResDir/$dir")
                    if (fullPath.exists()) {
                        removePngFiles(fullPath)
                    }
                }

                androidResolutions.forEach { (folder, size) ->
                    val outputDir = File("$androidResDir/$folder")
                    if (!outputDir.exists()) outputDir.mkdirs()
                    resizeAndSaveImage(imageFileToUse, size, size, File(outputDir, "ic_launcher.png"))
                    resizeAndSaveImage(imageFileToUse, size, size, File(outputDir, "ic_launcher_foreground.png"))
                    resizeAndSaveImage(imageFileToUse, size, size, File(outputDir, "ic_launcher_round.png"), true)
                }

                // Generate iOS icons (Assuming specific resolutions required by iOS)
                val iosResolutions = listOf(
                    20, 29, 40, 58, 60, 76, 80, 87, 120, 152, 167, 180, 1024
                )

                iosResolutions.forEach { size ->

                    val outputDir = File(iosResDir)
                    if (!outputDir.exists()) outputDir.mkdirs()
                    resizeAndSaveImage(imageFileToUse, size, size, File(outputDir, "app-icon-${size}.png"))
                }
            }
        }
    }

    private fun resizeAndSaveImage(inputFile: File, width: Int, height: Int, outputFile: File, isRounded: Boolean = false) {
        val originalImage: BufferedImage = ImageIO.read(inputFile)
        val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = resizedImage.createGraphics()
        // Set high-quality rendering hints
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Scale the original image
        graphics.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null)

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

    private fun convertSvgToPng(svgFile: File, targetWidth: Int = 1024, targetHeight: Int = 1024, backgroundColor: Color = Color.WHITE): File {
        val svgUniverse = SVGUniverse()
        val diagram: SVGDiagram = svgUniverse.getDiagram(svgFile.toURI())

        // Get the original dimensions of the SVG
        val originalWidth = diagram.width
        val originalHeight = diagram.height

        // Create a BufferedImage with a higher resolution
        val highResWidth = targetWidth * 2
        val highResHeight = targetHeight * 2
        val bufferedImage = BufferedImage(highResWidth, highResHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = bufferedImage.createGraphics()

        // Set background color
        graphics.color = backgroundColor
        graphics.fillRect(0, 0, highResWidth, highResHeight)

        // Apply scaling and translation to center the SVG
        val scaleFactor = minOf(highResWidth / originalWidth, highResHeight / originalHeight)
        val xOffset = (highResWidth - originalWidth * scaleFactor) / 2
        val yOffset = (highResHeight - originalHeight * scaleFactor) / 2

        val transform = AffineTransform()
        transform.translate(xOffset.toDouble(), yOffset.toDouble())
        transform.scale(scaleFactor.toDouble(), scaleFactor.toDouble())

        graphics.transform = transform

        // Render the SVG diagram
        diagram.render(graphics)
        graphics.dispose()

        // Downscale to target dimensions
        val scaledImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val g2 = scaledImage.createGraphics()
        g2.drawImage(bufferedImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null)
        g2.dispose()

        // Save the image to a file
        val outputFile = File(svgFile.parent, "icon.png")
        ImageIO.write(scaledImage, "png", outputFile)

        return outputFile
    }

}
