# Magnum Sal: Event Sourced

[Magnum Sal](https://boardgamegeek.com/boardgame/73316/magnum-sal) is a fun board game about mining salt out of the famous Polish salt mine [Wieliczka](https://www.wieliczka-saltmine.com/)

## The goal of this repo
Play around with EventSourcing and try not to make any assumptions on what domain classes you need and only create one when multiple events have hinted at one.

## Magnum Sal rules
It does!

But all shits 'n giggles aside, here are [the condensed rules](./condensed-rules.md), if anything's unclear here is [the actual rulebook](./rulebook.pdf).

## The kata rules
Make sure no impossible states can occur according to the Magnum Sal rules.  
Postpone creating domain classes as long as possible, purely rely on the `EventStream` instead.

## Tips
Keep your events as fine-grained as possible, and **always** in past tense.  
Start with determining the player order of a game with at least two players. Assume that the setup has been completed.  
Don't start with the town actions.  
Try to stall persisting state or creating other domain classes until you've implemented the _chain rule_.

Work towards this scenario:

1) Player 1 places a miner in the mineshaft's first spot.
1) Player 2 also places a miner in the mineshaft's first spot.
1) Player 1 places a miner in the mineshaft's second spot.
1) Player 2 also places a miner in the mineshaft's second spot.
1) Player 1 removes their miner from the first spot.
1) Player 2 also removes their miner from the first spot. <-- this should be an illegal move, because the first spot should be occupied according to the chain rule.

## Quick-start (+- 60' Kata time)
```shell script
git checkout -b quick-start
```

If you have limited time, you can work towards this scenario:

Forget about two players and the town and the corridors. Just focus on the mine shaft and the chain rule 

1) Player 1 places a miner in the mineshaft's first spot.
1) Player 1 removes a miner in the mineshaft's first spot.
1) Player 1 places a miner in the mineshaft's first spot again.
1) Player 1 places a miner in the mineshaft's second spot.
1) Player 1 removes their miner in the first spot. <-- this should be an illegal move, because the first spot should be occupied according to the chain rule.

`MineShaftPosition` already contains some validation and util functions that'll prove useful, check out the tests to see how it works.

## Event migration kata
```shell script
git checkout -b event-migration-kata
```

This is a kata that starts from a somewhat implemented game of MagnumSal, and has you refactor a pretty convoluted method to use an event type that doesn't exist yet.

So your job is to first migrate an existing event stream to throw this new event at the correct order in the event stream, to then use it in your implementation.

Don't worry, the code has been set up so that you can just focus on the migration itself.

See the event-migration-kata branch's readme to read a more exhaustive kata exercise and find all the info you need.
