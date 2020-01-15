# Magnum Sal: Event Sourced

[Magnum Sal](https://boardgamegeek.com/boardgame/73316/magnum-sal) is a fun board game about mining salt out of the famous Polish salt mine [Wieliczka](https://www.wieliczka-saltmine.com/)

## The goal of this repo
An exercise in migrating events from an EventStream.

## Event migration kata
### Some functional knowledge required
First, for the exercise to make sense you'll need to know some basic rules of the board game Magnum Sal that are relevant to the kata at hand.

Magnum Sal is a board game where you try to please the King by fulfilling specific orders of salt. As a player, you can mine salt out of the salt mine in different qualities and deliver the salt to the palace.

When you've put some of your miners in a MineChamber, and decide to mine salt, the amount of salt you can bring out of the mine depends on the _strength_ you have in that MineChamber.

_Strength_ is calculated as follows: Amount of your Miners - Amount of water cubes still present in the MineChamber (+ strength some tools can give you, like the pick-axe, but these extra's aren't considered in this implementation of the game, so I don't even know why I'm mentioning them here).

When you've performed a mining action, your miners will get tired for the work they've done. If there were water cubes still present, some of your miners got tired because they needed to hold back water while the rest of the miners were actually mining salt.

Here's an example:

    Given a MineChamber containing 4 cubes of Green Salt, and 1 cube of water  
    and you have 3 Miners present  
    When you mine 2 cubes of Green Salt  
    Then 3 Miners should be tired: 2 from mining 2 cubes of Green Salt, and 1 from holding back the 1 cube of water.

### Some technical knowledge required
The current implementation of the game almost relies completely on computed state from the EventStream by using computed properties.

Other objects are `PositionInMine`, `Salts` and `MineChamberTile` which are _Value Objects_ of which the API is hopefully self-explanatory.

If they're not, take a look at their corresponding unit tests to figure out how they work. 

Getting into an illegal game state is guarded by `Rule` classes that also internally use the EventStream and the _Value Objects_. For example the `TurnOrderRule` makes sure a player is unable to perform actions outside of their turn.

### The actual kata exercise
We've already set you up with an externally persisted EventStream of some game that was running and a Unit Test that you're supposed to write most of your code in: [`MigrationTest`](src/test/kotlin/be/kunlabora/magnumsal/migration/MigrationTest.kt).

Currently, checking which miners are tired is pretty convoluted:
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
We want to replace that with a simpler check that uses `MinersGotTiredEvent`s, but that MagnumSalEvent type doesn't exist yet.

1) First implement the migrate function that adds these events in the proper scenario's (as if the actual code in MagnumSal was already adding these events, like in the last step of this kata).
1) (Optional) Then update `MagnumSal.tiredWorkersAt` to use these new events. Verify by writing tests.
1) (Optional) Finally implement adding these events in `MagnumSal` when a Miner gets tired.

GL HF!


## Magnum Sal rules
It does!

But all shits 'n giggles aside, here are [the condensed rules](./condensed-rules.md), if anything's unclear here is [the actual rulebook](./rulebook.pdf).
