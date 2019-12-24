package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.*
import be.kunlabora.magnumsal.PositionInMine.Companion.at
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
                            "Bruno" using White,
                            "Tim" using Black,
                            "Nele" using Orange,
                            "Jan" using Purple)

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
                    .withPlayers("Bruno" using White, "Tim" using Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.determinePlayOrder(Orange, Black) }

            assertThat(eventStream.filterEvents<PlayerOrderDetermined>()).isEmpty()
        }

        @Test
        fun `Cannot determine a player order with two same colors`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayers("Bruno" using White, "Tim" using Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.determinePlayOrder(Black, Black) }
                    .withMessage("Transition requires player colors to be unique")

            assertThat(eventStream.filterEvents<PlayerOrderDetermined>()).isEmpty()
        }

        @Test
        fun `Can determine a player order when at least two players joined`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayers("Bruno" using White, "Tim" using Black)

            magnumSal.determinePlayOrder(White, Black)

            assertThat(eventStream).contains(PlayerOrderDetermined(White, Black))
        }

        @Test
        fun `Can determine a player order for three players`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayers("Bruno" using White,
                            "Tim" using Black,
                            "Snarf" using Purple)

            magnumSal.determinePlayOrder(Black, Purple, White)

            assertThat(eventStream).contains(PlayerOrderDetermined(Black, Purple, White))
        }

        @Test
        fun `Can determine a player order for four players`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayers("Bruno" using White,
                            "Tim" using Black,
                            "Gargamel" using Orange,
                            "Snarf" using Purple)

            magnumSal.determinePlayOrder(Orange, Black, Purple, White)

            assertThat(eventStream).contains(PlayerOrderDetermined(Orange, Black, Purple, White))
        }
    }

    @Nested
    inner class PlacingWorkersInTheMine {
        @Test
        fun `Can place a worker in Shaft 1`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)

            magnumSal.placeWorkerInMine(White, at(1, 0))

            assertThat(eventStream).contains(MinerPlaced(White, at(1, 0)))
        }

        @Test
        fun `Cannot place a worker in Shaft 2 when Shaft 1 is unoccupied`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(White, at(2, 0)) }

            assertThat(eventStream).doesNotContain(MinerPlaced(White, at(2, 0)))
        }

        @Test
        fun `Cannot place two workers in one turn`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)

            magnumSal.placeWorkerInMine(White, at(1, 0))
            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(White, at(2, 0)) }

            assertThat(eventStream)
                    .contains(MinerPlaced(White, at(1, 0)))
                    .doesNotContain(MinerPlaced(White, at(2, 0)))
        }

        @Test
        fun `Cannot place a worker when it's not your turn`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(Black, at(1, 0)) }

            assertThat(eventStream)
                    .doesNotContain(MinerPlaced(Black, at(1, 0)))
        }

        @Test
        fun `Cannot place a worker when you're out of workers`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
                    .distributeWorkersInTheMineShaft(5, listOf(White, Black))

            visualize(Miners.from(eventStream))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(White, at(6, 0)) }
                    .withMessage("Transition requires you to have enough available workers")

            assertThat(eventStream).doesNotContain(
                    MinerPlaced(White, at(6, 0))
            )
        }

        @Test
        fun `Second player can place a worker in shaft 1`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))

            magnumSal.placeWorkerInMine(Black, at(1, 0))

            assertThat(eventStream).contains(MinerPlaced(Black, at(1, 0)))
        }

        @Test
        fun `Second player can place a worker in shaft 2`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))

            magnumSal.placeWorkerInMine(Black, at(2, 0))

            assertThat(eventStream).contains(MinerPlaced(Black, at(2, 0)))
        }

        @Test
        fun `Second player cannot place a worker in shaft 3`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(Black, at(3, 0)) }

            assertThat(eventStream).doesNotContain(MinerPlaced(Black, at(3, 0)))
        }

        @Test
        fun `Players can fill up the shaft`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)

            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(3, 0))
            magnumSal.placeWorkerInMine(Black, at(4, 0))
            magnumSal.placeWorkerInMine(White, at(5, 0))
            magnumSal.placeWorkerInMine(Black, at(6, 0))

            assertThat(eventStream).contains(
                    MinerPlaced(White, at(1, 0)),
                    MinerPlaced(Black, at(2, 0)),
                    MinerPlaced(White, at(3, 0)),
                    MinerPlaced(Black, at(4, 0)),
                    MinerPlaced(White, at(5, 0)),
                    MinerPlaced(Black, at(6, 0))
            )
        }
    }

    @Nested
    inner class RemovingMinersFromTheMine {
        @Test
        fun `can remove worker if it does not break the chain`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(1, 0))

            magnumSal.removeWorkerFromMine(White, at(1, 0))

            assertThat(eventStream).contains(MinerRemoved(White, at(1, 0)))
        }

        @Test
        fun `cannot remove worker if it would break the chain`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.removeWorkerFromMine(White, at(1, 0)) }

            assertThat(eventStream).doesNotContain(MinerRemoved(White, at(1, 0)))
        }

        @Test
        fun `cannot remove worker if it would break the chain 2nd scenario`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(1, 0))
            magnumSal.placeWorkerInMine(White, at(2, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.removeWorkerFromMine(White, at(1, 0))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.removeWorkerFromMine(Black, at(1, 0)) }

            assertThat(eventStream).doesNotContain(MinerRemoved(Black, at(1, 0)))
        }

        @Test
        fun `cannot remove worker if it's not your turn`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(1, 0))
            magnumSal.removeWorkerFromMine(White, at(1, 0))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.removeWorkerFromMine(White, at(1, 0)) }

            assertThat(eventStream).containsOnlyOnce(MinerRemoved(White, at(1, 0)))
        }
    }

    @Nested
    inner class CheckingForAvailableWorkers {
        @Test
        fun `No more workers after placing and removing miners`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
                    .distributeWorkersInTheMineShaft(5, listOf(White, Black))
            magnumSal.removeWorkerFromMine(White, at(5, 0))
            magnumSal.removeWorkerFromMine(Black, at(5, 0))

            visualize(Miners.from(eventStream))

            magnumSal.placeWorkerInMine(White, at(5, 0))
            magnumSal.placeWorkerInMine(Black, at(5, 0))

            visualize(Miners.from(eventStream))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.placeWorkerInMine(White, at(6, 0)) }
                    .withMessage("Transition requires you to have enough available workers")

            assertThat(eventStream).doesNotContain(
                    MinerPlaced(White, at(6, 0))
            )
        }
    }
}
