package be.kunlabora.magnumsal

interface Event

data class EventStream(private val _events: MutableList<Event> = emptyList<Event>().toMutableList())
    : List<Event> by _events {

    fun push(event: Event) {
        _events.add(event)
    }
}

inline fun <reified T : Event> EventStream.lastEventOrNull() = lastOrNull { it is T } as T?
inline fun <reified T : Event> EventStream.filterEvents() = filterIsInstance<T>() as EventStream
