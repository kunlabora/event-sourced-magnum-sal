package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MinerMovement.PlaceMiner
import be.kunlabora.magnumsal.MinerMovement.RemoveMiner
import be.kunlabora.magnumsal.exception.transitionRequires

class ChainRule(private val _eventStream: EventStream) {

    private val mineOccupation
        get() = Miners.from(_eventStream).map { it.at }

    fun withoutBreakingTheChain(minerMovement: MinerMovement, block: () -> Unit): Any {
        transitionRequires("not to break the chain") {
            when (minerMovement) {
                is PlaceMiner -> placingAMinerDoesNotBreakTheChain(Miner(minerMovement.player, minerMovement.at))
                is RemoveMiner -> removingAMinerDoesNotBreakTheChain(Miner(minerMovement.player, minerMovement.at))
            }
        }
        return block()
    }

    private fun placingAMinerDoesNotBreakTheChain(miner: Miner): Boolean {
        return miner.at.isTheTop() || isThereAMinerAt(miner.at.higher())
    }

    private fun isThereAMinerAt(position: PositionInMine) =
            position in mineOccupation

    private fun removingAMinerDoesNotBreakTheChain(miner: Miner): Boolean {
        return isItTheLastMinerInTheChain(miner) || areThereOtherMinersAt(miner.at)
    }

    private fun isItTheLastMinerInTheChain(miner: Miner) =
            miner.at.deeper() !in mineOccupation

    private fun areThereOtherMinersAt(position: PositionInMine) =
            mineOccupation.count { it == position } > 1
}
