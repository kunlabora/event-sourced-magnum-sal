package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.PositionInMine.Companion.at
import be.kunlabora.magnumsal.SaltQuality.BROWN
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import java.util.*
import java.util.UUID.randomUUID

class MineChamberTest {
    @Test
    fun `A MineChamber can not be present in the mineshaft`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineChamber(at(2, 0), listOf(BROWN), 0, randomUUID()) }
                .withMessage("A MineChamber can not be present in the mineshaft")
    }

    @Test
    fun `A MineChamber can have max 4 cubes of Water`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineChamber(at(2, 1), listOf(BROWN), 5, randomUUID()) }
                .withMessage("A MineChamber must have between 0 and 4 cubes of Water")
    }

    @Test
    fun `A MineChamber can not have a negative value for cubes of Water`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineChamber(at(2, 1), listOf(BROWN), -2, randomUUID()) }
                .withMessage("A MineChamber must have between 0 and 4 cubes of Water")
    }

    @Test
    fun `A MineChamber must have at least one Salt`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineChamber(at(2, 1), emptyList(), 3, randomUUID()) }
                .withMessage("A MineChamber must have at least one Salt")
    }
}
