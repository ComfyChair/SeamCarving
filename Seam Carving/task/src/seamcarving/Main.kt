package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
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
            val image: BufferedImage = ImageIO.read(File(inPath))
            image.verticalSeam()
            ImageIO.write(image, "png", File(outPath))
        } else {
            println("Input file ${File(inPath).absolutePath} does not exist.")
        }
    }
}

fun BufferedImage.verticalSeam() {
    val energyPixels = this.toEnergy().mapIndexed { y, row ->
        row.mapIndexed { x, energy -> Pixel(x, y, energy) } }
    // initialize distances with MAX_VALUE
    val distances = energyPixels.flatten()
        .associateWith { Double.MAX_VALUE }.toMutableMap()
    // fill distance map to determine minimal paths from first to last row
    println("Filling distance map")
    energyPixels[0].forEach { it.seamFrom(distances, this.width, this.height) }
    // mark Path with red pixels
    println("Painting pixels")
    var current = distances.filter { e -> e.key.y == height - 1 }.minByOrNull { e -> e.value }!!.key
    this.setRGB(current.x, current.y, Color.red.rgb)
    for (y in height - 2 downTo 0) {
        current = distances.filter { e -> e.key.y == y && e.key.x in current.x -1 .. current.x +1}
            .minByOrNull { e -> e.value }!!.key
        this.setRGB(current.x, current.y, Color.red.rgb)
    }
}
//TODO: Use priority queue to shorten runtime

fun Pixel.seamFrom(distances: MutableMap<Pixel, Double>, width: Int, height: Int) {
    // current shortest Path: min of last row's pixel.value
    val minPath : Double = distances.entries
        .filter { e -> e.key.y == this.y -1 && e.key.x in this.x -1 .. this.x +1 } // predecessor
        .minOfOrNull { e -> e.value } ?: 0.0
    val newMin = minPath + this.energy
    if (minPath + this.energy < distances[this]!!) {
        // if path is shorter than known paths, update map and pursuit path
        distances[this] = newMin
        if (this.y < height - 1) {
            val nextSteps = this.stepDown()
            nextSteps.filter { it.isInBounds(width, height) }
                .forEach { pixel -> distances.keys.find { it == pixel }!!.seamFrom(distances, width, height) }
        }
    }
}

private fun Pixel.stepDown() : List<Pixel> {
    val newY = this.y + 1
    return listOf(
        Pixel(this.x - 1, newY),
        Pixel(this.x, newY),
        Pixel(this.x + 1, newY)
    )
}

fun energyImage(image: BufferedImage) {
    val energy = image.toEnergy()
    val maxEnergy = energy.maxOf { row -> row.maxOf { it } }
    // set pixels to normalized energy = intensity
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            // normalize energy
            val intensity = (255.0 * energy[y][x] / maxEnergy).toInt()
            val color = Color(intensity, intensity, intensity)
            image.setRGB(x, y, color.rgb)
        }
    }
}

private fun BufferedImage.toEnergy(): Array<Array<Double>> {
    return Array(this.height) { y -> Array(this.width) { x ->
        getEnergy(x, y)
    } }
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

