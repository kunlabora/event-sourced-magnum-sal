package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.PositionInMine.Companion.at
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class PositionInMineTest {

    @Test
    fun `The top of the Mine is only at index 1`() {
        assertThat(at(1, 0).isTheTop()).isTrue()
        assertThat(at(2, 0).isTheTop()).isFalse()
        assertThat(at(3, 0).isTheTop()).isFalse()
        assertThat(at(4, 0).isTheTop()).isFalse()
        assertThat(at(5, 0).isTheTop()).isFalse()
        assertThat(at(6, 0).isTheTop()).isFalse()
    }

    @Test
    fun `The Mine is only 6 deep`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { at(7, 0) }
    }

    @Test
    fun `The Mine does not go above ground`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { at(0, 0) }
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { at(-1, 0) }
    }

}
