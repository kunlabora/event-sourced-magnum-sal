package be.kunlabora.magnumsal.migration.generate

import be.kunlabora.magnumsal.*
import be.kunlabora.magnumsal.MagnumSalEvent.MineChamberRevealed
import be.kunlabora.magnumsal.PlayerColor.*
import be.kunlabora.magnumsal.PositionInMine.Companion.at
import be.kunlabora.magnumsal.gamepieces.Salts
import be.kunlabora.magnumsal.migration.jackson.MigrationObjectMapper
import be.kunlabora.magnumsal.migration.read.eventLogFile
import java.io.File


fun main() {
    writeEventLog(File(eventLogFile), playTheGame())
}

private fun playTheGame(): EventStream {
    val eventStream = EventStream()

    val magnumSal = TestMagnumSal(eventStream)
            .withDebugger()
            .withPlayersInOrder(
                    "Day[9]" using White,
                    "Tasteless" using Black,
                    "iNcontroL" using Orange,
                    "TotalBiscuit" using Purple)
            .build()

    fun mineAllTheSalt(player: PlayerColor, at: PositionInMine) {
        val (_, salt, water) = eventStream.filterEvents<MineChamberRevealed>().single { it.at == at }.tile
        val playerMinersAt = Miners.from(eventStream).count { it.player == player && it.at == at }
        val strength = playerMinersAt - water
        val saltToMine = Salts(salt.take(strength)).also { println("$player tries to mine $it at $at with $strength miners") }
        if (saltToMine.isNotEmpty()) magnumSal.mine(player, at, saltToMine)
    }

    magnumSal.placeWorkerInMine(White, at(1, 0))
    magnumSal.placeWorkerInMine(Black, at(2, 0))
    magnumSal.placeWorkerInMine(Orange, at(2, 1))
    magnumSal.placeWorkerInMine(Purple, at(2, 2))

    magnumSal.placeWorkerInMine(White, at(2, -1))
    magnumSal.placeWorkerInMine(White, at(2, -1))

    magnumSal.placeWorkerInMine(Black, at(2, -2))
    magnumSal.placeWorkerInMine(Black, at(2, -2))

    magnumSal.placeWorkerInMine(Orange, at(2, 1))
    magnumSal.placeWorkerInMine(Orange, at(2, 1))

    magnumSal.placeWorkerInMine(Purple, at(2, 2))
    magnumSal.placeWorkerInMine(Purple, at(2, 2))

    magnumSal.placeWorkerInMine(White, at(2, -1))
    mineAllTheSalt(White, at(2, -1))

    magnumSal.placeWorkerInMine(Black, at(2, -2))
    mineAllTheSalt(Black, at(2, -2))

    mineAllTheSalt(Orange, at(2, 1))
    magnumSal.removeWorkerFromMine(Orange, at(2, 1))

    mineAllTheSalt(Purple, at(2, 2))
    magnumSal.removeWorkerFromMine(Purple, at(2, 2))

    return eventStream
}


fun writeEventLog(eventLog: File, eventStream: EventStream) {
    val objectMapper = MigrationObjectMapper().objectMapper
    eventLog.writeBytes(objectMapper.writeValueAsBytes(eventStream))
}
