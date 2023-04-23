package me.victoralan.software.wallet

import com.fasterxml.jackson.databind.ObjectMapper
import me.victoralan.crypto.CryptoUtils
import me.victoralan.crypto.encoder.Base58
import java.io.File


class WalletManager {
    fun loadWallet(file: File, passphrase: String) : Wallet{
        val mapper = ObjectMapper()
        val key = CryptoUtils().getKeyFromPassword(passphrase)
        println("4: " + Base58.encode(key.encoded)) //

        val decryptedData = CryptoUtils().decryptAES(file.readText(), key)
        println(decryptedData)
        return mapper.readValue(decryptedData, Wallet::class.java)
    }

}