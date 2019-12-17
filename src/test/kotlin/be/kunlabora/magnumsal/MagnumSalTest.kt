package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.*
import be.kunlabora.magnumsal.MineShaftPosition.Companion.at
import be.kunlabora.magnumsal.PlayerColor.*
import be.kunlabora.magnumsal.exception.IllegalTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MagnumSalTest {

    private lateinit var eventStream: EventStream

    @BeforeEach
    internal fun setUp() {
        eventStream = EventStream()
    }

    @Nested
    inner class AddingPlayers {
        @Test
        fun `Cannot add two players with the same color`() {
            val magnumSal = MagnumSal(eventStream)
            magnumSal.addPlayer("Tim", Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.addPlayer("Bruno", Black) }

            assertThat(eventStream)
                    .containsExactly(PlayerJoined("Tim", Black))
                    .doesNotContain(PlayerJoined("Bruno", Black))
        }

        @Test
        fun `Cannot add a fifth player`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder(
                            "Bruno" to White,
                            "Tim" to Black,
                            "Nele" to Orange,
                            "Jan" to Purple)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.addPlayer("Snarf", Orange) }

            assertThat(eventStream)
                    .doesNotContain(PlayerJoined("Snarf", Orange))
        }
    }

    @Nested
    inner class DeterminingPlayOrder {
        @Test
        fun `Cannot determine a player order when only one player joined`() {
            val magnumSal = MagnumSal(eventStream)
            magnumSal.addPlayer("Tim", Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.determinePlayOrder(White, Black) }

            assertThat(eventStream).containsExactly(PlayerJoined("Tim", Black))
        }

        @Test
        fun `Cannot determine a player order with colors that players didn't choose`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayers("Bruno" to White, "Tim" to Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.determinePlayOrder(Orange, Black) }

            assertThat(eventStream.filterEvents<PlayerOrderDetermined>()).isEmpty()
        }

        @Test
        fun `Cannot determine a player order with two same colors`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayers("Bruno" to White, "Tim" to Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.determinePlayOrder(Black, Black) }
                    .withMessage("Transition requires player colors to be unique")

            assertThat(eventStream.filterEvents<PlayerOrderDetermined>()).isEmpty()
        }

        @Test
        fun `Can determine a player order when at least two players joined`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayers("Bruno" to White, "Tim" to Black)

            magnumSal.determinePlayOrder(White, Black)

            assertThat(eventStream).contains(PlayerOrderDetermined(White, Black))
        }

        @Test
        fun `Can determine a player order for three players`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayers("Bruno" to White,
                            "Tim" to Black,
                            "Snarf" to Purple)

            magnumSal.determinePlayOrder(Black, Purple, White)

            assertThat(eventStream).contains(PlayerOrderDetermined(Black, Purple, White))
        }

        @Test
        fun `Can determine a player order for four players`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayers("Bruno" to White,
                            "Tim" to Black,
                            "Gargamel" to Orange,
                            "Snarf" to Purple)

            magnumSal.determinePlayOrder(Orange, Black, Purple, White)

            assertThat(eventStream).contains(PlayerOrderDetermined(Orange, Black, Purple, White))
        }
    }

    @Nested
    inner class PlacingWorkersInTheMine {
        @Test
        fun `Can place a worker in Shaft 1`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)

            magnumSal.placeWorkerInMine(White, at(1))

            assertThat(eventStream).contains(MinerPlaced(White, at(1)))
        }

        @Test
        fun `Cannot place a worker in Shaft 2 when Shaft 1 is unoccupied`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(White, at(2)) }

            assertThat(eventStream).doesNotContain(MinerPlaced(White, at(2)))
        }

        @Test
        fun `Cannot place two workers in one turn`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)

            magnumSal.placeWorkerInMine(White, at(1))
            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(White, at(2)) }

            assertThat(eventStream)
                    .contains(MinerPlaced(White, at(1)))
                    .doesNotContain(MinerPlaced(White, at(2)))
        }

        @Test
        fun `Cannot place a worker when it's not your turn`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(Black, at(1)) }

            assertThat(eventStream)
                    .doesNotContain(MinerPlaced(Black, at(1)))
        }

        @Test
        fun `Second player can place a worker in shaft 1`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)
            magnumSal.placeWorkerInMine(White, at(1))

            magnumSal.placeWorkerInMine(Black, at(1))

            assertThat(eventStream).contains(MinerPlaced(Black, at(1)))
        }

        @Test
        fun `Second player can place a worker in shaft 2`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)
            magnumSal.placeWorkerInMine(White, at(1))

            magnumSal.placeWorkerInMine(Black, at(2))

            assertThat(eventStream).contains(MinerPlaced(Black, at(2)))
        }

        @Test
        fun `Second player cannot place a worker in shaft 3`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)
            magnumSal.placeWorkerInMine(White, at(1))
            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(Black, at(3)) }

            assertThat(eventStream).doesNotContain(MinerPlaced(Black, at(3)))
        }

        @Test
        fun `Players can fill up the shaft`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)

            magnumSal.placeWorkerInMine(White, at(1))
            magnumSal.placeWorkerInMine(Black, at(2))
            magnumSal.placeWorkerInMine(White, at(3))
            magnumSal.placeWorkerInMine(Black, at(4))
            magnumSal.placeWorkerInMine(White, at(5))
            magnumSal.placeWorkerInMine(Black, at(6))

            assertThat(eventStream).contains(
                    MinerPlaced(White, at(1)),
                    MinerPlaced(Black, at(2)),
                    MinerPlaced(White, at(3)),
                    MinerPlaced(Black, at(4)),
                    MinerPlaced(White, at(5)),
                    MinerPlaced(Black, at(6))
            )
        }

        @Test
        fun `Cannot place a worker when you're out of workers`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)
                    .distributeWorkersInTheMineShaft(5, listOf(White, Black))

            MineShaft.from(eventStream).visualize()

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(White, at(6)) }
                    .withMessage("Transition requires you to have enough available workers")

            assertThat(eventStream).doesNotContain(
                    MinerPlaced(White, at(6))
            )
        }
    }

    @Nested
    inner class RemovingMinersFromTheMine {
        @Test
        fun `can remove worker if it does not break the chain`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)
            magnumSal.placeWorkerInMine(White, at(1))
            magnumSal.placeWorkerInMine(Black, at(1))

            magnumSal.removeWorkerFromMine(White, at(1))

            assertThat(eventStream).contains(MinerRemoved(White, at(1)))
        }

        @Test
        fun `cannot remove worker if it would break the chain`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)
            magnumSal.placeWorkerInMine(White, at(1))
            magnumSal.placeWorkerInMine(Black, at(2))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.removeWorkerFromMine(White, at(1)) }

            assertThat(eventStream).doesNotContain(MinerRemoved(White, at(1)))
        }

        @Test
        fun `cannot remove worker if it would break the chain 2nd scenario`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)
            magnumSal.placeWorkerInMine(White, at(1))
            magnumSal.placeWorkerInMine(Black, at(1))
            magnumSal.placeWorkerInMine(White, at(2))
            magnumSal.placeWorkerInMine(Black, at(2))
            magnumSal.removeWorkerFromMine(White, at(1))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.removeWorkerFromMine(Black, at(1)) }

            assertThat(eventStream).doesNotContain(MinerRemoved(Black, at(1)))
        }

        @Test
        fun `cannot remove worker if it's not your turn`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)
            magnumSal.placeWorkerInMine(White, at(1))
            magnumSal.placeWorkerInMine(Black, at(1))
            magnumSal.removeWorkerFromMine(White, at(1))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.removeWorkerFromMine(White, at(1)) }

            assertThat(eventStream).containsOnlyOnce(MinerRemoved(White, at(1)))
        }
    }

    @Nested
    inner class CheckingForTurnOrder {
        @Test
        fun `Illegal case in a game with 2 players, first player goes twice after one round`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black)

            magnumSal.placeWorkerInMine(White, at(1))
            magnumSal.placeWorkerInMine(Black, at(1))
            magnumSal.placeWorkerInMine(White, at(2))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(White, at(2)) }
                    .withMessage("Transition requires it to be your turn")

            assertThat(eventStream).containsOnlyOnce(MinerPlaced(White, at(2)))
        }

        @Test
        fun `Illegal case in a game with 3 players, third player goes twice`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black, "Snarf" to Orange)

            magnumSal.placeWorkerInMine(White, at(1))
            magnumSal.placeWorkerInMine(Black, at(1))
            magnumSal.placeWorkerInMine(Orange, at(1))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(Orange, at(1)) }
                    .withMessage("Transition requires it to be your turn")

            assertThat(eventStream).containsOnlyOnce(MinerPlaced(Orange, at(1)))
        }

        @Test
        fun `Illegal case in a game with 4 players, fourth player goes twice`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" to White, "Tim" to Black, "Snarf" to Orange, "Gargamel" to Purple)

            magnumSal.placeWorkerInMine(White, at(1))
            magnumSal.placeWorkerInMine(Black, at(1))
            magnumSal.placeWorkerInMine(Orange, at(1))
            magnumSal.placeWorkerInMine(Purple, at(1))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(Purple, at(1)) }
                    .withMessage("Transition requires it to be your turn")

            assertThat(eventStream).containsOnlyOnce(MinerPlaced(Purple, at(1)))
        }
    }
}
