package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.PlayerColor.*
import be.kunlabora.magnumsal.PositionInMine.Companion.at
import be.kunlabora.magnumsal.gamepieces.AllMineChamberTiles
import be.kunlabora.magnumsal.gamepieces.MineChamberTile

data class Player(val name: PlayerName, val color: PlayerColor)

infix fun PlayerName.using(color: PlayerColor): Player = Player(this, color)
typealias PlayerName = String

fun MagnumSal.withPlayers(player1: Player,
                          player2: Player,
                          player3: Player? = null,
                          player4: Player? = null): MagnumSal {
    listOfNotNull(player1, player2, player3, player4).forEach {
        this.addPlayer(it.name, it.color)
    }
    return this
}

fun MagnumSal.withPlayerOrder(player1: PlayerColor,
                              player2: PlayerColor,
                              player3: PlayerColor?,
                              player4: PlayerColor?): MagnumSal {
    this.determinePlayOrder(player1, player2, player3, player4)
    return this
}

fun MagnumSal.withPlayersInOrder(player1: Player,
                                 player2: Player,
                                 player3: Player? = null,
                                 player4: Player? = null): MagnumSal {
    return withPlayers(player1, player2, player3, player4)
            .withPlayerOrder(player1.color, player2.color, player3?.color, player4?.color)
}

fun MagnumSal.distributeWorkersInTheMineShaft(amountOfWorkersToUse: Int, playerOrder: List<PlayerColor>): MagnumSal {
    for (pos in 1..amountOfWorkersToUse) {
        for (player in playerOrder) {
            this.placeWorkerInMine(player, at(pos, 0))
        }
    }
    return this
}

fun MagnumSal.withFourWhiteMinersAtFirstRightMineChamber(): MagnumSal {
    this.placeWorkerInMine(White, at(1, 0))
    this.placeWorkerInMine(Black, at(1, 0))
    this.placeWorkerInMine(White, at(2, 0))
    this.placeWorkerInMine(Black, at(2, 0))
    this.removeWorkerFromMine(White, at(1, 0))
    this.placeWorkerInMine(Black, at(2, 0))
    this.removeWorkerFromMine(White, at(2, 0))
    this.placeWorkerInMine(Black, at(2, 0))
    this.placeWorkerInMine(White, at(2, 1))
    this.placeWorkerInMine(Black, at(2, 0))
    this.placeWorkerInMine(White, at(2, 1))
    this.removeWorkerFromMine(Black, at(2, 0))
    this.placeWorkerInMine(White, at(2, 1))
    this.removeWorkerFromMine(Black, at(2, 0))
    this.placeWorkerInMine(White, at(2, 1))
    this.removeWorkerFromMine(Black, at(2, 0))

    return this
}

fun MagnumSal.revealAllLevelIMineChambers(): MagnumSal {
    this.withPlayersInOrder("Bruno" using White, "Tim" using Black)
    this.placeWorkerInMine(White, at(1, 0))
    this.placeWorkerInMine(Black, at(2, 0))
    this.placeWorkerInMine(White, at(2, 1))
    this.placeWorkerInMine(Black, at(2, 2))
    this.placeWorkerInMine(White, at(2, 3))
    this.placeWorkerInMine(Black, at(2, 4))
    this.placeWorkerInMine(White, at(2, 1))
    this.removeWorkerFromMine(Black, at(2, 4))
    this.removeWorkerFromMine(White, at(2, 3))
    this.removeWorkerFromMine(Black, at(2, 2))
    this.removeWorkerFromMine(White, at(2, 1))
    this.placeWorkerInMine(Black, at(2, -1))
    this.placeWorkerInMine(White, at(2, -2))
    this.placeWorkerInMine(Black, at(2, -3))
    this.placeWorkerInMine(White, at(2, -4))
    return this
}

fun MagnumSal.revealAllLevelIIMineChambers(): MagnumSal {
    this.withPlayersInOrder("Bruno" using White, "Tim" using Black, "Snarf" using Orange)
    this.placeWorkerInMine(White, at(1, 0))
    this.placeWorkerInMine(Black, at(2, 0))
    this.placeWorkerInMine(Orange, at(3, 0))
    this.placeWorkerInMine(White, at(4, 0))
    this.placeWorkerInMine(Black, at(4, 1))
    this.placeWorkerInMine(Orange, at(4, 2))
    this.placeWorkerInMine(White, at(4, 3))
    this.placeWorkerInMine(Black, at(4, -1))
    this.placeWorkerInMine(Orange, at(4, -2))
    this.placeWorkerInMine(White, at(4, -3))
    return this
}

fun MagnumSal.revealAllLevelIIIMineChambers(): MagnumSal {
    this.withPlayersInOrder("Bruno" using White, "Tim" using Black, "Snarf" using Orange)
    this.placeWorkerInMine(White, at(1, 0))
    this.placeWorkerInMine(Black, at(2, 0))
    this.placeWorkerInMine(Orange, at(3, 0))
    this.placeWorkerInMine(White, at(4, 0))
    this.placeWorkerInMine(Black, at(5, 0))
    this.placeWorkerInMine(Orange, at(6, 0))
    this.placeWorkerInMine(White, at(6, 1))
    this.placeWorkerInMine(Black, at(6, 2))
    this.placeWorkerInMine(Orange, at(6, -1))
    this.placeWorkerInMine(White, at(6, -2))
    return this
}

// Util
fun visualize(miners: Miners) {
    println("#".repeat(10) + " MineShaft Top " + "#".repeat(10))
    miners.groupBy { it.at }
            .forEach { (at, miners) ->
                val amountOfMinersPerPlayer = miners.groupBy(Miner::player) { miner -> miners.count { it == miner } }
                println("$at: $amountOfMinersPerPlayer")
            }
    println("#".repeat(10) + " MineShaft End " + "#".repeat(10))
}

class TestMagnumSal(val eventStream: EventStream)

fun TestMagnumSal.withOnlyMineChamberTilesOf(tile: MineChamberTile): MagnumSal {
    val tiles = AllMineChamberTiles
            .filter { it.level == tile.level }
            .map { it.copy(salt = tile.salt, waterCubes = tile.waterCubes) }
    return MagnumSal(this.eventStream, tiles)
}
