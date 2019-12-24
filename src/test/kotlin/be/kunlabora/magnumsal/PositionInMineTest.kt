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
    fun `The Mine has Ends as last mine chambers including the bottom of the mineshaft`() {
        assertThat(at(2, 4).isAnEnd()).isTrue()
        assertThat(at(2, -4).isAnEnd()).isTrue()
        assertThat(at(4, 3).isAnEnd()).isTrue()
        assertThat(at(4, -3).isAnEnd()).isTrue()
        assertThat(at(6, 2).isAnEnd()).isTrue()
        assertThat(at(6, -2).isAnEnd()).isTrue()
        assertThat(at(6, 0).isAnEnd()).isTrue()

        assertThat(at(6, 1).isAnEnd()).isFalse()
        assertThat(at(4, -1).isAnEnd()).isFalse()
        assertThat(at(2, 0).isAnEnd()).isFalse()
        assertThat(at(1, 0).isAnEnd()).isFalse()
    }

    @Test
    fun `The Mine has 3 cross-sections`() {
        assertThat(at(2,0).isCrossSection()).isTrue()
        assertThat(at(4,0).isCrossSection()).isTrue()
        assertThat(at(6,0).isCrossSection()).isTrue()

        assertThat(at(1,0).isCrossSection()).isFalse()
        assertThat(at(2,2).isCrossSection()).isFalse()
        assertThat(at(4,-1).isCrossSection()).isFalse()
        assertThat(at(6,1).isCrossSection()).isFalse()
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

    @Test
    fun `previous | when it's a mineshaft position, returns higher up the mineshaft`() {
        assertThat(at(6, 0).previous()).isEqualTo(at(5, 0))
        assertThat(at(5, 0).previous()).isEqualTo(at(4, 0))
        assertThat(at(4, 0).previous()).isEqualTo(at(3, 0))
        assertThat(at(3, 0).previous()).isEqualTo(at(2, 0))
        assertThat(at(2, 0).previous()).isEqualTo(at(1, 0))
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { at(1, 0).previous() }
                .withMessage("The Mine does not go above ground")
    }

    @Test
    fun `previous | when it's a first corridor position, returns position at same depth but in the mineshaft`() {
        assertThat(at(2,1).previous()).isEqualTo(at(2,0))
        assertThat(at(4,1).previous()).isEqualTo(at(4,0))
        assertThat(at(6,1).previous()).isEqualTo(at(6,0))
        assertThat(at(2,-1).previous()).isEqualTo(at(2,0))
        assertThat(at(4,-1).previous()).isEqualTo(at(4,0))
        assertThat(at(6,-1).previous()).isEqualTo(at(6,0))
    }

    @Test
    fun `previous | from end of 1st left corridor up to the top`() {
        val travelled = emptyList<PositionInMine>().toMutableList()
        at(2,-4)
                .previous().also { travelled += it }
                .previous().also { travelled += it }
                .previous().also { travelled += it }
                .previous().also { travelled += it }
                .previous().also { travelled += it }

        assertThat(travelled).containsExactly(
                at(2,-3),
                at(2,-2),
                at(2,-1),
                at(2,0),
                at(1,0)
        )
    }

    @Test
    fun `next | when it's a mineshaft position, returns deeper down the mineshaft`() {
        assertThat(at(1, 0).next()).isEqualTo(at(2, 0))
        assertThat(at(2, 0).next()).isEqualTo(at(3, 0))
        assertThat(at(3, 0).next()).isEqualTo(at(4, 0))
        assertThat(at(4, 0).next()).isEqualTo(at(5, 0))
        assertThat(at(5, 0).next()).isEqualTo(at(6, 0))
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { at(6, 0).next() }
                .withMessage("The Mine is only 6 deep")
    }

    @Test
    fun `next | when it's a corridor position, returns further along the corridor`() {
        assertThat(at(2,1).next()).isEqualTo(at(2,2))
        assertThat(at(4,1).next()).isEqualTo(at(4,2))
        assertThat(at(6,1).next()).isEqualTo(at(6,2))
        assertThat(at(2,-1).next()).isEqualTo(at(2,-2))
        assertThat(at(4,-1).next()).isEqualTo(at(4,-2))
        assertThat(at(6,-1).next()).isEqualTo(at(6,-2))
    }

    @Test
    fun `nearestFurtherPositions | when not a cross-section, should return next`() {
        assertThat(at(1,0).nearestFurtherPositions()).containsExactly(at(2,0))
        assertThat(at(2,1).nearestFurtherPositions()).containsExactly(at(2,2))
        assertThat(at(4,-1).nearestFurtherPositions()).containsExactly(at(4,-2))
    }

    @Test
    fun `nearestFurtherPositions | when a cross-section, should return next positions of both corridors and one mineshaft deeper, except at bottom`() {
        assertThat(at(2,0).nearestFurtherPositions()).containsExactly(at(3,0), at(2,-1), at(2,1))
        assertThat(at(4,0).nearestFurtherPositions()).containsExactly(at(5,0), at(4,-1), at(4,1))
        assertThat(at(6,0).nearestFurtherPositions()).containsExactly(at(6,-1), at(6,1))
    }
}
