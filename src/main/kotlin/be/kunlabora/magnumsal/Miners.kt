package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.exception.transitionRequires

class Miners private constructor(private val _miners: List<Miner>) : List<Miner> by _miners {

    private val occupation
        get() = _miners.map{ it.at }

    fun attemptPlacingAMiner(player: PlayerColor, at: PositionInMine) {
        transitionRequires("the previous position to be occupied") {
            placingWouldNotBreakTheChain(Miner(player, at))
        }
    }

    fun attemptRemovingAMiner(player: PlayerColor, at: PositionInMine) {
        transitionRequires("the chain not to be broken") {
            removingWouldNotBreakTheChain(Miner(player, at))
        }
    }

    private fun placingWouldNotBreakTheChain(miner: Miner) =
            miner.at.isTheTop() || isThereAMinerAt(miner.at.higher())

    private fun removingWouldNotBreakTheChain(miner: Miner) =
            isItTheLastMinerInTheChain(miner) || areThereOtherMinersAt(miner.at)

    private fun isItTheLastMinerInTheChain(miner: Miner) =
            miner.at.deeper() !in occupation

    private fun areThereOtherMinersAt(position: PositionInMine) =
            occupation.count { it == position } > 1

    private fun isThereAMinerAt(position: PositionInMine) =
            position in occupation

    companion object {
        fun from(eventStream: EventStream): Miners {
            val miners: List<Miner> = eventStream.filterEvents<MagnumSalEvent>().fold(emptyList()) { acc, event ->
                when (event) {
                    is MagnumSalEvent.MinerRemoved -> Miner.asMinerInShaft(event)?.let { acc - it } ?: acc
                    is MagnumSalEvent.MinerPlaced -> Miner.asMinerInShaft(event)?.let { acc + it } ?: acc
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
        fun asMinerInShaft(event: MagnumSalEvent): Miner? = when (event) {
            is MagnumSalEvent.MinerRemoved -> Miner(event.player, event.positionInMine)
            is MagnumSalEvent.MinerPlaced -> Miner(event.player, event.positionInMine)
            else -> null
        }
    }
}
