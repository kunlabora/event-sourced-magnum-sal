package be.kunlabora.magnumsal

sealed class MagnumSalEvent: Event {
    data class PlayerAdded(val name: String) : MagnumSalEvent()
}

class MagnumSal(private val eventStream: EventStream) {
    fun addPlayer(playerName: String) {
        TODO("add an event to the event stream")
    }
}
