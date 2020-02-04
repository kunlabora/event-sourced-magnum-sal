package be.kunlabora.magnumsal

sealed class MagnumSalEvent: Event {
    data class PlayerAdded(val name: String) : MagnumSalEvent()
}

class MagnumSal(private val eventStream: EventStream) {
    /**
     * Adds a player with the given name to a game of MagnumSal.
     * This is merely an example that works in tandem with the already existing PlayerAdded event.
     * When you've managed to implement this and get the existing test green,
     * you should feel confident enough to continue on your own.
     * If that's not the case, take a look at the hints in the README.md and/or read the assignment (actual exercise) again.
     */
    fun addPlayer(playerName: String) {
        TODO("add an event to the event stream")
    }

    fun placeWorker() {
        TODO("FIRST create a test, then implement this accordingly")
    }
}
