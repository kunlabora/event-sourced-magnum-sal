package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.exception.transitionRequires

class WorkerLimitRule(private val eventStream: EventStream) {

    private val players
        get() = eventStream.filterEvents<MagnumSalEvent.PlayerJoined>()

    private val workersAtStart: Int
        get() = if (amountOfPlayers == 2) 5 else 4

    private val amountOfPlayers
        get() = players.count()

    private val minersPlaced
        get() = eventStream.filterEvents<MagnumSalEvent.MinerPlaced>()
    private val minersRemoved
        get() = eventStream.filterEvents<MagnumSalEvent.MinerRemoved>()


    fun requirePlayerToHaveEnoughWorkers(player: PlayerColor) {
        transitionRequires("you to have enough available workers") {
            hasEnoughWorkersInPool(player)
        }
    }

    private fun hasEnoughWorkersInPool(player: PlayerColor): Boolean {
        val minersPlacedBy = minersPlaced.count { it.player == player }
        val minersRemovedBy = minersRemoved.count { it.player == player }
        return (minersPlacedBy - minersRemovedBy) < workersAtStart
    }


}
