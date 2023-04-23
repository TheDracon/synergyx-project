package me.victoralan.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex
import java.security.*
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class CryptoUtils {

    private val rsaCipher = Cipher.getInstance("RSA")
    private val aesCipher = Cipher.getInstance("AES")


    fun getKeyFromPassword(password: String, salt: String = password): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 65536, 256)
        return SecretKeySpec(
            factory.generateSecret(spec)
                .encoded, "AES"
        )
    }
    fun generateIv(): IvParameterSpec {
        val iv = ByteArray(16)
        Random(-1).nextBytes(iv)
        return IvParameterSpec(iv)
    }

    fun encryptAES(input: String, key: SecretKey, iv: IvParameterSpec = generateIv()): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val cipherText = cipher.doFinal(input.toByteArray())
        return Base64.getEncoder()
            .encodeToString(cipherText)
    }
    fun encodeSecretKey(secretKey: SecretKey): String {
        val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        return encodedKey
    }

    fun decodeSecretKey(encodedKey: String, algorithm: String = "AES/CBC/PKCS5Padding"): SecretKey {
        val decodedKey = Base64.getDecoder().decode(encodedKey)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, algorithm)
    }

    fun decryptAES(cipherText: String, key: SecretKey, iv: IvParameterSpec = generateIv()): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val plainText = cipher.doFinal(
            Base64.getDecoder()
                .decode(cipherText)
        )
        return plainText.decodeToString()
    }
    fun generateRSAkey(seed: ByteArray? = null, bitSize: Int): KeyPair {
        // Create a secure random number generator with the specified seed
        val secureRandom = SecureRandom.getInstance("SHA1PRNG")
        secureRandom.setSeed(seed)

        // Create a key pair generator with the specified algorithm and secure random
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(bitSize, secureRandom)

        return keyPairGenerator.genKeyPair()
    }
    fun rsaEncrypt(data: ByteArray, rsaPublicKey: PublicKey) : ByteArray {
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
        return rsaCipher.doFinal(data)
    }
    fun rsaEncrypt(data: String, rsaPublicKey: PublicKey) : String {
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
        return rsaCipher.doFinal(data.toByteArray()).decodeToString()
    }

    fun rsaDecrypt(data: ByteArray, rsaPrivateKey: PrivateKey) : ByteArray {
        // Load the RSA private key from a file or other source

        rsaCipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey)
        return rsaCipher.doFinal(data)
    }
    fun rsaDecrypt(data: String, rsaPrivateKey: PrivateKey) : String {
        // Load the RSA private key from a file or other source

        rsaCipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey)
        return rsaCipher.doFinal(data.toByteArray()).decodeToString()
    }
    fun generateKeyPairECC(passphrase: ByteArray): KeyPair {
        Security.addProvider(BouncyCastleProvider())
        val keyGen = KeyPairGenerator.getInstance("EC", "BC")
        keyGen.initialize(256, SecureRandom(passphrase)) // 256-bit key size
        val keyPair = keyGen.generateKeyPair()
        return keyPair
    }
    fun encryptECC(publicKey: PublicKey, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("ECIESwithAES-CBC");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return cipher.doFinal(data)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid public key encoding: ${Hex.toHexString(publicKey.encoded)}", e)
        }
    }

    fun decryptECC(privateKey: PrivateKey, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("ECIESwithAES-CBC");

        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(data)
    }

}