package me.victoralan.blockchain.blockitems

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import me.victoralan.Hash
import java.io.Serializable
import java.lang.RuntimeException

abstract class BlockItem(open val time: Long) : Serializable {
    var hash: Hash = Hash("")
    abstract fun calculateHash(): Hash

}
