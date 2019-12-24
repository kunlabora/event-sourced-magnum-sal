package be.kunlabora.magnumsal

import java.lang.IllegalArgumentException
import kotlin.math.absoluteValue

data class PositionInMine(val depth: Int, val width: Int) {
    init {
        require(depth in 1..6) { "The Mine is only 6 deep" }
        if (depth in listOf(1, 3, 5) && width != 0) {
            throw IllegalArgumentException("Position out of bounds: there is no corridor at depth $depth")
        } else {
            when(depth) {
                2 -> require(width in -4..4) { "Position out of bounds: there is no mine chamber at $width in the corridor at depth $depth" }
                4 -> require(width in -3..3) { "Position out of bounds: there is no mine chamber at $width in the corridor at depth $depth" }
                6 -> require(width in -2..2) { "Position out of bounds: there is no mine chamber at $width in the corridor at depth $depth" }
            }
        }
    }

    fun higher() = PositionInMine(depth - 1, 0)
    fun deeper() = PositionInMine(depth + 1, 0)
    fun isTheTop() = this.depth == 1
    override fun toString(): String {
        return if (width == 0) "mineshaft[$depth]" else if (width < 0) "minechamber[$depth, left[${width.absoluteValue}]]" else "minechamber[$depth, right[$width]]"
    }

    companion object {
        fun at(depth: Int, width: Int): PositionInMine = PositionInMine(depth, width)
    }
}
