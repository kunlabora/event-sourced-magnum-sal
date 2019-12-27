package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.PlayerColor.*
import be.kunlabora.magnumsal.PositionInMine.Companion.at
import be.kunlabora.magnumsal.exception.IllegalTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class TurnOrderRuleTest {
    private lateinit var eventStream: EventStream

    @BeforeEach
    internal fun setUp() {
        eventStream = EventStream()
    }

    @Test
    fun `Illegal case in a game with 2 players, first player goes twice in first round`() {
        val magnumSal = TestMagnumSal(eventStream)
                .withPlayersInOrder("Bruno" using White, "Tim" using Black)
                .build()

        magnumSal.placeWorkerInMine(White, at(1, 0))

        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy {
                    TurnOrderRule(eventStream).onlyInPlayersTurn(White) {
                        fail { "this should not be executed, because it's not $White's turn" }
                    }
                }
                .withMessage("Transition requires it to be your turn")
    }

    @Test
    fun `Illegal case in a game with 2 players, second player goes twice in first round`() {
        val magnumSal = TestMagnumSal(eventStream)
                .withPlayersInOrder("Bruno" using White, "Tim" using Black)
                .build()

        magnumSal.placeWorkerInMine(White, at(1, 0))
        magnumSal.placeWorkerInMine(Black, at(1, 0))

        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy {
                    TurnOrderRule(eventStream).onlyInPlayersTurn(Black) {
                        fail { "this should not be executed, because it's not $Black's turn" }
                    }
                }
                .withMessage("Transition requires it to be your turn")
    }

    @Test
    fun `Illegal case in a game with 3 players, third player goes twice in first round`() {
        val magnumSal = TestMagnumSal(eventStream)
                .withPlayersInOrder("Bruno" using White, "Tim" using Black, "Snarf" using Orange)
                .build()

        magnumSal.placeWorkerInMine(White, at(1, 0))
        magnumSal.placeWorkerInMine(Black, at(1, 0))
        magnumSal.placeWorkerInMine(Orange, at(1, 0))

        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy {
                    TurnOrderRule(eventStream).onlyInPlayersTurn(Orange) {
                        fail { "this should not be executed, because it's not $Orange's turn" }
                    }
                }
                .withMessage("Transition requires it to be your turn")

        assertThat(eventStream).containsOnlyOnce(MagnumSalEvent.MinerPlaced(Orange, at(1, 0)))
    }

    @Test
    fun `Illegal case in a game with 4 players, fourth player goes twice in first round`() {
        val magnumSal = TestMagnumSal(eventStream)
                .withPlayersInOrder("Bruno" using White, "Tim" using Black, "Snarf" using Orange, "Gargamel" using Purple)
                .build()

        magnumSal.placeWorkerInMine(White, at(1, 0))
        magnumSal.placeWorkerInMine(Black, at(1, 0))
        magnumSal.placeWorkerInMine(Orange, at(1, 0))
        magnumSal.placeWorkerInMine(Purple, at(1, 0))

        assertThatExceptionOfType(IllegalTransitionException::class.java)
                .isThrownBy {
                    TurnOrderRule(eventStream).onlyInPlayersTurn(Orange) {
                        fail { "this should not be executed, because it's not $Purple's turn" }
                    }
                }
                .withMessage("Transition requires it to be your turn")

        assertThat(eventStream).containsOnlyOnce(MagnumSalEvent.MinerPlaced(Purple, at(1, 0)))
    }

    @Test
    fun `Legal case in a game with 2 players, first player goes twice after first round`() {
        val magnumSal = TestMagnumSal(eventStream)
                .withPlayersInOrder("Bruno" using White, "Tim" using Black)
                .build()

        magnumSal.placeWorkerInMine(White, at(1, 0))
        magnumSal.placeWorkerInMine(Black, at(1, 0))
        magnumSal.placeWorkerInMine(White, at(2, 0))

        var success = false
        TurnOrderRule(eventStream).onlyInPlayersTurn(White) { success = true }

        assertThat(success).isTrue()
    }

    @Test
    fun `Legal case in a game with 2 players, second player goes twice after first round`() {
        val magnumSal = TestMagnumSal(eventStream)
                .withPlayersInOrder("Bruno" using White, "Tim" using Black)
                .build()

        magnumSal.placeWorkerInMine(White, at(1, 0))
        magnumSal.placeWorkerInMine(Black, at(1, 0))
        magnumSal.placeWorkerInMine(White, at(2, 0))
        magnumSal.placeWorkerInMine(White, at(2, 0))

        var success = false
        TurnOrderRule(eventStream).onlyInPlayersTurn(Black) { success = true }

        assertThat(success).isTrue()
    }

    @Test
    fun `Legal case in a game with 4 players, fourth player goes twice after first round`() {
        val magnumSal = TestMagnumSal(eventStream)
                .withPlayersInOrder("Bruno" using White, "Tim" using Black, "Snarf" using Orange, "Azrael" using Purple)
                .build()

        magnumSal.placeWorkerInMine(White, at(1, 0))
        magnumSal.placeWorkerInMine(Black, at(1, 0))
        magnumSal.placeWorkerInMine(Orange, at(1, 0))
        magnumSal.placeWorkerInMine(Purple, at(1, 0))
        magnumSal.placeWorkerInMine(White, at(2, 0))
        magnumSal.placeWorkerInMine(White, at(2, 0))
        magnumSal.placeWorkerInMine(Black, at(2, 0))
        magnumSal.placeWorkerInMine(Black, at(2, 0))
        magnumSal.placeWorkerInMine(Orange, at(2, 0))
        magnumSal.placeWorkerInMine(Orange, at(2, 0))

        var success = false
        TurnOrderRule(eventStream).onlyInPlayersTurn(Purple) { success = true }

        assertThat(success).isTrue()
    }
}
