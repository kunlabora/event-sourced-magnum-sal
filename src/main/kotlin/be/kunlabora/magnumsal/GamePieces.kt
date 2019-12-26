package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.SaltQuality.*
import java.util.*

enum class Level {
    I, II, III
}

data class MineChamberTile(val level: Level, val salt: List<SaltQuality>, val waterCubes: WaterCubes = 0, val id: UUID = UUID.randomUUID())
infix fun Level.with(salt: List<SaltQuality>): MineChamberTile = MineChamberTile(this, salt)
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
