package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.exception.transitionRequires

class MineShaft private constructor(private val _miners: List<MinerInShaft>) : List<MinerInShaft> by _miners {

    private val occupation
        get() = _miners.map{ it.at }

    fun attemptPlacingAMiner(player: PlayerColor, at: PositionInMine) {
        transitionRequires("the previous position to be occupied") {
            placingWouldNotBreakTheChain(MinerInShaft(player, at))
        }
    }

    fun attemptRemovingAMiner(player: PlayerColor, at: PositionInMine) {
        transitionRequires("the chain not to be broken") {
            removingWouldNotBreakTheChain(MinerInShaft(player, at))
        }
    }

    private fun placingWouldNotBreakTheChain(minerInShaft: MinerInShaft) =
            minerInShaft.at.isTheTop() || isThereAMinerAt(minerInShaft.at.higher())

    private fun removingWouldNotBreakTheChain(minerInShaft: MinerInShaft) =
            isItTheLastMinerInTheChain(minerInShaft) || areThereOtherMinersAt(minerInShaft.at)

    private fun isItTheLastMinerInTheChain(miner: MinerInShaft) =
            miner.at.deeper() !in occupation

    private fun areThereOtherMinersAt(position: PositionInMine) =
            occupation.count { it == position } > 1

    private fun isThereAMinerAt(position: PositionInMine) =
            position in occupation

    companion object {
        fun from(eventStream: EventStream): MineShaft {
            val miners: List<MinerInShaft> = eventStream.filterEvents<MagnumSalEvent>().fold(emptyList()) { acc, event ->
                when (event) {
                    is MagnumSalEvent.MinerRemoved -> MinerInShaft.asMinerInShaft(event)?.let { acc - it } ?: acc
                    is MagnumSalEvent.MinerPlaced -> MinerInShaft.asMinerInShaft(event)?.let { acc + it } ?: acc
                    else -> acc
                }
            }
            return MineShaft(miners)
        }
    }
}

data class MinerInShaft(val player: PlayerColor, val at: PositionInMine) {
    override fun toString(): String = "$player at $at"

    companion object {
        fun asMinerInShaft(event: MagnumSalEvent): MinerInShaft? = when (event) {
            is MagnumSalEvent.MinerRemoved -> MinerInShaft(event.player, event.positionInMine)
            is MagnumSalEvent.MinerPlaced -> MinerInShaft(event.player, event.positionInMine)
            else -> null
        }
    }
}
