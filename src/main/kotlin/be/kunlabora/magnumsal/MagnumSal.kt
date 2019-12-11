package be.kunlabora.magnumsal

import be.kunlabora.magnumsal.Event.*
import be.kunlabora.magnumsal.exception.transitionRequires

class MagnumSal(private val eventStream: EventStream) {
    fun addPlayer(name: String, color: String) {
        eventStream.push(PlayerJoined(name, color))
    }

    fun startGame() {
        transitionRequires("At least 2 players needed") {
            eventStream.filterIsInstance<PlayerJoined>().count() >= 2
        }
        eventStream.push(GameStarted)
    }

}

