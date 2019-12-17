package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.exception.transitionRequires

class MineShaft private constructor(private val _miners: List<MinerInShaft>) : List<MinerInShaft> by _miners {

    private val occupation
        get() = _miners.map{ it.at }

    fun attemptPlacingAMiner(player: PlayerColor, at: MineShaftPosition) {
        transitionRequires("the previous position to be occupied") {
            placingWouldNotBreakTheChain(MinerInShaft(player, at))
        }
    }

    fun attemptRemovingAMiner(player: PlayerColor, at: MineShaftPosition) {
        transitionRequires("the chain not to be broken") {
            removingWouldNotBreakTheChain(MinerInShaft(player, at))
        }
    }

    private fun placingWouldNotBreakTheChain(minerInShaft: MinerInShaft) =
            minerInShaft.at.isTheTop() || isThereAMinerAt(minerInShaft.at.previous())

    private fun removingWouldNotBreakTheChain(minerInShaft: MinerInShaft) =
            isItTheLastMinerInTheChain(minerInShaft) || areThereOtherMinersAt(minerInShaft.at)

    private fun isItTheLastMinerInTheChain(miner: MinerInShaft) =
            miner.at.next() !in occupation

    private fun areThereOtherMinersAt(position: MineShaftPosition) =
            occupation.count { it == position } > 1

    private fun isThereAMinerAt(position: MineShaftPosition) =
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

data class MinerInShaft(val player: PlayerColor, val at: MineShaftPosition) {
    override fun toString(): String = "$player at $at"

    companion object {
        fun asMinerInShaft(event: MagnumSalEvent): MinerInShaft? = when (event) {
            is MagnumSalEvent.MinerRemoved -> MinerInShaft(event.player, event.mineShaftPosition)
            is MagnumSalEvent.MinerPlaced -> MinerInShaft(event.player, event.mineShaftPosition)
            else -> null
        }
    }
}
