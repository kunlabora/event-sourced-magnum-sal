package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.*
import be.kunlabora.magnumsal.MinerInShaft.Companion.asMinerInShaft
import be.kunlabora.magnumsal.exception.transitionRequires

sealed class MagnumSalEvent : Event {
    data class PlayerOrderDetermined(val player1: PlayerColor, val player2: PlayerColor) : MagnumSalEvent()
    data class PlayerJoined(val name: String, val color: PlayerColor) : MagnumSalEvent()
    data class MinerPlaced(val player: PlayerColor, val mineShaftPosition: MineShaftPosition) : MagnumSalEvent()
    data class MinerRemoved(val player: PlayerColor, val mineShaftPosition: MineShaftPosition) : MagnumSalEvent()
}

class MagnumSal(private val eventStream: EventStream) {

    private val players
        get() = eventStream.filterEvents<PlayerJoined>()

    private val amountOfPlayers
        get() = players.count()

    private val firstPlayer
        get() = eventStream.filterEvents<PlayerOrderDetermined>().single().player1

    private val minersPlaced
        get() = eventStream.filterEvents<MinerPlaced>()
    private val minersRemoved
        get() = eventStream.filterEvents<MinerRemoved>()
    private val playerActions
        get() = minersPlaced.map { it.player } + minersRemoved.map { it.player }

    private val mineShaftOccupation: List<MinerInShaft>
        get() = eventStream.filterEvents<MagnumSalEvent>().fold(emptyList()) { acc, event ->
            when (event) {
                is MinerRemoved -> asMinerInShaft(event)?.let { acc - it } ?: acc
                is MinerPlaced -> asMinerInShaft(event)?.let { acc + it } ?: acc
                else -> acc
            }
        }

    fun addPlayer(name: String, color: PlayerColor) {
        transitionRequires("the same color not to have been picked already") {
            color !in players.map { it.color }
        }
        transitionRequires("a maximum of one player to have joined") {
            amountOfPlayers <= 1
        }
        eventStream.push(PlayerJoined(name, color))
    }

    //TODO later: add player3: PlayerColor? = null, player4 : PlayerColor? = null
    fun determinePlayOrder(player1: PlayerColor, player2: PlayerColor) {
        require(player1 != player2) { "Play order can only be determined with different PlayerColors" }
        transitionRequires("at least 2 players") {
            amountOfPlayers >= 2
        }
        transitionRequires("player colors to have been picked") {
            player1 in players.map { it.color }
                    && player2 in players.map { it.color }
        }
        eventStream.push(PlayerOrderDetermined(player1, player2))
    }

    fun placeWorkerInMine(player: PlayerColor, mineShaftPosition: MineShaftPosition) {
        transitionRequires("it to be your turn") {
            itIsTheTurnOfPlayer(player)
        }
        transitionRequires("the previous MineShaft Position to be occupied") {
            aMinerIsGoingToBePlacedAtTheTop(mineShaftPosition) || aMinerWasPlacedAt(mineShaftPosition.previous())
        }
        eventStream.push(MinerPlaced(player, mineShaftPosition))
    }

    private fun itIsTheTurnOfPlayer(player: PlayerColor): Boolean {
        val isFirstPlayer = firstPlayer == player
        val playerActionsCount = playerActions.filter { it == player }.count()
        val opponentActionsCount = playerActions.filter { it != player }.count()

        return (isFirstPlayer && opponentActionsCount == playerActionsCount)
                || (!isFirstPlayer && opponentActionsCount > playerActionsCount)
    }

    private fun aMinerIsGoingToBePlacedAtTheTop(currentMineShaftPosition: MineShaftPosition) =
            (currentMineShaftPosition.index == 1)

    private fun aMinerWasPlacedAt(position: MineShaftPosition) =
            (position in minersPlaced.map { it.mineShaftPosition })

    fun removeWorkerFromMine(player: PlayerColor, mineShaftPosition: MineShaftPosition) {
        transitionRequires("it to be your turn") {
            itIsTheTurnOfPlayer(player)
        }
        transitionRequires("the chain not to be broken") {
            removingWouldNotBreakTheChain(MinerInShaft(player, mineShaftPosition))
        }
        eventStream.push(MinerRemoved(player, mineShaftPosition))
    }

    private fun removingWouldNotBreakTheChain(minerInShaft: MinerInShaft): Boolean {
        val mineShaftAfterRemoval = (mineShaftOccupation - minerInShaft).map { it.at }
        return isThereAnotherMinerAt(minerInShaft.at, mineShaftAfterRemoval)
                || wasItTheLastMinerAt(minerInShaft.at, mineShaftAfterRemoval)
    }

    private fun wasItTheLastMinerAt(mineShaftPosition: MineShaftPosition, mineShaftAfterRemoval: List<MineShaftPosition>) =
            mineShaftPosition.next() !in mineShaftAfterRemoval

    private fun isThereAnotherMinerAt(mineShaftPosition: MineShaftPosition, mineShaftAfterRemoval: List<MineShaftPosition>) =
            mineShaftPosition in mineShaftAfterRemoval
}

data class MineShaftPosition(val index: Int) {
    init {
        require(index in 1..6) { "Mine shaft is only 6 deep" }
    }
    fun previous(): MineShaftPosition = MineShaftPosition(index - 1)
    fun next(): MineShaftPosition = MineShaftPosition(index + 1)
    override fun toString(): String = "mine[$index]"
}

data class MinerInShaft(val player: PlayerColor, val at: MineShaftPosition) {
    override fun toString(): String = "$player at $at"

    companion object {
        fun asMinerInShaft(event: MagnumSalEvent): MinerInShaft? = when (event) {
            is MinerRemoved -> MinerInShaft(event.player, event.mineShaftPosition)
            is MinerPlaced -> MinerInShaft(event.player, event.mineShaftPosition)
            else -> null
        }
    }
}

sealed class PlayerColor {
    object Black : PlayerColor()
    object White : PlayerColor()
    object Orange : PlayerColor()
    object Purple : PlayerColor()
    override fun toString(): String = this.javaClass.simpleName
}

