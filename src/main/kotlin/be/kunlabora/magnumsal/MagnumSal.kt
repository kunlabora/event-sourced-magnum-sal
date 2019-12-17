package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.*
import be.kunlabora.magnumsal.exception.transitionRequires

sealed class MagnumSalEvent : Event {
    data class PlayerOrderDetermined(val player1: PlayerColor, val player2: PlayerColor, val player3: PlayerColor? = null, val player4: PlayerColor? = null) : MagnumSalEvent()
    data class PlayerJoined(val name: String, val color: PlayerColor) : MagnumSalEvent()
    data class MinerPlaced(val player: PlayerColor, val mineShaftPosition: MineShaftPosition) : MagnumSalEvent()
    data class MinerRemoved(val player: PlayerColor, val mineShaftPosition: MineShaftPosition) : MagnumSalEvent()
}

class MagnumSal(private val eventStream: EventStream) {

    private val players
        get() = eventStream.filterEvents<PlayerJoined>()

    private val amountOfPlayers
        get() = players.count()

    private val turnOrder
        get() =
            eventStream.filterEvents<PlayerOrderDetermined>().single()
                    .let { listOfNotNull(it.player1, it.player2, it.player3, it.player4) }

    private val minersPlaced
        get() = eventStream.filterEvents<MinerPlaced>()
    private val minersRemoved
        get() = eventStream.filterEvents<MinerRemoved>()
    private val playerActions
        get() = minersPlaced.map { it.player } + minersRemoved.map { it.player }

    private val mineShaft: MineShaft
        get() = MineShaft.from(eventStream)


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

    fun placeWorkerInMine(player: PlayerColor, at: MineShaftPosition) {
        requireItToBeTheTurnOf(player)
        mineShaft.attemptPlacingAMiner(player, at)
        eventStream.push(MinerPlaced(player, at))
    }

    fun removeWorkerFromMine(player: PlayerColor, mineShaftPosition: MineShaftPosition) {
        requireItToBeTheTurnOf(player)
        mineShaft.attemptRemovingAMiner(player, mineShaftPosition)
        eventStream.push(MinerRemoved(player, mineShaftPosition))
    }

    private fun requireItToBeTheTurnOf(player: PlayerColor) {
        transitionRequires("it to be your turn") {
            itIsTheTurnOf(player)
        }
    }

    private fun itIsTheTurnOf(player: PlayerColor): Boolean {
        return playerActions.count() % amountOfPlayers == turnOrder.indexOf(player)
    }
}

sealed class PlayerColor {
    object Black : PlayerColor()
    object White : PlayerColor()
    object Orange : PlayerColor()
    object Purple : PlayerColor()

    override fun toString(): String = this.javaClass.simpleName
}

