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
    internal fun `addPlayer | Cannot add two players with the same color`() {
        val player1 = PlayerJoined("Tim", Black)

        magnumSal.addPlayer("Tim", Black)
        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy { magnumSal.addPlayer("Bruno", Black) }

        assertThat(eventStream).containsExactly(player1)
    }

    //TODO To expand to 5th player at a later time
    @Test
    internal fun `addPlayer | Cannot add a third player`() {
        val player1 = PlayerJoined("Tim", Black)
        val player2 = PlayerJoined("Bruno", White)

        magnumSal.addPlayer("Tim", Black)
        magnumSal.addPlayer("Bruno", White)
        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy { magnumSal.addPlayer("Nele", Orange) }

        assertThat(eventStream).containsExactly(player1, player2)
    }

    @Test
    fun `determinePlayOrder | Cannot determine a player order when only one player joined`() {
        val player1 = PlayerJoined("Tim", Black)

        magnumSal.addPlayer("Tim", Black)
        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy { magnumSal.determinePlayOrder(White, Black) }

        assertThat(eventStream).containsExactly(player1)
    }

    @Test
    fun `determinePlayOrder | Cannot determine a player order with colors that players didn't choose`() {
        setupGameWithTwoPlayers()
        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy { magnumSal.determinePlayOrder(Orange, Black) }

        assertThat(eventStream).filteredOn { it is PlayerOrderDetermined }.isEmpty()
    }

    @Test
    fun `determinePlayOrder | Cannot determine a player order with two same colors`() {
        setupGameWithTwoPlayers()
        assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy { magnumSal.determinePlayOrder(Black, Black) }

        assertThat(eventStream).filteredOn { it is PlayerOrderDetermined }.isEmpty()
    }

    @Test
    fun `determinePlayOrder | Can determine a player order when at least two players joined`() {
        setupGameWithTwoPlayers()
        magnumSal.determinePlayOrder(White, Black)

        assertThat(eventStream).contains(PlayerOrderDetermined(White,Black))
    }

    @Test
    fun `placeWorkerInMine | Can place a worker in Shaft 1`() {
        setupGameWithTwoPlayers().withPlayerOrder(White, Black)

        magnumSal.placeWorkerInMine(White, "shaft[1]")

        assertThat(eventStream).contains(MinerPlaced(White,"shaft[1]"))
    }

    //TODO move to TestBuilder
    private fun setupGameWithTwoPlayers(): MagnumSal {
        magnumSal.addPlayer("Tim", Black)
        magnumSal.addPlayer("Bruno", White)
        return magnumSal
    }

}
