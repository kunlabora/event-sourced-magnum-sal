package be.kunlabora.magnumsal

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(JsonSubTypes.Type(MagnumSalEvent::class))
interface Event

data class EventStream(private val _events: MutableList<Event> = emptyList<Event>().toMutableList())
    : List<Event> by _events {

    fun push(event: Event) {
        _events.add(event)
    }
}

inline fun <reified T> EventStream.lastEventOrNull() = lastOrNull { it is T } as T?
inline fun <reified T> EventStream.filterEvents() = filterIsInstance<T>()
