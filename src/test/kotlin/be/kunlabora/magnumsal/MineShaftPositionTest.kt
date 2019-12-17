package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MineShaftPosition.Companion.at
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class MineShaftPositionTest {

    @Test
    fun `MineShaft is only 6 deep`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { at(7) }
    }

    @Test
    fun `The top of the MineShaft is only at index 1`() {
        assertThat(at(1).isTheTop()).isTrue()
        assertThat(at(2).isTheTop()).isFalse()
        assertThat(at(3).isTheTop()).isFalse()
        assertThat(at(4).isTheTop()).isFalse()
        assertThat(at(5).isTheTop()).isFalse()
        assertThat(at(6).isTheTop()).isFalse()
    }

    @Test
    fun `MineShaft does not go above ground`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { at(0) }
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { at(-1) }
    }

}
