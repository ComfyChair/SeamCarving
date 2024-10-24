package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun main(args: Array<String>) {
    var inPath = ""
    var outPath = ""
    for (i in args.indices step 2) {
        val command = args[i]
        when (command) {
            "-in" -> inPath = args[i + 1]
            "-out" -> outPath = args[i + 1]
        }
    }
    if (inPath.isNotEmpty() && outPath.isNotEmpty()) {
        if (File(inPath).exists()) {
            val energyImage = energyImage(inPath)
            ImageIO.write(energyImage, "png", File(outPath))
        } else {
            println("Input file ${File(inPath).absolutePath} does not exist.")
        }
    }
}

fun energyImage(inPath: String): BufferedImage {
    val image: BufferedImage = ImageIO.read(File(inPath))
    val energy = MutableList(image.height) { MutableList(image.width) { 0.0 } }
    // calculate energy values per pixel
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            energy[y][x] = image.getEnergy(x, y)
        }
    }
    val maxEnergy = energy.maxOf { row -> row.maxOf { it } }
    // set pixels to normalized energy = intensity
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val intensity = (255.0 * energy[y][x] / maxEnergy).toInt()
            val color = Color(intensity, intensity, intensity)
            image.setRGB(x, y, color.rgb)
        }
    }
    return image
}

fun BufferedImage.getEnergy(x: Int, y: Int): Double {
    val xDiffRef = when (x) {
        0 -> Pair(x, x+2)
        width - 1 -> Pair(x-2, x)
        else -> Pair(x-1, x+1)
    }
    val xGradient = gradient(
        this.getRGB(xDiffRef.first, y),
        this.getRGB(xDiffRef.second,y))

    val yDiffRef = when (y) {
        0 -> Pair(y, y+2)
        height - 1 -> Pair(y-2, y)
        else -> Pair(y-1, y+1)
    }
    val yGradient = gradient(
        this.getRGB(x, yDiffRef.first),
        this.getRGB(x, yDiffRef.second))

    val energy = sqrt((xGradient + yGradient).toDouble())
    return energy
}

fun gradient(pixelValue: Int, otherPixelValue: Int): Int {
    val rDiff = Color(pixelValue).red - Color(otherPixelValue).red
    val gDiff = Color(pixelValue).green - Color(otherPixelValue).green
    val bDiff = Color(pixelValue).blue - Color(otherPixelValue).blue
    val sum = rDiff.squared() + gDiff.squared() + bDiff.squared()
    return sum
}

fun Int.squared() = this * this

fun negativeImage(inPath: String) : BufferedImage {
    val image: BufferedImage = ImageIO.read(File(inPath))
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val originalColor = Color(image.getRGB(x, y))
            val negativeColor = Color(
                255 - originalColor.red,
                255 - originalColor.green,
                255 - originalColor.blue).rgb
            image.setRGB(x, y, negativeColor)
        }
    }
    return image
}

private fun redCrossImage() {
    println("Enter rectangle width:")
    val width = readln().toInt()
    println("Enter rectangle height:")
    val height = readln().toInt()
    println("Enter output image name:")
    val imageName = readln()

    val image = createRedCrossImage(width, height)
    ImageIO.write(image, "PNG", File(imageName))
}

private fun createRedCrossImage(width: Int, height: Int): BufferedImage {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()
    graphics.color = Color.red
    graphics.drawLine(0, 0, width -1, height-1)
    graphics.drawLine(0, height-1, width-1, 0)
    return image
}
