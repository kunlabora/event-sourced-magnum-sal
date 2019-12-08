package be.kunlabora.magnumsal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MagnumSalTest {

    @Test
    internal fun `A game can start`() {
        assertThat("Snarf").isEqualTo("Lion-O")
    }
}
