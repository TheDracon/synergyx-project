package me.victoralan.encoder

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import me.victoralan.blockchain.Block
import me.victoralan.blockchain.blockitems.Address
import me.victoralan.blockchain.blockitems.BlockItem
import me.victoralan.blockchain.blockitems.Transaction
import me.victoralan.crypto.ecdsa.ECDSA
import me.victoralan.entities.User
import java.io.*


class JacksonEncoder {
    fun encodeBlock(block: Block): String{
        val objectMapper = ObjectMapper()
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        return objectMapper.writeValueAsString(block)
    }
    fun decodeBlock(jsonString: String): Block{
        val objectMapper = ObjectMapper()
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        return objectMapper.readValue(jsonString, Block::class.java)
    }
}

fun main(string: Array<String>){

    val blockItems: ArrayList<BlockItem> = ArrayList()
    blockItems.add(Transaction(User("user1"), User("user2"), 1f))

    val block = Block(blockItems, System.nanoTime(), 1)

    try {
        val fos = FileOutputStream("myfile.ser")
        val oos = ObjectOutputStream(fos)
        oos.writeObject(block)
        oos.close()
        fos.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }



    val fis = FileInputStream("myfile.ser")
    val ois = ObjectInputStream(fis)
    var obj = ois.readObject() as Block
    ois.close()
    fis.close()
    println(block.transactions.joinToString(", "))
    println(block.transactions.joinToString(", ") == obj.transactions.joinToString(", "))

}