package seamcarving

data class Pixel(val x:Int, val y: Int, val energy: Double = 0.0) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pixel

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    fun isInBounds(width: Int, height: Int): Boolean {
        return x in 0 until width && y in 0 .. height
    }

    override fun toString(): String {
        return "($x, $y: $energy)"
    }
}

