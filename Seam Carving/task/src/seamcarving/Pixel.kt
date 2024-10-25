package seamcarving

data class Pixel(val x:Int, val y: Int) : Comparable<Pixel> {
    override fun compareTo(other: Pixel): Int {
        return if (this.y == other.y) {
            this.x.compareTo(other.x)
        } else {
            this.y.compareTo(other.y)
        }

    }

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

    override fun toString(): String {
        return "($x, $y)"
    }
}

