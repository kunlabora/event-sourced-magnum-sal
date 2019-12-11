package be.kunlabora.magnumsal


data class EventStream(private val _events: MutableList<Event> = emptyList<Event>().toMutableList())
    : List<Event> by _events {

    fun push(event: Event) {
        _events.add(event)
    }
}

sealed class Event {
    object GameStarted : Event()
    data class PlayerJoined(val name: String, val color: String) : Event()
}

interface Payload
