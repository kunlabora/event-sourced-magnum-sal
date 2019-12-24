package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.PositionInMine.Companion.at

data class Player(val name: PlayerName, val color: PlayerColor)

infix fun PlayerName.using(color: PlayerColor) : Player = Player(this, color)
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
