package be.kunlabora.magnumsal

class Miners private constructor(private val _miners: List<Miner>) : List<Miner> by _miners {

    companion object {
        fun from(eventStream: EventStream): Miners {
            val miners: List<Miner> = eventStream.filterEvents<MagnumSalEvent>().fold(emptyList()) { acc, event ->
                when (event) {
                    is MagnumSalEvent.MinerRemoved -> Miner.from(event)?.let { acc - it } ?: acc
                    is MagnumSalEvent.MinerPlaced -> Miner.from(event)?.let { acc + it } ?: acc
                    else -> acc
                }
            }
            return Miners(miners)
        }
    }
}

data class Miner(val player: PlayerColor, val at: PositionInMine) {
    override fun toString(): String = "$player at $at"

    companion object {
        fun from(event: MagnumSalEvent): Miner? = when (event) {
            is MagnumSalEvent.MinerRemoved -> Miner(event.player, event.positionInMine)
            is MagnumSalEvent.MinerPlaced -> Miner(event.player, event.positionInMine)
            else -> null
        }
    }
}
