package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.sqrt

enum class Orientation {VERTICAL, HORIZONTAL}

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
            image.seam(Orientation.HORIZONTAL)
            ImageIO.write(image, "png", File(outPath))
        } else {
            println("Input file ${File(inPath).absolutePath} does not exist.")
        }
    }
}

fun BufferedImage.seam(orientation: Orientation) {
    val red = Color.red.rgb
    var energies = this.toEnergy()
    val (width, height) = when (orientation) {
        Orientation.VERTICAL -> Pair(width, height)
        Orientation.HORIZONTAL -> Pair(height, width)
    }
    // flip image in case of horizontal orientation
    if (orientation == Orientation.HORIZONTAL) {
        energies = Array(height) { x -> Array(width) { y ->
            energies[y][x] } }
    }
    val pixels = energies.mapIndexed { y, row -> row.mapIndexed { x, _ -> Pixel(x, y) } }.flatten()
    // search for lowest energy path
    val shortest : List<Pixel> = dijkstraDown(pixels, energies, width, height)
    // paint path in image
    for (pixel in shortest) {
        when (orientation) {
            Orientation.VERTICAL -> this.setRGB(pixel.x, pixel.y, red)
            else -> this.setRGB(pixel.y, pixel.x, red)
        }
    }
}

private fun dijkstraDown(pixels: List<Pixel>, energies: Array<Array<Double>>, width: Int, height: Int) : List<Pixel> {
    val distances = pixels.associateWith { p -> if (p.y == 0) energies[0][p.x] else Double.MAX_VALUE }.toMutableMap()
    // start at smallest energy in first row
    val firstRow = pixels.filter { it.y == 0 }
    // priority queue is heap-based -> optimal for sparse graphs in Dijkstra algorithm
    val queue = PriorityQueue<List<Pixel>> { path1, path2 ->
        val dist1 = distances[path1.last()]!!
        val dist2 = distances[path2.last()]!!
        if (dist1 != dist2) dist1.compareTo(dist2) // lowest energy paths will be examined first
        else path1.size.compareTo(path2.size) }  // if that is a tie, examine longer path first
    queue.addAll(firstRow.map { listOf(it) })
    val paths = mutableListOf<List<Pixel>>()
    // look for path of lowest energy
    while (queue.isNotEmpty()) {
        val path = queue.poll()
        if (path.last().y == height - 1) {
            // add to result list when last row is reached
            paths.add(path)
        } else {
            val nextPixels = path.last().oneDown(width - 1)
            for (next in nextPixels) {
                val newDist: Double = distances[path.last()]!! + (energies[next.y][next.x])
                if (newDist < distances[next]!!) {
                    // update distance and add expanded path to queue
                    distances[next] = newDist
                    queue.add(path + next)
                }
            }
        }
    }
    return paths.minByOrNull { distances[it.last()]!! }!!
}

fun Pixel.oneDown(maxX: Int) : List<Pixel> {
    val newY = this.y + 1
    return listOf(Pixel(x - 1, newY), Pixel(x, newY), Pixel(x + 1, newY)).filter { it.x in 0..maxX }
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
        0 ->            Pair(x,     x + 2)
        width - 1 ->    Pair(x - 2, x)
        else ->         Pair(x - 1, x+1)
    }
    val xGradient = gradient(
        this.getRGB(xDiffRef.first, y),
        this.getRGB(xDiffRef.second,y))

    val yDiffRef = when (y) {
        0 ->            Pair(y,     y + 2)
        height - 1 ->   Pair(y - 2, y)
        else ->         Pair(y - 1, y + 1)
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

