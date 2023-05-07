package me.victoralan.software.wallet

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.node.ObjectNode
import me.victoralan.crypto.encoder.Base58
import java.io.Serializable
import java.lang.RuntimeException
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec

@JsonSerialize(using = AddressSerializer::class)
@JsonDeserialize(using = AddressDeserializer::class)
class Address(val publicKey: PublicKey,
              var address: String) : Serializable{

    constructor(publicKey: PublicKey, version: Int) : this(publicKey, "") {
        address = generateAddress(version.toShort(), publicKey)
        println(address)
        checkCoherence()
    }

    init {
        if (address != ""){
            checkCoherence()
        }
    }

    fun checkCoherence(){
        for (i in -128..128){
            println("$i: ${generateAddress(i.toShort(), publicKey)}")
            if (generateAddress(i.toShort(), publicKey) == address){
                return
            }
        }
        throw RuntimeException("Address not coherent")
    }
    private fun generateAddress(version: Short, publicKey: PublicKey): String {
        // Step 1: Perform SHA-256 hash on the public key
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hash1 = sha256.digest(publicKey.encoded)

        // Step 2: Perform SHA3-256 hash on the result of Step 1
        val sha3256 = MessageDigest.getInstance("SHA3-256")
        val hash2 = sha3256.digest(hash1)

        // Step 3: Add version byte to the front of the result of Step 2
        val versionByte = byteArrayOf((version-128).toByte(), version.toByte())
        val hash3 = ByteArray(versionByte.size + hash2.size)
        System.arraycopy(versionByte, 0, hash3, 0, versionByte.size)
        System.arraycopy(hash2, 0, hash3, versionByte.size, hash2.size)

        // Step 4: Perform SHA-256 hash on the result of Step 3
        val hash4 = sha256.digest(hash3)

        // Step 5: Perform SHA-256 hash on the result of Step 4
        val hash5 = sha256.digest(hash4)

        // Step 6: Take the first 4 bytes of the result of Step 5, and add them to the end of the result of Step 3
        val checksum = ByteArray(4)
        System.arraycopy(hash5, 0, checksum, 0, 4)
        val hash6 = ByteArray(hash3.size + checksum.size)
        System.arraycopy(hash3, 0, hash6, 0, hash3.size)
        System.arraycopy(checksum, 0, hash6, hash3.size, checksum.size)

        // Step 7: Encode the result of Step 6 using Base58Check encoding
        return Base58.encode(hash6)
    }
    override fun toString(): String {
        return "Address(publicKey=${Base58.encode(publicKey.encoded)}, address=$address)"
    }
}
class AddressSerializer : JsonSerializer<Address>(){
    override fun serialize(value: Address, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("address", value.address)
        gen.writeBinaryField("publicKey", value.publicKey.encoded)
        gen.writeEndObject()
    }
}

class AddressDeserializer : JsonDeserializer<Address>(){
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Address {
        val codec: ObjectCodec = p.codec
        val node: ObjectNode = codec.readTree(p)
        val address = node.get("address").asText()
        val publicKey = node.get("publicKey").binaryValue()
        val keyFactory: KeyFactory = KeyFactory.getInstance("EC")
        val publicKeySpec: X509EncodedKeySpec = X509EncodedKeySpec(publicKey)
        val realPublicKey: ECPublicKey = keyFactory.generatePublic(publicKeySpec) as ECPublicKey

        return Address(realPublicKey, address)
    }
}