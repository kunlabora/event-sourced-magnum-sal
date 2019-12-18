package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.exception.transitionRequires

class TurnOrderRule(private val eventStream: EventStream) {

    private val players
        get() = eventStream.filterEvents<MagnumSalEvent.PlayerJoined>()

    private val amountOfPlayers
        get() = players.count()

    private val turnOrder
        get() =
            eventStream.filterEvents<MagnumSalEvent.PlayerOrderDetermined>().single()
                    .let { listOfNotNull(it.player1, it.player2, it.player3, it.player4) }

    private val minersPlaced
        get() = eventStream.filterEvents<MagnumSalEvent.MinerPlaced>()
    private val minersRemoved
        get() = eventStream.filterEvents<MagnumSalEvent.MinerRemoved>()
    private val playerActions
        get() = minersPlaced.map { it.player } + minersRemoved.map { it.player }

    private fun itIsTheTurnOf(player: PlayerColor): Boolean {
        return playerActions.count() % amountOfPlayers == turnOrder.indexOf(player)
    }

    fun onlyInPlayersTurn(player: PlayerColor, block: () -> Unit): Any {
        transitionRequires("it to be your turn") {
            itIsTheTurnOf(player)
        }
        return block()
    }
}