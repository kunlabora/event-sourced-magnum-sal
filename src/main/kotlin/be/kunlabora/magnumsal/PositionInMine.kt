package be.kunlabora.magnumsal

import kotlin.math.absoluteValue

data class PositionInMine(val depth: Int, val width: Int) {

    private val ends
        get() = listOf(at(2, 4), at(2, -4), at(4, 3), at(4, -3), at(6, 2), at(6, -2), at(6, 0))

    init {
        require(depth > 0) { "The Mine does not go above ground" }
        require(depth in 1..6) { "The Mine is only 6 deep" }
        if (depth in listOf(1, 3, 5) && width != 0) {
            throw IllegalArgumentException("Position out of bounds: there is no corridor at depth $depth")
        } else {
            when (depth) {
                2 -> require(width in -4..4) { "Position out of bounds: there is no mine chamber at $width in the corridor at depth $depth" }
                4 -> require(width in -3..3) { "Position out of bounds: there is no mine chamber at $width in the corridor at depth $depth" }
                6 -> require(width in -2..2) { "Position out of bounds: there is no mine chamber at $width in the corridor at depth $depth" }
            }
        }
    }

    fun isTheTop() = this.depth == 1
    fun isAnEnd() = this in ends
    fun isInACorridor() = this.width != 0
    fun previous() = if (isInMineShaft()) higher() else previousInCorridor()
    fun next() = if (isInMineShaft()) deeper() else nextInCorridor()
    fun isCrossSection() = this in listOf(at(2,0),at(4,0),at(6,0))
    fun nearestFurtherPositions() = if (!isCrossSection()) listOf(next()) else if (depth != 6) listOf(deeper(), at(depth, width - 1), at(depth, width + 1)) else listOf(at(depth, width - 1), at(depth, width + 1))
    private fun previousInCorridor() = if (width < 0) at(depth, width + 1) else at(depth, width - 1)
    private fun nextInCorridor() = if (width < 0) at(depth, width - 1) else at(depth, width + 1)
    private fun isInMineShaft() = this.width == 0
    private fun higher() = at(depth - 1, 0)
    private fun deeper() = at(depth + 1, 0)

    override fun toString(): String {
        return if (width == 0) "mineshaft[$depth]" else if (width < 0) "minechamber[$depth, left[${width.absoluteValue}]]" else "minechamber[$depth, right[$width]]"
    }



    companion object {
        fun at(depth: Int, width: Int): PositionInMine = PositionInMine(depth, width)
    }
}
