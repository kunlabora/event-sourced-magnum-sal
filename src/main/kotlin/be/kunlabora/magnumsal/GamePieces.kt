package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.SaltQuality.*
import java.util.*

enum class Level {
    I, II, III
}

data class MineTile(val level: Level, val salt: List<SaltQuality>, val waterCubes: WaterCubes = 0, val id: UUID = UUID.randomUUID())
infix fun Level.with(salt: List<SaltQuality>): MineTile = MineTile(this, salt)
infix fun MineTile.and(waterCubes: WaterCubes): MineTile = this.copy(waterCubes = waterCubes)

val LevelOneMineTiles = listOf(
        Level.I with listOf(BROWN, BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, BROWN, BROWN) and 1,
        Level.I with listOf(BROWN, BROWN, BROWN) and 1,
        Level.I with listOf(BROWN, GREEN, GREEN) and 1,
        Level.I with listOf(BROWN, BROWN) and 0
)
val LevelTwoMineTiles = listOf(
        Level.II with listOf(BROWN, BROWN, BROWN, GREEN) and 2,
        Level.II with listOf(GREEN, GREEN, WHITE) and 2,
        Level.II with listOf(BROWN, WHITE, WHITE) and 3,
        Level.II with listOf(BROWN, GREEN, GREEN, WHITE) and 2,
        Level.II with listOf(BROWN, BROWN, GREEN, WHITE) and 2,
        Level.II with listOf(BROWN, GREEN, GREEN, GREEN) and 2
)
val LevelThreeMineTiles = listOf(
        Level.III with listOf(GREEN, GREEN, GREEN, WHITE, WHITE) and 3,
        Level.III with listOf(GREEN, WHITE, WHITE, WHITE) and 3,
        Level.III with listOf(GREEN, GREEN, WHITE, WHITE, WHITE) and 3,
        Level.III with listOf(WHITE, WHITE, WHITE, WHITE) and 3
)

val AllMineTiles = (LevelOneMineTiles + LevelTwoMineTiles + LevelThreeMineTiles)
