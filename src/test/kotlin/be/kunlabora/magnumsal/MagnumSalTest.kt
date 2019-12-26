package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.*
import be.kunlabora.magnumsal.PlayerColor.*
import be.kunlabora.magnumsal.PositionInMine.Companion.at
import be.kunlabora.magnumsal.exception.IllegalTransitionException
import be.kunlabora.magnumsal.gamepieces.AllMineChamberTiles
import be.kunlabora.magnumsal.gamepieces.Level
import be.kunlabora.magnumsal.gamepieces.MineChamberTile
import be.kunlabora.magnumsal.gamepieces.Salt.*
import be.kunlabora.magnumsal.gamepieces.Salts
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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
        fun `Second player can also place a worker in shaft 1`() {
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

        @Test
        fun `cannot remove worker if there's no worker`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(1, 0))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.removeWorkerFromMine(White, at(2, 0)) }

            assertThat(eventStream).doesNotContain(MinerRemoved(White, at(2, 0)))
        }

        @Test
        fun `cannot remove worker if it's not your worker`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.removeWorkerFromMine(Black, at(1, 0)) }

            assertThat(eventStream).doesNotContain(MinerRemoved(Black, at(1, 0)))
        }
    }

    @Nested
    inner class ExploringTheMine {

        @Test
        fun `Placing a Miner on an undiscovered mine chamber tile, reveals the contents of the mine chamber`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(2, 1))

            assertThat(eventStream.filterEvents<MineChamberRevealed>().map { it.at })
                    .containsOnlyOnce(at(2,1))
        }

        @Test
        fun `Placing a second Miner on a discovered mine chamber tile, does not reveal the contents of the mine chamber again`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(2, 1))
            magnumSal.placeWorkerInMine(Black, at(2, 1))

            assertThat(eventStream.filterEvents<MineChamberRevealed>().map { it.at })
                    .containsOnlyOnce(at(2,1))
        }

        @Test
        fun `Uncovering all mine chamber tiles at depth 2, should only reveal level I mine chambers`() {
            MagnumSal(eventStream).revealAllLevelIMineChambers()

            val uncoveredLevelIChambers = eventStream.filterEvents<MineChamberRevealed>().map { it.tile }
            assertThat(uncoveredLevelIChambers)
                    .usingElementComparatorIgnoringFields("at")
                    .containsAll(AllMineChamberTiles.filter { it.level == Level.I })
                    .doesNotContainAnyElementsOf(AllMineChamberTiles.filter { it.level == Level.II })
                    .doesNotContainAnyElementsOf(AllMineChamberTiles.filter { it.level == Level.III })
        }

        @Test
        fun `Uncovering all mine chamber tiles at depth 4, should only reveal level II mine chambers`() {
            MagnumSal(eventStream).revealAllLevelIIMineChambers()

            val uncoveredLevelIChambers = eventStream.filterEvents<MineChamberRevealed>().map { it.tile }
            assertThat(uncoveredLevelIChambers)
                    .usingElementComparatorIgnoringFields("at")
                    .containsAll(AllMineChamberTiles.filter { it.level == Level.II })
                    .doesNotContainAnyElementsOf(AllMineChamberTiles.filter { it.level == Level.I })
                    .doesNotContainAnyElementsOf(AllMineChamberTiles.filter { it.level == Level.III })
        }

        @Test
        fun `Uncovering all mine chamber tiles at depth 6, should only reveal level III mine chambers`() {
            MagnumSal(eventStream).revealAllLevelIIIMineChambers()

            val uncoveredLevelIChambers = eventStream.filterEvents<MineChamberRevealed>().map { it.tile }
            assertThat(uncoveredLevelIChambers)
                    .usingElementComparatorIgnoringFields("at")
                    .containsAll(AllMineChamberTiles.filter { it.level == Level.III })
                    .doesNotContainAnyElementsOf(AllMineChamberTiles.filter { it.level == Level.I })
                    .doesNotContainAnyElementsOf(AllMineChamberTiles.filter { it.level == Level.II })
        }
    }

    @Nested
    inner class MiningFromTheMine {
        @Test
        fun `Cannot mine when it's not your turn`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1,0))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.mine(White, at(1,0), Salts(BROWN)) }
                    .withMessage("Transition requires it to be your turn")
        }

        @Test
        fun `Cannot mine from the mineshaft`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1,0))
            magnumSal.placeWorkerInMine(Black, at(2,0))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.mine(White, at(1,0), Salts(BROWN)) }
                    .withMessage("Transition requires you to mine from a MineChamber")
        }

        @Test
        fun `Cannot mine from an unrevealed Mine Chamber`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1,0))
            magnumSal.placeWorkerInMine(Black, at(2,0))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.mine(White, at(2,1), Salts(BROWN)) }
                    .withMessage("Transition requires you to mine from a revealed MineChamber")
        }

        @Test
        fun `Cannot mine from a Mine Chamber when no own miners present`() {
            val magnumSal = MagnumSal(eventStream)
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1,0))
            magnumSal.placeWorkerInMine(Black, at(2,0))
            magnumSal.placeWorkerInMine(White, at(2,1))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.mine(Black, at(2,1), Salts(BROWN)) }
                    .withMessage("Transition requires you to have a miner at ${at(2,1)}")
        }

        @Test
        fun `Cannot mine salt that is not in the Mine Chamber`() {
            val magnumSal = TestMagnumSal(eventStream)
                    .withOnlyMineChamberTilesOf(MineChamberTile(Level.I, Salts(BROWN), 0))
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1,0))
            magnumSal.placeWorkerInMine(Black, at(2,0))
            magnumSal.placeWorkerInMine(White, at(2,1))
            magnumSal.placeWorkerInMine(Black, at(2,2))
            magnumSal.placeWorkerInMine(White, at(2,1))
            magnumSal.placeWorkerInMine(Black, at(2,2))
            magnumSal.placeWorkerInMine(White, at(2,1))
            magnumSal.placeWorkerInMine(Black, at(2,2))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.mine(White, at(2,1), Salts(GREEN, GREEN, WHITE)) }
                    .withMessage("Transition requires there to be 2 Green salt, 1 White salt in ${at(2,1)}")
        }

        //Mining requires strength
        @Test
        fun `Cannot mine from a Mine Chamber with water when not enough own miners present`() {
            val magnumSal = TestMagnumSal(eventStream)
                    .withOnlyMineChamberTilesOf(MineChamberTile(Level.I, Salts(BROWN), 1))
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1,0))
            magnumSal.placeWorkerInMine(Black, at(2,0))
            magnumSal.placeWorkerInMine(White, at(2,1))
            magnumSal.placeWorkerInMine(Black, at(2,2))

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { magnumSal.mine(White, at(2,1), Salts(BROWN)) }
                    .withMessage("Transition requires you to have enough miners at ${at(2,1)}")
        }

        @Test
        fun `Mining from a Mine Chamber without water and with own miners present allows to mine an amount of salt equal to own miners`() {
            val magnumSal = TestMagnumSal(eventStream)
                    .withOnlyMineChamberTilesOf(MineChamberTile(Level.I, Salts(GREEN, GREEN, WHITE), 1))
                    .withPlayersInOrder("Bruno" using White, "Tim" using Black)
                    .withFourWhiteMinersAtFirstRightMineChamber()

            assertThat(Miners.from(eventStream).filter { it.player == White && it.at == at(2,1)}).hasSize(4)

            magnumSal.mine(White, at(2,1), Salts(GREEN, GREEN, WHITE))

            assertThat(eventStream).containsOnlyOnce(SaltMined(White, at(2,1), Salts(GREEN, GREEN, WHITE)))
        }

        //Mining tires miners
        @Test
        @Disabled
        fun `Mining from a Mine Chamber without water, tires miners that mined salt`() {

        }

        @Test
        @Disabled
        fun `Mining from a Mine Chamber with water, tires miners that mined salt and had to hold back the water`() {

        }

        //Mining costs zloty
        @Test
        @Disabled
        fun `Cannot mine from a Mine Chamber when player does not have enough money to pay the chain`() {

        }
    }
}
