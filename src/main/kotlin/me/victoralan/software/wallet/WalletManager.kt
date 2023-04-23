package me.victoralan.software.wallet

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.File
import java.io.ObjectInputStream


class WalletManager {
    fun loadWallet(file: File, passphrase: String) : Wallet{
        val mapper = ObjectMapper()
//        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
//        mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA)
//        mapper.enable(JsonParser.Feature.IGNORE_UNDEFINED)
//        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//        mapper.disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
//        mapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
//        mapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
//        mapper.disable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
//        mapper.disable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
        return mapper.readValue(file, Wallet::class.java)
    }

}