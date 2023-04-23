package me.victoralan.software.wallet

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import me.victoralan.blockchain.blockitems.MoneyTransaction
import me.victoralan.blockchain.blockitems.Transaction
import me.victoralan.crypto.ecdsa.ECDSA
import me.victoralan.crypto.encoder.Base58
import me.victoralan.software.Node
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import rsa.keys.RSAPublicKey
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.io.Serializable
import java.security.KeyFactory
import java.security.KeyPair
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

@JsonSerialize(using = WalletSerializer::class)
@JsonDeserialize(using = WalletDeserializer::class)
class Wallet(val keyPair: KeyPair, val rsaEncryptionKey: RSAPublicKey) : Serializable {
    val transactions: ArrayList<MoneyTransaction> = ArrayList()
    var currentValance: Float = 0f
    var userSettings: UserSettings = UserSettings()
    var trustedNodes: ArrayList<Node> = ArrayList()
    var pendingTransaction: ArrayList<Transaction> = ArrayList()
    var qrCodes: ArrayList<QRCode> = ArrayList()
    var addresses: ArrayList<Address> = ArrayList()
    // TODO("ADDRESS BOOK")
    val saveFile: File = File("./wallet.synergyx")
    private fun generateAddress(version: Int): String {
        // Step 1: Perform SHA-256 hash on the public key
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hash1 = sha256.digest(keyPair.public.encoded)

        // Step 2: Perform SHA3-256 hash on the result of Step 1
        val sha3256 = MessageDigest.getInstance("SHA3-256")
        val hash2 = sha3256.digest(hash1)

        // Step 3: Add version byte to the front of the result of Step 2
        val versionByte = byteArrayOf((version-128).toByte(), SecureRandom(version.toBigInteger().toByteArray()).nextInt().toByte())
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
    fun signTransaction(transaction: ByteArray): ByteArray {
        return ECDSA().signMessage(transaction, keyPair.private)

    }


    fun saveWallet() {
        val objectMapper = ObjectMapper()
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)

        val module = SimpleModule()
        module.addSerializer(Wallet::class.java, WalletSerializer())
        module.addDeserializer(Wallet::class.java, WalletDeserializer())
        objectMapper.registerModule(module)

        val data = objectMapper.writeValueAsString(this)
        saveFile.writeText(data)
    }
    fun verifySignature(transaction: ByteArray, signature: ByteArray): Boolean {
        return ECDSA().verifySignature(transaction, signature, keyPair.public)
    }
    fun generateQRCodes(){
        for (address in addresses){
            val addr = address.address
            val qrCode: QRCode = QRCode.from(addr)
            qrCodes.add(qrCode)
        }
    }

    fun getImageQRCode(index: Int) : ByteArrayOutputStream {
        val qrCode = qrCodes[index]
        println("Generating qrCode of address: ${addresses[index].address}")
        return qrcodeToImage(qrCode).stream()
    }

    fun qrcodeToImage(qrCode: QRCode) : QRCode {
        return qrCode.withSize(1024, 1024).to(ImageType.PNG)
    }
    fun createAddress(version: Int = 0){
        val addr = generateAddress(version)
        val addressTransaction = Address(keyPair.public, addr)
        addresses.add(addressTransaction)

        //TODO: BROADCAST ADDRESS CREATION
    }
}

class WalletSerializer : JsonSerializer<Wallet>() {
    override fun serialize(
        value: Wallet, gen: JsonGenerator, provider: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeBinaryField("publicKey", value.keyPair.public.encoded)
        gen.writeBinaryField("privateKey", value.keyPair.private.encoded)
        gen.writeBinaryField("rsaEncryptionKey", value.rsaEncryptionKey.getEncoded())
        gen.writeNumberField("currentValance", value.currentValance)
        gen.writeObjectField("userSettings", value.userSettings)
        gen.writeObjectField("trustedNodes", value.trustedNodes)
        gen.writeObjectField("pendingTransaction", value.pendingTransaction)
        gen.writeObjectField("addresses", value.addresses)
        gen.writeObjectField("transactions", value.transactions)
        gen.writeEndObject()
    }
}
class WalletDeserializer : JsonDeserializer<Wallet>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Wallet {
        val codec: ObjectCodec = p.codec
        val node: ObjectNode = codec.readTree(p)
        val publicKeyBytes: ByteArray = node.get("publicKey").binaryValue()
        val privateKeyBytes: ByteArray = node.get("privateKey").binaryValue()

        val rsaEncryptionKeyBytes: ByteArray = node.get("rsaEncryptionKey").binaryValue()
        val currentValance: Float = node.get("currentValance").floatValue()
        val userSettings: UserSettings = codec.treeToValue(node.get("userSettings"), UserSettings::class.java)
        val trustedNodes: ArrayList<Node> = codec.treeToValue(node.get("trustedNodes"), ArrayList::class.java) as ArrayList<Node>
        val pendingTransaction: ArrayList<Transaction> = codec.treeToValue(node.get("pendingTransaction"), ArrayList::class.java) as ArrayList<Transaction>
        val addresses: ArrayList<Address> = node.get("addresses").map { addressNode -> codec.treeToValue(addressNode, Address::class.java) } as ArrayList<Address>

        val keyFactory: KeyFactory = KeyFactory.getInstance("EC")
        val publicKeySpec: X509EncodedKeySpec = X509EncodedKeySpec(publicKeyBytes)
        val privateKeySpec: PKCS8EncodedKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val publicKey: ECPublicKey = keyFactory.generatePublic(publicKeySpec) as ECPublicKey
        val privateKey: ECPrivateKey = keyFactory.generatePrivate(privateKeySpec) as ECPrivateKey
        val keyPair: KeyPair = KeyPair(publicKey, privateKey)

        val rsaPublicKey: RSAPublicKey = RSAPublicKey.fromEncoded(rsaEncryptionKeyBytes)

        val wallet = Wallet(keyPair, rsaPublicKey)
        wallet.transactions.addAll(node.get("transactions").map { transactionNode -> codec.treeToValue(transactionNode, MoneyTransaction::class.java) })
        wallet.currentValance = currentValance
        wallet.userSettings = userSettings
        wallet.trustedNodes = trustedNodes
        wallet.pendingTransaction = pendingTransaction
        wallet.addresses = addresses
        wallet.generateQRCodes()
        return wallet
    }
}