package be.kunlabora.magnumsal.migration.read

import be.kunlabora.magnumsal.EventStream
import be.kunlabora.magnumsal.MagnumSalEvent
import be.kunlabora.magnumsal.filterEvents
import be.kunlabora.magnumsal.migration.MigrationObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

const val eventLogFile = "src/test/resources/eventLog.json"
fun readEventLog() = readStream(File(eventLogFile))

fun main() {
    val deserializedEventStream = readEventLog()
    println(deserializedEventStream.filterEvents<MagnumSalEvent.PlayerOrderDetermined>())
}


fun readStream(eventLog: File): EventStream {
    val objectMapper = MigrationObjectMapper().objectMapper
    return objectMapper.readValue(eventLog)
}
