package be.kunlabora.magnumsal

fun MagnumSal.withPlayerOrder(player1: PlayerColor, player2: PlayerColor): MagnumSal {
    this.determinePlayOrder(player1, player2)
    return this
}
