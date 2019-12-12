package be.kunlabora.magnumsal

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test

class MineShaftPositionTest {

    @Test
    fun `A MineShaftPosition has to be between 1 and 6`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineShaftPosition(0) }
                .withMessage("MineShaftPosition 0 does not exist.")
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineShaftPosition(-1) }
                .withMessage("MineShaftPosition -1 does not exist.")
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { MineShaftPosition(7) }
                .withMessage("MineShaftPosition 7 does not exist.")
    }

    @Test
    fun `previous | MineShaftPosition 1 returns itself`() {
        assertThat(MineShaftPosition(1).previous()).isEqualTo(MineShaftPosition(1))
    }

    @Test
    fun `previous | MineShaftPosition other than 1 returns previous position`() {
        assertThat(MineShaftPosition(2).previous()).isEqualTo(MineShaftPosition(1))
        assertThat(MineShaftPosition(3).previous()).isEqualTo(MineShaftPosition(2))
        assertThat(MineShaftPosition(4).previous()).isEqualTo(MineShaftPosition(3))
        assertThat(MineShaftPosition(5).previous()).isEqualTo(MineShaftPosition(4))
        assertThat(MineShaftPosition(6).previous()).isEqualTo(MineShaftPosition(5))
    }

    @Test
    fun `next | MineShaftPosition 6 returns itself`() {
        assertThat(MineShaftPosition(6).next()).isEqualTo(MineShaftPosition(6))
    }

    @Test
    fun `next | MineShaftPosition other than 6 returns next position`() {
        assertThat(MineShaftPosition(1).next()).isEqualTo(MineShaftPosition(2))
        assertThat(MineShaftPosition(2).next()).isEqualTo(MineShaftPosition(3))
        assertThat(MineShaftPosition(3).next()).isEqualTo(MineShaftPosition(4))
        assertThat(MineShaftPosition(4).next()).isEqualTo(MineShaftPosition(5))
        assertThat(MineShaftPosition(5).next()).isEqualTo(MineShaftPosition(6))
    }

    @Test
    fun `toString | just returns the internal int value`() {
        assertThat(MineShaftPosition(1).toString()).isEqualTo("mineshaft[1]")
    }
}
