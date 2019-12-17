package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.*
import be.kunlabora.magnumsal.exception.transitionRequires

sealed class MagnumSalEvent : Event {
    data class PlayerOrderDetermined(val player1: PlayerColor, val player2: PlayerColor) : MagnumSalEvent()
    data class PlayerJoined(val name: String, val color: PlayerColor) : MagnumSalEvent()
    data class MinerPlaced(val player: PlayerColor, val mineShaftPosition: MineShaftPosition) : MagnumSalEvent()
    data class MinerRemoved(val player: PlayerColor, val mineShaftPosition: MineShaftPosition) : MagnumSalEvent()
}

class MagnumSal(private val eventStream: EventStream) {

    private val players
        get() = eventStream.filterEvents<PlayerJoined>()

    private val amountOfPlayers
        get() = players.count()

    private val firstPlayer
        get() = eventStream.filterEvents<PlayerOrderDetermined>().single().player1

    private val minersPlaced
        get() = eventStream.filterEvents<MinerPlaced>()
    private val minersRemoved
        get() = eventStream.filterEvents<MinerRemoved>()
    private val playerActions
        get() = minersPlaced.map { it.player } + minersRemoved.map { it.player }

    private val mineShaftOccupation: MineShaftOccupation
        get() = MineShaftOccupation.from(eventStream)

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

    fun placeWorkerInMine(player: PlayerColor, at: MineShaftPosition) {
        requireItToBeTheTurnOf(player)
        mineShaftOccupation.attemptPlacingAMiner(player, at)
        eventStream.push(MinerPlaced(player, at))
    }

    fun removeWorkerFromMine(player: PlayerColor, mineShaftPosition: MineShaftPosition) {
        requireItToBeTheTurnOf(player)
        mineShaftOccupation.attemptRemovingAMiner(player, mineShaftPosition)
        eventStream.push(MinerRemoved(player, mineShaftPosition))
    }

    private fun requireItToBeTheTurnOf(player: PlayerColor) {
        transitionRequires("it to be your turn") {
            itIsTheTurnOf(player)
        }
    }

    private fun itIsTheTurnOf(player: PlayerColor): Boolean {
        val isFirstPlayer = firstPlayer == player
        val playerActionsCount = playerActions.filter { it == player }.count()
        val opponentActionsCount = playerActions.filter { it != player }.count()

        return (isFirstPlayer && opponentActionsCount == playerActionsCount)
                || (!isFirstPlayer && opponentActionsCount > playerActionsCount)
    }
}

sealed class PlayerColor {
    object Black : PlayerColor()
    object White : PlayerColor()
    object Orange : PlayerColor()
    object Purple : PlayerColor()
    override fun toString(): String = this.javaClass.simpleName
}

