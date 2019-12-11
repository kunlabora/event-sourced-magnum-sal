package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.Event.*
import be.kunlabora.magnumsal.exception.IllegalTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MagnumSalTest {

    private lateinit var magnumSal: MagnumSal
    private lateinit var eventStream: EventStream

    @BeforeEach
    internal fun setUp() {
        eventStream = EventStream()
        magnumSal = MagnumSal(eventStream)
    }

    @Test
    internal fun `A game can start when at least two players joined`() {
        val player1 = PlayerJoined("Tim", "Black")
        val player2 = PlayerJoined("Bruno", "White")

        magnumSal.addPlayer("Tim", "Black")
        magnumSal.addPlayer("Bruno", "White")
        magnumSal.startGame()

        assertThat(eventStream).containsExactly(player1, player2, GameStarted)
    }

    @Test
    internal fun `A game can not start when only one player joined`() {
        val eventStream = eventStream
        val player1 = PlayerJoined("Tim", "Black")

        magnumSal.addPlayer("Tim", "Black")
        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy { magnumSal.startGame() }

        assertThat(eventStream).containsExactly(player1)
    }
}
