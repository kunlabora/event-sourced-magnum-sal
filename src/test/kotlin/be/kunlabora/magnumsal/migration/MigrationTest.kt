package be.kunlabora.magnumsal.migration

import be.kunlabora.magnumsal.EventStream
import be.kunlabora.magnumsal.migration.read.readEventLog
import org.junit.jupiter.api.Test

class MigrationTest {
    @Test
    fun `migrate | returns a new EventStream with MinersGotTired events`() {
        val migratedEventStream = migrate(readEventLog())
//        assertThat migratedEventStream contains the correct MinersGotTired events
    }
}

fun migrate(eventStream: EventStream): EventStream {
    TODO("""
Current checking of tired miners is pretty convoluted:
```kotlin
    private fun tiredWorkersAt(player: PlayerColor, at: PositionInMine): Int {
        val playersSaltMiningActions = eventStream.filterEvents<SaltMined>()
                .filter { it.from == at && it.player == player }
        val minersTiredFromMining = playersSaltMiningActions
                .fold(0) { acc, it -> acc + it.saltMined.size }
        val minersTiredFromHoldingBackWater = playersSaltMiningActions
                .count() * waterRemainingInChamber(at)
        return minersTiredFromMining + minersTiredFromHoldingBackWater
    }
```
We want to replace that with a simpler check for `MinersGotTiredEvent`s, but those don't exist yet.

1) First implement the migrate function that adds these events in the proper scenario's (as if the actual code in MagnumSal was already adding these events, like in the last step of this kata).
1) (Optional) Then update `MagnumSal.tiredWorkersAt` to use these new events. Verify by writing tests.
1) (Optional) Finally implement adding these events in `MagnumSal` when a Miner gets tired.

GL HF!
        """)
}
