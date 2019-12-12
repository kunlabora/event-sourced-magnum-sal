package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MagnumSalTest {

    @Test
    fun `A player can be added`() {
        val eventStream = EventStream()
        val magnumSal = MagnumSal(eventStream)

        magnumSal.addPlayer("Snarf")

        assertThat(eventStream).containsExactly(PlayerAdded("Snarf"))
    }
}
