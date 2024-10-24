package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

fun main() {
    println("Enter rectangle width:")
    val width = readln().toInt()
    println("Enter rectangle height:")
    val height = readln().toInt()
    println("Enter output image name:")
    val imageName = readln()

    val image = rectangleImage(width, height)
    ImageIO.write(image, "PNG", File(imageName))
}

fun rectangleImage(width: Int, height: Int): BufferedImage {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()
    graphics.color = Color.red
    graphics.drawLine(0, 0, width -1, height-1)
    graphics.drawLine(0, height-1, width-1, 0)
    return image
}
