package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.Event.*
import be.kunlabora.magnumsal.PlayerColor.*
import be.kunlabora.magnumsal.exception.IllegalTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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

    @Nested
    inner class AddPlayer {
        @Test
        fun `Cannot add two players with the same color`() {
            val player1 = PlayerJoined("Tim", Black)

            magnumSal.addPlayer("Tim", Black)
            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.addPlayer("Bruno", Black) }

            assertThat(eventStream).containsExactly(player1)
        }

        //TODO To expand to 5th player at a later time
        @Test
        fun `Cannot add a third player`() {
            val player1 = PlayerJoined("Tim", Black)
            val player2 = PlayerJoined("Bruno", White)

            magnumSal.addPlayer("Tim", Black)
            magnumSal.addPlayer("Bruno", White)
            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.addPlayer("Nele", Orange) }

            assertThat(eventStream).containsExactly(player1, player2)
        }
    }

    @Nested
    inner class DeterminePlayOrder {
        @Test
        fun `Cannot determine a player order when only one player joined`() {
            val player1 = PlayerJoined("Tim", Black)

            magnumSal.addPlayer("Tim", Black)
            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.determinePlayOrder(White, Black) }

            assertThat(eventStream).containsExactly(player1)
        }

        @Test
        fun `Cannot determine a player order with colors that players didn't choose`() {
            setupMagnumSalWithTwoPlayers()
            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.determinePlayOrder(Orange, Black) }

            assertThat(eventStream).filteredOn { it is PlayerOrderDetermined }.isEmpty()
        }

        @Test
        fun `Cannot determine a player order with two same colors`() {
            setupMagnumSalWithTwoPlayers()
            assertThatExceptionOfType(IllegalArgumentException::class.java)
                    .isThrownBy { magnumSal.determinePlayOrder(Black, Black) }

            assertThat(eventStream).filteredOn { it is PlayerOrderDetermined }.isEmpty()
        }

        @Test
        fun `Can determine a player order when at least two players joined`() {
            setupMagnumSalWithTwoPlayers()
            magnumSal.determinePlayOrder(White, Black)

            assertThat(eventStream).contains(PlayerOrderDetermined(White, Black))
        }
    }

    @Nested
    inner class PlaceWorkerInMine {
        @Test
        fun `Can place a worker in Shaft 1`() {
            setupMagnumSalWithTwoPlayers().withPlayerOrder(White, Black)

            magnumSal.placeWorkerInMine(White, MineShaftPosition(1))

            assertThat(eventStream).contains(MinerPlaced(White, MineShaftPosition(1)))
        }

        @Test
        fun `Cannot place a worker in Shaft 2 when Shaft 1 is unoccupied`() {
            setupMagnumSalWithTwoPlayers().withPlayerOrder(White, Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(White, MineShaftPosition(2)) }

            assertThat(eventStream).doesNotContain(MinerPlaced(White, MineShaftPosition(2)))
        }

        @Test
        fun `Cannot place two workers in one turn`() {
            val magnumSal = setupMagnumSalWithTwoPlayers().withPlayerOrder(White, Black)

            magnumSal.placeWorkerInMine(White, MineShaftPosition(1))
            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(White, MineShaftPosition(2)) }

            assertThat(eventStream)
                    .contains(MinerPlaced(White, MineShaftPosition(1)))
                    .doesNotContain(MinerPlaced(White, MineShaftPosition(2)))
        }

        @Test
        fun `Cannot place a worker when it's not your turn`() {
            val magnumSal = setupMagnumSalWithTwoPlayers().withPlayerOrder(White, Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(Black, MineShaftPosition(1)) }

            assertThat(eventStream)
                    .doesNotContain(MinerPlaced(Black, MineShaftPosition(1)))
        }

        @Test
        fun `Second player can place a worker in shaft 1`() {
            val magnumSal = setupMagnumSalWithTwoPlayers()
                    .withPlayerOrder(White, Black)
            magnumSal.placeWorkerInMine(White, MineShaftPosition(1))

            magnumSal.placeWorkerInMine(Black, MineShaftPosition(1))

            assertThat(eventStream).contains(MinerPlaced(Black, MineShaftPosition(1)))
        }

        @Test
        fun `Second player can place a worker in shaft 2`() {
            val magnumSal = setupMagnumSalWithTwoPlayers()
                    .withPlayerOrder(White, Black)
            magnumSal.placeWorkerInMine(White, MineShaftPosition(1))

            magnumSal.placeWorkerInMine(Black, MineShaftPosition(2))

            assertThat(eventStream).contains(MinerPlaced(Black, MineShaftPosition(2)))
        }

        @Test
        fun `Second player cannot place a worker in shaft 3`() {
            val magnumSal = setupMagnumSalWithTwoPlayers()
                    .withPlayerOrder(White, Black)
            magnumSal.placeWorkerInMine(White, MineShaftPosition(1))
            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(Black, MineShaftPosition(3)) }

            assertThat(eventStream).doesNotContain(MinerPlaced(Black, MineShaftPosition(3)))
        }

        @Test
        fun `Players can fill up the shaft`() {
            val magnumSal = setupMagnumSalWithTwoPlayers()
                    .withPlayerOrder(White, Black)

            magnumSal.placeWorkerInMine(White, MineShaftPosition(1))
            magnumSal.placeWorkerInMine(Black, MineShaftPosition(2))
            magnumSal.placeWorkerInMine(White, MineShaftPosition(3))
            magnumSal.placeWorkerInMine(Black, MineShaftPosition(4))
            magnumSal.placeWorkerInMine(White, MineShaftPosition(5))
            magnumSal.placeWorkerInMine(Black, MineShaftPosition(6))

            assertThat(eventStream).contains(
                    MinerPlaced(White, MineShaftPosition(1)),
                    MinerPlaced(Black, MineShaftPosition(2)),
                    MinerPlaced(White, MineShaftPosition(3)),
                    MinerPlaced(Black, MineShaftPosition(4)),
                    MinerPlaced(White, MineShaftPosition(5)),
                    MinerPlaced(Black, MineShaftPosition(6))
            )
        }


        //TODO at some point
        fun `placeWorkerInMine | Cannot place a worker when you're out of workers`() {

        }
    }

    @Nested
    inner class RemoveWorkerFromMineTest {
        @Test
        internal fun `can remove worker if it does not break the chain`() {
            val magnumSal = setupMagnumSalWithTwoPlayers()
                    .withPlayerOrder(White, Black)
            magnumSal.placeWorkerInMine(White, MineShaftPosition(1))

            magnumSal.removeWorkerFromMine(White, MineShaftPosition(1))

            assertThat(eventStream).contains(MinerRemoved(White, MineShaftPosition(1)))
        }

        @Test
        internal fun `cannot remove worker if it would break the chain`() {
            val magnumSal = setupMagnumSalWithTwoPlayers()
                    .withPlayerOrder(White, Black)
            magnumSal.placeWorkerInMine(White, MineShaftPosition(1))
            magnumSal.placeWorkerInMine(Black, MineShaftPosition(2))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.removeWorkerFromMine(White, MineShaftPosition(1)) }

            assertThat(eventStream).doesNotContain(MinerRemoved(White, MineShaftPosition(1)))
        }
    }

    @Nested
    inner class MineShaftPositionTest {

        @Test
        fun `MineShaft is only 6 deep`() {
            assertThatExceptionOfType(IllegalArgumentException::class.java)
                    .isThrownBy { MineShaftPosition(7) }
        }

        @Test
        fun `MineShaft does not go above ground`() {
            assertThatExceptionOfType(IllegalArgumentException::class.java)
                    .isThrownBy { MineShaftPosition(0) }
            assertThatExceptionOfType(IllegalArgumentException::class.java)
                    .isThrownBy { MineShaftPosition(-1) }
        }

    }

    //TODO move to TestBuilder
    private fun setupMagnumSalWithTwoPlayers(): MagnumSal {
        magnumSal.addPlayer("Tim", Black)
        magnumSal.addPlayer("Bruno", White)
        return magnumSal
    }

}
