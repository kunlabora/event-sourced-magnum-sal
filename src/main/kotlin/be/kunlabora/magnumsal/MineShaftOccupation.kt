package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.exception.transitionRequires

class MineShaftOccupation private constructor(private val _miners: List<MinerInShaft>) : List<MinerInShaft> by _miners {

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
            minerInShaft.at.isTheTop() || isThereAnotherMinerAt(minerInShaft.at.previous())

    private fun removingWouldNotBreakTheChain(minerInShaft: MinerInShaft): Boolean {
        val mineShaftAfterRemoval = MineShaftOccupation(_miners - minerInShaft)
        return mineShaftAfterRemoval.isThereAnotherMinerAt(minerInShaft.at)
                || mineShaftAfterRemoval.wasItTheLastMinerInTheChain(minerInShaft)
    }

    private fun wasItTheLastMinerInTheChain(miner: MinerInShaft) =
            miner.at.next() !in _miners.map { it.at }

    private fun isThereAnotherMinerAt(position: MineShaftPosition) =
            position in _miners.map { it.at }

    companion object {
        fun from(eventStream: EventStream): MineShaftOccupation {
            val miners : List<MinerInShaft> = eventStream.filterEvents<MagnumSalEvent>().fold(emptyList()) { acc, event ->
                when (event) {
                    is MagnumSalEvent.MinerRemoved -> MinerInShaft.asMinerInShaft(event)?.let { acc - it } ?: acc
                    is MagnumSalEvent.MinerPlaced -> MinerInShaft.asMinerInShaft(event)?.let { acc + it } ?: acc
                    else -> acc
                }
            }
            return MineShaftOccupation(miners)
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
