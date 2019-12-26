package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.gamepieces.Level
import be.kunlabora.magnumsal.gamepieces.MineChamberTile
import be.kunlabora.magnumsal.gamepieces.Salt
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import java.util.*

class MineChamberTileTest {
    @Test
    fun `A MineChamberTile can have max 4 cubes of Water`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineChamberTile(Level.I, listOf(Salt.BROWN), 5, UUID.randomUUID()) }
                .withMessage("A MineChamberTile must have between 0 and 4 cubes of Water")
    }

    @Test
    fun `A MineChamberTile can not have a negative value for cubes of Water`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineChamberTile(Level.I, listOf(Salt.BROWN), -2, UUID.randomUUID()) }
                .withMessage("A MineChamberTile must have between 0 and 4 cubes of Water")
    }

    @Test
    fun `A MineChamberTile must have at least one Salt`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineChamberTile(Level.I, emptyList(), 3, UUID.randomUUID()) }
                .withMessage("A MineChamberTile must have at least one Salt")
    }
}
