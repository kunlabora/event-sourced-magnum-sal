package be.kunlabora.magnumsal

import org.assertj.core.api.Assertions.assertThat
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
    fun `The top of the MineShaft is only at index 1`() {
        assertThat(MineShaftPosition(1).isTheTop()).isTrue()
        assertThat(MineShaftPosition(2).isTheTop()).isFalse()
        assertThat(MineShaftPosition(3).isTheTop()).isFalse()
        assertThat(MineShaftPosition(4).isTheTop()).isFalse()
        assertThat(MineShaftPosition(5).isTheTop()).isFalse()
        assertThat(MineShaftPosition(6).isTheTop()).isFalse()
    }

    @Test
    fun `MineShaft does not go above ground`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineShaftPosition(0) }
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineShaftPosition(-1) }
    }

}
