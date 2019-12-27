package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.MinerPlaced
import be.kunlabora.magnumsal.MagnumSalEvent.MinerRemoved
import be.kunlabora.magnumsal.MinerMovement.PlaceMiner
import be.kunlabora.magnumsal.MinerMovement.RemoveMiner
import be.kunlabora.magnumsal.PlayerColor.Black
import be.kunlabora.magnumsal.PlayerColor.White
import be.kunlabora.magnumsal.PositionInMine.Companion.at
import be.kunlabora.magnumsal.exception.IllegalTransitionException
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChainRuleTest {

    private lateinit var eventStream: EventStream

    @BeforeEach
    internal fun setUp() {
        eventStream = EventStream()
    }

    @Nested
    inner class PlacingWorkersInTheMine {
        @Test
        fun `Illegal case where placing a worker in the mineshaft at depth 2 without a miner at the top would break the chain`() {
            TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(PlaceMiner(White, at(2, 0))) { fail("should not be executed") } }
        }

        @Test
        fun `Illegal case where placing a worker in the mineshaft at depth 3 without miners at depth 2 and 1 would break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(PlaceMiner(Black, at(3, 0))) { fail("should not be executed") } }
        }

        @Test
        fun `Illegal case where placing a worker in a corridor at depth 2 without a miner in the mineshaft at depth 2 would break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(PlaceMiner(Black, at(2, 1))) { fail("should not be executed") } }
        }

        @Test
        fun `Legal case where placing a worker in a corridor at depth 2 with a miner in the mineshaft at depth 2 would not break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            val chainRule = ChainRule(eventStream)

            chainRule.withoutBreakingTheChain(PlaceMiner(White, at(2, 1))) { eventStream.push(MinerPlaced(White, at(2, 1))) }

            assertThat(eventStream).containsOnlyOnce(MinerPlaced(White, at(2, 1)))
        }

        @Test
        fun `Illegal case where placing a worker 3 positions along the right corridor without a miner in a previous chamber of the same corridor would break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(2, 1))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(PlaceMiner(Black, at(2, 3))) { fail("should not be executed") } }
        }

        @Test
        fun `Legal case where placing a worker 3 positions along the right corridor with a miner in a previous chamber of the same corridor would break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(2, 1))
            magnumSal.placeWorkerInMine(White, at(2, 2))
            val chainRule = ChainRule(eventStream)

            chainRule.withoutBreakingTheChain(PlaceMiner(Black, at(2, 3))) { eventStream.push(MinerPlaced(Black, at(2,3))) }

            assertThat(eventStream).containsOnlyOnce(MinerPlaced(Black, at(2,3)))
        }

        @Test
        fun `Illegal case where placing a worker 3 positions along the left corridor without a miner in a previous chamber of the same corridor would break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(2, -1))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(PlaceMiner(Black, at(2, -3))) { fail("should not be executed") } }
        }

        @Test
        fun `Legal case where placing a worker 3 positions along the left corridor with a miner in a previous chamber of the same corridor would break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(2, -1))
            magnumSal.placeWorkerInMine(White, at(2, -2))
            val chainRule = ChainRule(eventStream)

            chainRule.withoutBreakingTheChain(PlaceMiner(Black, at(2, -3))) { eventStream.push(MinerPlaced(Black, at(2,-3))) }

            assertThat(eventStream).containsOnlyOnce(MinerPlaced(Black, at(2,-3)))
        }
    }

    @Nested
    inner class RemovingMinersFromTheMine {
        @Test
        fun `Illegal case where removing a miner from the mineshaft would break the chain - simplest case`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(RemoveMiner(White, at(1, 0))) { fail("should not be executed") } }
        }

        @Test
        fun `Illegal case with a history of placing and removing miners where removing a miner from the mineshaft would break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(1, 0))
            magnumSal.placeWorkerInMine(White, at(2, 0))
            magnumSal.removeWorkerFromMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(RemoveMiner(Black, at(1, 0))) { fail("should not be executed") } }
        }

        @Test
        fun `Legal case with a completely filled mineshaft where removing a miner from the bottom would not break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(3, 0))
            magnumSal.placeWorkerInMine(White, at(4, 0))
            magnumSal.placeWorkerInMine(Black, at(5, 0))
            magnumSal.placeWorkerInMine(Black, at(6, 0))
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(White, at(2, 0))
            val chainRule = ChainRule(eventStream)

            chainRule.withoutBreakingTheChain(RemoveMiner(Black, at(6, 0))) { eventStream.push(MinerRemoved(Black, at(6, 0))) }

            assertThat(eventStream).containsOnlyOnce(MinerRemoved(Black, at(6, 0)))
        }

        @Test
        fun `Illegal case where removing a miner from a corridor would break the chain - simplest case`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(2, 1))
            magnumSal.placeWorkerInMine(White, at(2, 2))
            magnumSal.placeWorkerInMine(Black, at(2, 3))
            magnumSal.placeWorkerInMine(Black, at(2, 4))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(RemoveMiner(White, at(2, 1))) { fail("should not be executed") } }
        }

        @Test
        fun `Illegal case where removing a miner from the mineshaft cross-section with an occupied right corridor would break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(2, 1))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(RemoveMiner(Black, at(2, 0))) { fail("should not be executed") } }
        }

        @Test
        fun `Illegal case where removing a miner from the mineshaft cross-section with an occupied left corridor would break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(2, -1))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(RemoveMiner(Black, at(2, 0))) { fail("should not be executed") } }
        }

        @Test
        fun `Legal case with a completely filled corridor where removing a miner from the end of the corridor would not break the chain`() {
            val magnumSal = TestMagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black).build()
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.placeWorkerInMine(White, at(2, 1))
            magnumSal.placeWorkerInMine(White, at(2, 2))
            magnumSal.placeWorkerInMine(Black, at(2, 3))
            magnumSal.placeWorkerInMine(Black, at(2, 4))
            magnumSal.placeWorkerInMine(White, at(2, 1))
            magnumSal.placeWorkerInMine(White, at(2, 2))
            val chainRule = ChainRule(eventStream)

            chainRule.withoutBreakingTheChain(RemoveMiner(Black, at(2, 4))) { eventStream.push(MinerRemoved(Black, at(2, 4))) }

            assertThat(eventStream).containsOnlyOnce(MinerRemoved(Black, at(2, 4)))
        }
    }
}
