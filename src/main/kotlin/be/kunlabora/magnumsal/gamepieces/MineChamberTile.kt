package be.kunlabora.magnumsal.gamepieces

import be.kunlabora.magnumsal.PositionInMine
import be.kunlabora.magnumsal.gamepieces.Salt.*
import be.kunlabora.magnumsal.toCamelCase
import java.util.*

enum class Level {
    I, II, III;

    companion object {
        fun from(at: PositionInMine): Level? {
            if (!at.isInACorridor()) return null
            return when (at.depth) {
                2 -> I
                4 -> II
                6 -> III
                else -> null
            }
        }
    }
}

typealias WaterCubes = Int

enum class Salt {
    BROWN,
    GREEN,
    WHITE;

    override fun toString(): String {
        return super.toString().toCamelCase()
    }
}

data class Salts(private val _salts: List<Salt>) : List<Salt> by _salts {
    fun cubesPerQuality() = _salts.groupBy { it }.mapValues { it.value.size }

    override fun toString(): String =
            cubesPerQuality().map { "${it.value} ${it.key} salt" }.joinToString()
}

data class MineChamberTile(val level: Level, val salt: List<Salt>, val waterCubes: WaterCubes = 0, val id: UUID = UUID.randomUUID()) {
    init {
        require(waterCubes in 0..4) { "A MineChamberTile must have between 0 and 4 cubes of Water" }
        require(salt.isNotEmpty()) { "A MineChamberTile must have at least one Salt" }
    }
}

infix fun Level.with(salt: List<Salt>): MineChamberTile = MineChamberTile(this, salt)
infix fun MineChamberTile.and(waterCubes: WaterCubes): MineChamberTile = this.copy(waterCubes = waterCubes)

private val LevelOneMineChamberTiles = listOf(
        Level.I with listOf(BROWN, BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, BROWN, BROWN) and 1,
        Level.I with listOf(BROWN, BROWN, BROWN) and 1,
        Level.I with listOf(BROWN, GREEN, GREEN) and 1,
        Level.I with listOf(BROWN, BROWN) and 0
)
private val LevelTwoMineChamberTiles = listOf(
        Level.II with listOf(BROWN, BROWN, BROWN, GREEN) and 2,
        Level.II with listOf(GREEN, GREEN, WHITE) and 2,
        Level.II with listOf(BROWN, WHITE, WHITE) and 3,
        Level.II with listOf(BROWN, GREEN, GREEN, WHITE) and 2,
        Level.II with listOf(BROWN, BROWN, GREEN, WHITE) and 2,
        Level.II with listOf(BROWN, GREEN, GREEN, GREEN) and 2
)
private val LevelThreeMineChamberTiles = listOf(
        Level.III with listOf(GREEN, GREEN, GREEN, WHITE, WHITE) and 3,
        Level.III with listOf(GREEN, WHITE, WHITE, WHITE) and 3,
        Level.III with listOf(GREEN, GREEN, WHITE, WHITE, WHITE) and 3,
        Level.III with listOf(WHITE, WHITE, WHITE, WHITE) and 3
)

val AllMineChamberTiles = (LevelOneMineChamberTiles + LevelTwoMineChamberTiles + LevelThreeMineChamberTiles)
