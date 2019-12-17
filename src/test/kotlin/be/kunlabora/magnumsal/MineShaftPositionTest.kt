package be.kunlabora.magnumsal

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class MineShaftPositionTest {

    @Test
    fun `MineShaft is only 6 deep`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineShaftPosition(7) }
    }

    @Test
    fun `MineShaft does not go above ground`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineShaftPosition(0) }
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineShaftPosition(-1) }
    }

}
