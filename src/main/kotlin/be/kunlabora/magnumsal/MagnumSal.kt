package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.MagnumSalEvent.*
import be.kunlabora.magnumsal.MinerMovement.PlaceMiner
import be.kunlabora.magnumsal.MinerMovement.RemoveMiner
import be.kunlabora.magnumsal.exception.transitionRequires
import be.kunlabora.magnumsal.gamepieces.AllMineChamberTiles
import be.kunlabora.magnumsal.gamepieces.Level
import be.kunlabora.magnumsal.gamepieces.MineChamberTile
import be.kunlabora.magnumsal.gamepieces.Salts

sealed class MagnumSalEvent : Event {
    data class PlayerOrderDetermined(val player1: PlayerColor, val player2: PlayerColor, val player3: PlayerColor? = null, val player4: PlayerColor? = null) : MagnumSalEvent()
    data class PlayerJoined(val name: String, val color: PlayerColor) : MagnumSalEvent()
    data class MinerPlaced(val player: PlayerColor, val at: PositionInMine) : MagnumSalEvent()
    data class MinerRemoved(val player: PlayerColor, val at: PositionInMine) : MagnumSalEvent()
    data class MineChamberRevealed(val at: PositionInMine, val tile: MineChamberTile) : MagnumSalEvent()
    data class SaltMined(val player: PlayerColor, val from: PositionInMine, val saltMined: Salts) : MagnumSalEvent()
}

class MagnumSal(private val eventStream: EventStream, private val allMineChamberTiles: List<MineChamberTile> = AllMineChamberTiles) {

    private val turnOrderRule = TurnOrderRule(eventStream)
    private val chainRule = ChainRule(eventStream)
    private val workerLimitRule = WorkerLimitRule(eventStream)

    private val players
        get() = eventStream.filterEvents<PlayerJoined>()

    private val amountOfPlayers
        get() = players.count()

    private val miners: Miners
        get() = Miners.from(eventStream)

    private val revealedMineChambers
        get() = eventStream.filterEvents<MineChamberRevealed>()

    fun addPlayer(name: String, color: PlayerColor) {
        transitionRequires("the same color not to have been picked already") {
            color !in players.map { it.color }
        }
        eventStream.push(PlayerJoined(name, color))
    }

    fun determinePlayOrder(player1: PlayerColor,
                           player2: PlayerColor,
                           player3: PlayerColor? = null,
                           player4: PlayerColor? = null) {
        val colors = setOf(player1, player2, player3, player4).filterNotNull()
        transitionRequires("player colors to be unique") {
            colors.size == listOfNotNull(player1, player2, player3, player4).size
        }
        transitionRequires("at least 2 players") {
            amountOfPlayers >= 2
        }
        transitionRequires("player colors to have been picked") {
            val players = players.map { it.color }.toSet()
            colors.intersect(players).size == players.size
        }
        eventStream.push(PlayerOrderDetermined(player1, player2, player3, player4))
    }

    fun placeWorkerInMine(player: PlayerColor, at: PositionInMine) = onlyInPlayersTurn(player) {
        withoutBreakingTheChain(PlaceMiner(player, at)) {
            requirePlayerToHaveEnoughWorkers(player)
            eventStream.push(MinerPlaced(player, at))
            revealMineChamberIfPossible(at)
        }
    }

    fun removeWorkerFromMine(player: PlayerColor, at: PositionInMine) = onlyInPlayersTurn(player) {
        withoutBreakingTheChain(RemoveMiner(player, at)) {
            transitionRequires("you to have a miner at $at") {
                hasWorkerAt(player, at)
            }
            eventStream.push(MinerRemoved(player, at))
        }
    }

    fun mine(player: PlayerColor, at: PositionInMine, saltToMine: Salts) = onlyInPlayersTurn(player) {
        transitionRequires("you to have a miner at $at") {
            hasWorkerAt(player, at)
        }
        transitionRequires("you to mine from a MineChamber") {
            at.isInACorridor()
        }
        transitionRequires("you to have enough miners at $at") {
            strengthAt(player, at) >= saltToMine.size
        }
        transitionRequires("there to be $saltToMine in $at") {
            saltIsAvailableAt(saltToMine, at)
        }
        eventStream.push(SaltMined(player, at, saltToMine))
    }

    private fun saltIsAvailableAt(saltToMine: Salts, at: PositionInMine): Boolean {
        val saltInMineChamber = saltLeftInMineChamber(at)
        return saltToMine.canBeMinedFrom(saltInMineChamber)
    }

    private fun saltLeftInMineChamber(at: PositionInMine): Salts {
        val saltsOnTile = Salts(revealedMineChambers.single { it.at == at }.tile.salt)
        val saltsAlreadyMined = eventStream.filterEvents<SaltMined>().singleOrNull { it.from == at }?.saltMined
        return saltsAlreadyMined?.let { saltsOnTile - it } ?: saltsOnTile
    }

    private fun strengthAt(player: PlayerColor, at: PositionInMine): Int {
        val waterInChamber = revealedMineChambers.single { it.at == at }.tile.waterCubes
        val playerMiners = miners.count { it == Miner(player, at) }
        return playerMiners - waterInChamber
    }

    private fun revealMineChamberIfPossible(at: PositionInMine) {
        if (at.isInACorridor() && isNotRevealed(at)) {
            revealNewMineChamber(at)
        }
    }

    private fun isNotRevealed(at: PositionInMine) = at !in revealedMineChambers.map { it.at }

    private fun revealNewMineChamber(at: PositionInMine) {
        val level = Level.from(at)
        val revealedMineChamberTiles = revealedMineChambers.map { it.tile }
        val unrevealedMineChamberTiles = allMineChamberTiles.shuffled() - revealedMineChamberTiles
        val tile = unrevealedMineChamberTiles.filter { it.level == level }[0]
        eventStream.push(MineChamberRevealed(at, tile))
    }

    private fun hasWorkerAt(player: PlayerColor, at: PositionInMine): Boolean = miners.any { it == Miner(player, at) }

    private fun requirePlayerToHaveEnoughWorkers(player: PlayerColor) = workerLimitRule.requirePlayerToHaveEnoughWorkers(player)
    private fun onlyInPlayersTurn(player: PlayerColor, block: () -> Unit): Any = turnOrderRule.onlyInPlayersTurn(player, block)
    private fun withoutBreakingTheChain(minerMovement: MinerMovement, block: () -> Unit): Any = chainRule.withoutBreakingTheChain(minerMovement, block)

    companion object
}

sealed class PlayerColor {
    object Black : PlayerColor()
    object White : PlayerColor()
    object Orange : PlayerColor()
    object Purple : PlayerColor()

    override fun toString(): String = this.javaClass.simpleName
}

