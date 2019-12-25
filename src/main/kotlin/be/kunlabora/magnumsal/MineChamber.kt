package be.kunlabora.magnumsal

typealias WaterCubes = Int
enum class SaltQuality {
    BROWN,
    GREEN,
    WHITE
}
data class MineChamber(val at: PositionInMine, val salt: List<SaltQuality>, val water: WaterCubes = 0) {
    init {
        require(water in 0..4) { "A MineChamber must have between 0 and 4 cubes of Water" }
        require(at.isInACorridor()) { "A MineChamber can not be present in the mineshaft" }
        require(salt.isNotEmpty()) { "A MineChamber must have at least one Salt" }
    }
}
