package be.kunlabora.magnumsal

import java.util.*

typealias WaterCubes = Int

enum class SaltQuality {
    BROWN,
    GREEN,
    WHITE
}

data class MineChamber(val at: PositionInMine, val salt: List<SaltQuality>, val water: WaterCubes = 0, val mineTileId: UUID) {
    init {
        require(water in 0..4) { "A MineChamber must have between 0 and 4 cubes of Water" }
        require(at.isInACorridor()) { "A MineChamber can not be present in the mineshaft" }
        require(salt.isNotEmpty()) { "A MineChamber must have at least one Salt" }
    }

    private val level
        get() = when (this.at.depth) {
            2 -> Level.I
            4 -> Level.II
            6 -> Level.III
            else -> throw IllegalStateException("Somehow a MineChamber was created at a depth where there is no corridor...")
        }

    fun asTile(): MineTile = (this.level with this.salt and this.water).copy(id = this.mineTileId)
}
