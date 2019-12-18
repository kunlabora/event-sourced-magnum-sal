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

    @Test
    fun `There are only corridors on depths 2, 4 and 6`() {
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(1, 1) }
                .withMessage("Position out of bounds: there is no corridor at depth 1")
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(3, 1) }
                .withMessage("Position out of bounds: there is no corridor at depth 3")
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(5, 1) }
                .withMessage("Position out of bounds: there is no corridor at depth 5")
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(1, -1) }
                .withMessage("Position out of bounds: there is no corridor at depth 1")
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(3, -1) }
                .withMessage("Position out of bounds: there is no corridor at depth 3")
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(5, -1) }
                .withMessage("Position out of bounds: there is no corridor at depth 5")
    }

    @Test
    fun `There are only 4 mine chambers in both corridors at depth 2`() {
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(2, -5) }
                .withMessage("Position out of bounds: there is no mine chamber at -5 in the corridor at depth 2")
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(2, 5) }
                .withMessage("Position out of bounds: there is no mine chamber at 5 in the corridor at depth 2")
    }

    @Test
    fun `There are only 3 mine chambers in both corridors at depth 4`() {
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(4, -4) }
                .withMessage("Position out of bounds: there is no mine chamber at -4 in the corridor at depth 4")
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(4, 4) }
                .withMessage("Position out of bounds: there is no mine chamber at 4 in the corridor at depth 4")
    }

    @Test
    fun `There are only 2 mine chambers in both corridors at depth 6`() {
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(6, -3) }
                .withMessage("Position out of bounds: there is no mine chamber at -3 in the corridor at depth 6")
        assertThatExceptionOfType(java.lang.IllegalArgumentException::class.java)
                .isThrownBy { at(6, 3) }
                .withMessage("Position out of bounds: there is no mine chamber at 3 in the corridor at depth 6")
    }
}
