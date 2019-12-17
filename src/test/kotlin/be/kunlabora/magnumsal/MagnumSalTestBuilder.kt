package be.kunlabora.magnumsal

typealias Player = Pair<String, PlayerColor>

fun MagnumSal.withPlayers(player1: Player,
                          player2: Player,
                          player3: Player? = null,
                          player4: Player? = null): MagnumSal {
    listOfNotNull(player1, player2, player3, player4).forEach {
        this.addPlayer(it.first, it.second)
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
            .withPlayerOrder(player1.second, player2.second, player3?.second, player4?.second)
}

fun MagnumSal.distributeWorkersInTheMineShaft(amountOfWorkersToUse: Int, playerOrder: List<PlayerColor>): MagnumSal {
    (1..amountOfWorkersToUse).forEach { pos ->
        playerOrder.forEach { player ->
            this.placeWorkerInMine(player, MineShaftPosition(pos))
        }
    }
    return this
}

// Util
fun MineShaft.visualize() {
    this.groupBy { it.at }
            .forEach { (at, miners) ->
                val amountOfMinersPerPlayer = miners.groupBy(MinerInShaft::player) { miner -> miners.count { it == miner } }
                println("At $at: $amountOfMinersPerPlayer")
            }
}
