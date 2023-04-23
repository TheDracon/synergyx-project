package me.victoralan.crypto.ecdsa

import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.*


class ECDSA {
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        val ecGenParameterSpec = ECGenParameterSpec("secp384r1") // Using Bitcoin's curve
        keyPairGenerator.initialize(ecGenParameterSpec, SecureRandom())
        return keyPairGenerator.generateKeyPair()
    }

    // Sign a message using the private key
    fun signMessage(message: ByteArray, privateKey: PrivateKey): ByteArray {
        val signature: Signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey , SecureRandom("".toByteArray()))
        signature.update(message)
        return signature.sign()
    }

    // Verify a message's signature using the public key
    fun verifySignature(message: ByteArray, signature: ByteArray, publicKey: PublicKey): Boolean {
        val sig: Signature = Signature.getInstance("SHA256withECDSA")
        sig.initVerify(publicKey)
        sig.update(message)
        return sig.verify(signature)
    }
}