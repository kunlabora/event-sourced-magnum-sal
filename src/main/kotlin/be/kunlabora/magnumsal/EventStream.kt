package be.kunlabora.magnumsal


data class EventStream(private val _events: List<Event>): List<Event> by _events

sealed class Event {
    object GameStarted : Event()
}

interface Payload
