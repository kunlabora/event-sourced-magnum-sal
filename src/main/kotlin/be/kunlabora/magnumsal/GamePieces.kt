package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.SaltQuality.BROWN
import be.kunlabora.magnumsal.SaltQuality.GREEN
import java.util.*

enum class Level {
    I, II, III
}

data class MineTile(val level: Level, val salt: List<SaltQuality>, val waterCubes: WaterCubes = 0, val id: UUID = UUID.randomUUID())

val LevelOneChambers = listOf(
        Level.I with listOf(BROWN, BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, GREEN) and 1,
        Level.I with listOf(BROWN, BROWN, BROWN) and 1,
        Level.I with listOf(BROWN, BROWN, BROWN) and 1,
        Level.I with listOf(BROWN, GREEN, GREEN) and 1,
        Level.I with listOf(BROWN, BROWN) and 0
)

infix fun Level.with(salt: List<SaltQuality>): MineTile = MineTile(this, salt)
infix fun MineTile.and(waterCubes: WaterCubes): MineTile = this.copy(waterCubes = waterCubes)
