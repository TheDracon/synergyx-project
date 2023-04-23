package me.victoralan

import me.victoralan.crypto.ecdsa.ECDSA
import me.victoralan.crypto.encoder.Base58
import me.victoralan.software.wallet.Wallet
import me.victoralan.software.wallet.WalletManager
import rsa.keys.generator.RSAKeyGen
import java.io.File
import java.io.FileOutputStream


fun main(args: Array<String>) {

    val publicKey = RSAKeyGen.generateKeyPair("thedracon", 2048).public

    val wallet = Wallet(ECDSA().generateKeyPair(), publicKey)
    wallet.createAddress(10)
    wallet.createAddress(21)
    wallet.generateQRCodes()
    wallet.saveWallet()

    val walletManager = WalletManager()
    val newWallet = walletManager.loadWallet(File("./wallet.synergyx"), "thedracon")

    println(newWallet.rsaEncryptionKey.toString() == wallet.rsaEncryptionKey.toString())

}