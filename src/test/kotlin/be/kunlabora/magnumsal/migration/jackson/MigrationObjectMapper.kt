package be.kunlabora.magnumsal.migration.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class MigrationObjectMapper(val objectMapper: ObjectMapper = ObjectMapper()) {
    init {
        objectMapper
                .registerKotlinModule()
                .registerModule(Jdk8Module())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.INDENT_OUTPUT)
    }
}
