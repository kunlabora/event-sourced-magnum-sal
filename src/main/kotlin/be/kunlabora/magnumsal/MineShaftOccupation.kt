package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.exception.transitionRequires

class MineShaftOccupation(private val _miners: List<MinerInShaft>) : List<MinerInShaft> by _miners {

    fun attemptPlacingMiner(at: MineShaftPosition) {
        transitionRequires("the previous position to be occupied") {
            aMinerIsGoingToBePlacedAtTheTop(at) || aMinerWasPlacedAt(at.previous())
        }
    }

    private fun aMinerIsGoingToBePlacedAtTheTop(currentMineShaftPosition: MineShaftPosition) =
            (currentMineShaftPosition.index == 1)

    private fun aMinerWasPlacedAt(position: MineShaftPosition) =
            (position in _miners.map { it.at })


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
