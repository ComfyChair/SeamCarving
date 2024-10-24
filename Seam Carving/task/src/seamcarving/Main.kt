package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    var inPath: String = ""
    var outPath: String = ""
    for (i in 0 until args.size step 2) {
        val command = args[i]
        when (command) {
            "-in" -> inPath = args[i + 1]
            "-out" -> outPath = args[i + 1]
        }
    }
    if (inPath.isNotEmpty() && outPath.isNotEmpty()) {
        val negative = createNegative(inPath)
        ImageIO.write(negative, "png", File(outPath))
    }
}

fun createNegative(inPath: String) : BufferedImage {
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
