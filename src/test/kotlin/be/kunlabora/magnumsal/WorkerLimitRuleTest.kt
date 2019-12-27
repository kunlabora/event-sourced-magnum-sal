package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.PlayerColor.Black
import be.kunlabora.magnumsal.PlayerColor.White
import be.kunlabora.magnumsal.PositionInMine.Companion.at
import be.kunlabora.magnumsal.exception.IllegalTransitionException
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class WorkerLimitRuleTest {

    private lateinit var eventStream: EventStream

    @BeforeEach
    internal fun setUp() {
        eventStream = EventStream()
    }

    @Test
    fun `Given no extra workers hired, no more workers after placing and removing miners`() {
        val magnumSal = TestMagnumSal(eventStream)
                .withPlayersInOrder("Bruno" using White, "Tim" using Black)
                .distributeWorkersInTheMineShaft(5, listOf(White, Black))
                .build()
        val workerLimitRule = WorkerLimitRule(eventStream)
        magnumSal.removeWorkerFromMine(White, at(5, 0))
        magnumSal.placeWorkerInMine(White, at(5, 0))

        magnumSal.removeWorkerFromMine(Black, at(5, 0))
        magnumSal.placeWorkerInMine(Black, at(5, 0))

        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy { workerLimitRule.requirePlayerToHaveEnoughWorkers(White) }
                .withMessage("Transition requires you to have enough available workers")
    }
}
