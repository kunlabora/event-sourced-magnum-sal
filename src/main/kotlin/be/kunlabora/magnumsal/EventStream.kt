package be.kunlabora.magnumsal


data class EventStream(private val _events: MutableList<Event> = emptyList<Event>().toMutableList())
    : List<Event> by _events {

    fun push(event: Event) {
        _events.add(event)
    }
}

sealed class Event {
    data class PlayerOrderDetermined(val player1: PlayerColor, val player2: PlayerColor) : Event()
    data class PlayerJoined(val name: String, val color: PlayerColor) : Event()
    data class MinerPlaced(val player: PlayerColor, val position: Position) : Event()
}
