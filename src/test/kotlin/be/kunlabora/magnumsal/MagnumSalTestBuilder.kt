package be.kunlabora.magnumsal

typealias Player = Pair<String, PlayerColor>

fun MagnumSal.withPlayers(player1: Player,
                          player2: Player,
                          player3: Player? = null,
                          player4: Player? = null): MagnumSal {
    this.addPlayer(player1.first, player1.second)
    this.addPlayer(player2.first, player2.second)
    player3?.let { this.addPlayer(it.first, it.second) }
    player4?.let { this.addPlayer(it.first, it.second) }
    return this
}

fun MagnumSal.withPlayerOrder(player1: PlayerColor,
                              player2: PlayerColor,
                              player3: PlayerColor?,
                              player4: PlayerColor?): MagnumSal {
    this.determinePlayOrder(player1, player2)
    return this
}

fun MagnumSal.withPlayersInOrder(player1: Player,
                                 player2: Player,
                                 player3: Player? = null,
                                 player4: Player? = null): MagnumSal {
    return withPlayers(player1, player2, player3, player4)
            .withPlayerOrder(player1.second, player2.second, player3?.second, player4?.second)
}
