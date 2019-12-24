package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.MinerPlaced
import be.kunlabora.magnumsal.MagnumSalEvent.MinerRemoved
import be.kunlabora.magnumsal.MinerMovement.PlaceMiner
import be.kunlabora.magnumsal.MinerMovement.RemoveMiner
import be.kunlabora.magnumsal.PlayerColor.Black
import be.kunlabora.magnumsal.PlayerColor.White
import be.kunlabora.magnumsal.PositionInMine.Companion.at
import be.kunlabora.magnumsal.exception.IllegalTransitionException
import org.assertj.core.api.Assertions
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
            MagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black)
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(PlaceMiner(White, at(2, 0))) { fail("should not be executed") } }
        }

        @Test
        fun `Illegal case where placing a worker in the mineshaft at depth 3 without miners at depth 2 and 1 would break the chain`() {
            val magnumSal = MagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(PlaceMiner(Black, at(3, 0))) { fail("should not be executed") } }
        }
    }

    @Nested
    inner class RemovingMinersFromTheMine {
        @Test
        fun `Illegal case where removing a miner from the mineshaft would break the chain - simplest case`() {
            val magnumSal = MagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(RemoveMiner(White, at(1, 0))) { fail("should not be executed") } }
        }

        @Test
        fun `Illegal case with a history of placing and removing miners where removing a miner from the mineshaft would break the chain`() {
            val magnumSal = MagnumSal(eventStream).withPlayersInOrder("Bruno" using White, "Tim" using Black)
            magnumSal.placeWorkerInMine(White, at(1, 0))
            magnumSal.placeWorkerInMine(Black, at(1, 0))
            magnumSal.placeWorkerInMine(White, at(2, 0))
            magnumSal.placeWorkerInMine(Black, at(2, 0))
            magnumSal.removeWorkerFromMine(White, at(1, 0))
            val chainRule = ChainRule(eventStream)

            assertThatExceptionOfType(IllegalTransitionException::class.java)
                    .isThrownBy { chainRule.withoutBreakingTheChain(RemoveMiner(Black, at(1, 0))) { fail("should not be executed") } }
        }
    }
}
