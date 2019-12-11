package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.Event.*
import be.kunlabora.magnumsal.exception.transitionRequires

class MagnumSal(private val eventStream: EventStream) {
    fun addPlayer(name: String, color: PlayerColor) {
        eventStream.push(PlayerJoined(name, color))
    }

    //TODO later: add player3: PlayerColor? = null, player4 : PlayerColor? = null
    fun determinePlayOrder(player1: PlayerColor, player2: PlayerColor) {
        require(player1 != player2) { "Play order can only be determined with different PlayerColors" }
        transitionRequires("At least 2 players needed") {
            eventStream.filterIsInstance<PlayerJoined>().count() >= 2
        }
        transitionRequires("Players colors should have been picked for player order") {
            player1 in eventStream.filterIsInstance<PlayerJoined>().map { it.color }
                    && player2 in eventStream.filterIsInstance<PlayerJoined>().map { it.color }
        }
        eventStream.push(DeterminedPlayerOrder(player1, player2))
    }
}

sealed class PlayerColor {
    object Black: PlayerColor()
    object White: PlayerColor()
    object Orange: PlayerColor()
    object Purple: PlayerColor()
}

