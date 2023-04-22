import rsa.keys.RSAKeyPair
import rsa.keys.RSAPrivateKey
import rsa.keys.RSAPublicKey
import rsa.keys.generator.RSAKeyGen
import rsa.signing.RSASign
import rsa.signing.RSAVerify
import sha.SHA3
import java.util.*


fun main(args: Array<String>) {
//    val keyPair: RSAKeyPair = RSAKeyGen.generateKeyPair(random = Random(12), 2048)
//
//    println("PUBLIC KEY: \n${keyPair.public.toString().addNewlines(225)} ${"\n".repeat(2)}")
//    println("PRIVATE KEY: \n${keyPair.private.toString().addNewlines(225)} ${"\n".repeat(2)}")
//
//
//
//    val publicKey = keyPair.public
//    val privateKey = keyPair.private
//
//    val encryptor = RSAEncryptor(publicKey)
//    val decryptor = RSADecryptor(privateKey)
//
//    val encryptedString = encryptor.encryptString("HELLO HOW ARE YOU!")
//    println(encryptedString.addNewlines(225))
//
//    val decryptedString = decryptor.decryptString(encryptedString)
//    println(decryptedString)
    val keyPair: RSAKeyPair = RSAKeyGen.generateKeyPair(random = Random(12), 512)
    var publicKey = keyPair.public
    var privateKey = keyPair.private

    val encodedP = publicKey.getEncoded()
    val encodedS = privateKey.getEncoded()

    publicKey = RSAPublicKey.fromEncoded(encodedP)
    privateKey = RSAPrivateKey.fromEncoded(encodedS)

    val signer = RSASign(privateKey)
    val verifir = RSAVerify(publicKey)
    var signature = signer.sign("Hello Word!".toByteArray())
    println("SIGNATURE: ${signature.decodeToString()}")
    println(verifir.verify(signature, "Hello Word!".toByteArray()))
}
fun String.addNewlines(chunkSize: Int): String {
    return this.chunked(chunkSize) { "$it\n" }.joinToString("")
}

