package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.Event.*
import be.kunlabora.magnumsal.exception.transitionRequires

class MagnumSal(private val eventStream: EventStream) {
    fun addPlayer(name: String, color: PlayerColor) {
        transitionRequires("the same color not to have been picked already") {
            color !in eventStream.filterIsInstance<PlayerJoined>().map { it.color }
        }
        transitionRequires("a maximum of one player to have joined") {
            eventStream.filterIsInstance<PlayerJoined>().count() <= 1
        }
        eventStream.push(PlayerJoined(name, color))
    }

    //TODO later: add player3: PlayerColor? = null, player4 : PlayerColor? = null
    fun determinePlayOrder(player1: PlayerColor, player2: PlayerColor) {
        require(player1 != player2) { "Play order can only be determined with different PlayerColors" }
        transitionRequires("at least 2 players") {
            eventStream.filterIsInstance<PlayerJoined>().count() >= 2
        }
        transitionRequires("player colors to have been picked") {
            player1 in eventStream.filterIsInstance<PlayerJoined>().map { it.color }
                    && player2 in eventStream.filterIsInstance<PlayerJoined>().map { it.color }
        }
        eventStream.push(PlayerOrderDetermined(player1, player2))
    }

    fun placeWorkerInMine(player: PlayerColor, mineShaftPosition: MineShaftPosition) {
        transitionRequires("the previous MineShaft Position to be occupied") {
            aMinerIsGoingToBePlacedAtTheTop(mineShaftPosition) || aMinerWasPlacedAt(mineShaftPosition.previous())
        }
        transitionRequires("it to be your turn") {
            itIsTheTurnOfPlayer(player)
        }
        eventStream.push(MinerPlaced(player, mineShaftPosition))
    }

    private fun itIsTheTurnOfPlayer(player: PlayerColor): Boolean {
        val isFirstPlayer = eventStream.filterIsInstance<PlayerOrderDetermined>().single().player1 == player
        val playerActionsCount = eventStream.filterIsInstance<MinerPlaced>().filter { it.player == player }.count()
        val opponentActionsCount = eventStream.filterIsInstance<MinerPlaced>().filter { it.player != player }.count()

        return (isFirstPlayer && opponentActionsCount == playerActionsCount)
                || (!isFirstPlayer && opponentActionsCount > playerActionsCount)
    }

    private fun aMinerIsGoingToBePlacedAtTheTop(currentMineShaftPosition: MineShaftPosition) =
            (currentMineShaftPosition.index == 1)

    private fun aMinerWasPlacedAt(currentMineShaftPosition: MineShaftPosition) =
            (currentMineShaftPosition in eventStream.filterIsInstance<MinerPlaced>().map { it.mineShaftPosition })
}

data class MineShaftPosition(val index: Int) {
    fun previous(): MineShaftPosition {
        return MineShaftPosition(index - 1)
    }
}

sealed class PlayerColor {
    object Black : PlayerColor()
    object White : PlayerColor()
    object Orange : PlayerColor()
    object Purple : PlayerColor()
}

