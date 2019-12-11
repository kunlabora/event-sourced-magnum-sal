package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.Event.*
import be.kunlabora.magnumsal.exception.transitionRequires

class MagnumSal(private val eventStream: EventStream) {

    private val players
        get() = eventStream.filterIsInstance<PlayerJoined>()

    private val amountOfPlayers
        get() = players.count()

    private val firstPlayer
        get() = eventStream.filterIsInstance<PlayerOrderDetermined>().single().player1

    private val playerActions
        get() = eventStream.filterIsInstance<MinerPlaced>()

    fun addPlayer(name: String, color: PlayerColor) {
        transitionRequires("the same color not to have been picked already") {
            color !in players.map { it.color }
        }
        transitionRequires("a maximum of one player to have joined") {
            amountOfPlayers <= 1
        }
        eventStream.push(PlayerJoined(name, color))
    }

    //TODO later: add player3: PlayerColor? = null, player4 : PlayerColor? = null
    fun determinePlayOrder(player1: PlayerColor, player2: PlayerColor) {
        require(player1 != player2) { "Play order can only be determined with different PlayerColors" }
        transitionRequires("at least 2 players") {
            amountOfPlayers >= 2
        }
        transitionRequires("player colors to have been picked") {
            player1 in players.map { it.color }
                    && player2 in players.map { it.color }
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
        val isFirstPlayer = firstPlayer == player
        val playerActionsCount = playerActions.filter { it.player == player }.count()
        val opponentActionsCount = playerActions.filter { it.player != player }.count()

        return (isFirstPlayer && opponentActionsCount == playerActionsCount)
                || (!isFirstPlayer && opponentActionsCount > playerActionsCount)
    }

    private fun aMinerIsGoingToBePlacedAtTheTop(currentMineShaftPosition: MineShaftPosition) =
            (currentMineShaftPosition.index == 1)

    private fun aMinerWasPlacedAt(position: MineShaftPosition) =
            (position in playerActions.map { it.mineShaftPosition })

    fun removeWorkerFromMine(player: PlayerColor, mineShaftPosition: MineShaftPosition) {
        transitionRequires("the chain not to be broken") {
            !aMinerWasPlacedAt(mineShaftPosition.next())
        }
        eventStream.push(MinerRemoved(player, mineShaftPosition))
    }
}

data class MineShaftPosition(val index: Int) {
    init {
        require(index in 1..6) { "Mine shaft is only 6 deep" }
    }

    fun previous(): MineShaftPosition {
        return MineShaftPosition(index - 1)
    }

    fun next(): MineShaftPosition {
        return MineShaftPosition(index + 1)
    }
}

sealed class PlayerColor {
    object Black : PlayerColor()
    object White : PlayerColor()
    object Orange : PlayerColor()
    object Purple : PlayerColor()
}

