package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.*
import be.kunlabora.magnumsal.exception.transitionRequires

sealed class MagnumSalEvent : Event {
    data class PlayerOrderDetermined(val player1: PlayerColor, val player2: PlayerColor, val player3: PlayerColor? = null, val player4: PlayerColor? = null) : MagnumSalEvent()
    data class PlayerJoined(val name: String, val color: PlayerColor) : MagnumSalEvent()
    data class MinerPlaced(val player: PlayerColor, val positionInMine: PositionInMine) : MagnumSalEvent()
    data class MinerRemoved(val player: PlayerColor, val positionInMine: PositionInMine) : MagnumSalEvent()
}

class MagnumSal(private val eventStream: EventStream) {

    private val turnOrderRule: TurnOrderRule = TurnOrderRule(eventStream)

    private val players
        get() = eventStream.filterEvents<PlayerJoined>()

    private val workersAtStart: Int
        get() = if (amountOfPlayers == 2) 5 else 4

    private val amountOfPlayers
        get() = players.count()

    private val minersPlaced
        get() = eventStream.filterEvents<MinerPlaced>()
    private val minersRemoved
        get() = eventStream.filterEvents<MinerRemoved>()

    private val miners: Miners
        get() = Miners.from(eventStream)


    fun addPlayer(name: String, color: PlayerColor) {
        transitionRequires("the same color not to have been picked already") {
            color !in players.map { it.color }
        }
        eventStream.push(PlayerJoined(name, color))
    }

    fun determinePlayOrder(player1: PlayerColor,
                           player2: PlayerColor,
                           player3: PlayerColor? = null,
                           player4: PlayerColor? = null) {
        val colors = setOf(player1, player2, player3, player4).filterNotNull()
        transitionRequires("player colors to be unique") {
            colors.size == listOfNotNull(player1, player2, player3, player4).size
        }
        transitionRequires("at least 2 players") {
            amountOfPlayers >= 2
        }
        transitionRequires("player colors to have been picked") {
            val players = players.map { it.color }.toSet()
            colors.intersect(players).size == players.size
        }
        eventStream.push(PlayerOrderDetermined(player1, player2, player3, player4))
    }

    fun placeWorkerInMine(player: PlayerColor, at: PositionInMine) = onlyInPlayersTurn(player) {
        requirePlayerToHaveEnoughWorkers(player)
        miners.attemptPlacingAMiner(player, at)
        eventStream.push(MinerPlaced(player, at))
    }

    fun removeWorkerFromMine(player: PlayerColor, positionInMine: PositionInMine) = onlyInPlayersTurn(player) {
        miners.attemptRemovingAMiner(player, positionInMine)
        eventStream.push(MinerRemoved(player, positionInMine))
    }

    private fun requirePlayerToHaveEnoughWorkers(player: PlayerColor) {
        transitionRequires("you to have enough available workers") {
            hasEnoughWorkersInPool(player)
        }
    }

    private fun hasEnoughWorkersInPool(player: PlayerColor): Boolean {
        val minersPlacedBy = minersPlaced.count { it.player == player }
        val minersRemovedBy = minersRemoved.count { it.player == player }
        return (minersPlacedBy - minersRemovedBy) < workersAtStart
    }

    private fun onlyInPlayersTurn(player: PlayerColor, block: () -> Unit): Any = turnOrderRule.onlyInPlayersTurn(player, block)
}

sealed class PlayerColor {
    object Black : PlayerColor()
    object White : PlayerColor()
    object Orange : PlayerColor()
    object Purple : PlayerColor()

    override fun toString(): String = this.javaClass.simpleName
}

