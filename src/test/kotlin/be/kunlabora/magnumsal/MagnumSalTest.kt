package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.Event.*
import be.kunlabora.magnumsal.PlayerColor.*
import be.kunlabora.magnumsal.exception.IllegalTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException

class MagnumSalTest {

    private lateinit var magnumSal: MagnumSal
    private lateinit var eventStream: EventStream

    @BeforeEach
    internal fun setUp() {
        eventStream = EventStream()
        magnumSal = MagnumSal(eventStream)
    }

    @Test
    fun `determinePlayOrder | Cannot determine a player order when only one player joined`() {
        val eventStream = eventStream
        val player1 = PlayerJoined("Tim", Black)

        magnumSal.addPlayer("Tim", Black)
        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy { magnumSal.determinePlayOrder(White, Black) }

        assertThat(eventStream).containsExactly(player1)
    }

    @Test
    fun `determinePlayOrder | Cannot determine a player order with colors that players didn't choose`() {
        val eventStream = eventStream
        val player1 = PlayerJoined("Tim", Black)
        val player2 = PlayerJoined("Bruno", White)

        magnumSal.addPlayer("Tim", Black)
        magnumSal.addPlayer("Bruno", White)
        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy { magnumSal.determinePlayOrder(Orange, Black) }

        assertThat(eventStream).containsExactly(player1, player2)
    }

    @Test
    fun `determinePlayOrder | Cannot determine a player order with two same colors`() {
        val eventStream = eventStream
        val player1 = PlayerJoined("Tim", Black)
        val player2 = PlayerJoined("Bruno", White)

        magnumSal.addPlayer("Tim", Black)
        magnumSal.addPlayer("Bruno", White)
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { magnumSal.determinePlayOrder(Black, Black) }

        assertThat(eventStream).containsExactly(player1, player2)
    }

    @Test
    fun `determinePlayOrder | Can determine a player order when at least two players joined`() {
        val player1 = PlayerJoined("Tim", Black)
        val player2 = PlayerJoined("Bruno", White)

        magnumSal.addPlayer("Tim", Black)
        magnumSal.addPlayer("Bruno", White)
        magnumSal.determinePlayOrder(White, Black)

        assertThat(eventStream).containsExactly(player1, player2, DeterminedPlayerOrder(White,Black))
    }

}
