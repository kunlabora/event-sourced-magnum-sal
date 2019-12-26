package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.PositionInMine.Companion.at
import be.kunlabora.magnumsal.gamepieces.Level
import be.kunlabora.magnumsal.gamepieces.MineChamberTile
import be.kunlabora.magnumsal.gamepieces.SaltQuality.BROWN
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class RevealedMineChamberTest {
    @Test
    fun `A MineChamber can not be present in the mineshaft`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { RevealedMineChamber(at(2, 0), MineChamberTile(Level.I, listOf(BROWN), 0, randomUUID())) }
                .withMessage("A MineChamber can not be present in the mineshaft")
    }

    @Test
    fun `A MineChamber's position needs to relate to the level of the MineChamberTile`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { RevealedMineChamber(at(4, 1), MineChamberTile(Level.I, listOf(BROWN), 0, randomUUID())) }
                .withMessage("A MineChamber's position needs to relate to the level of its MineChamberTile")
    }
}
