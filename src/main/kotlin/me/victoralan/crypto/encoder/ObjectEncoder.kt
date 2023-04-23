package me.victoralan.crypto.encoder

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


class ObjectEncoder {
    fun <T> encode(obj: T): ByteArray{
        // create a ByteArrayOutputStream to write the serialized object to
        val byteStream = ByteArrayOutputStream()
        // create an ObjectOutputStream to serialize the object to the byte stream
        val objectStream = ObjectOutputStream(byteStream)
        objectStream.writeObject(obj)
        objectStream.flush()
        // get the serialized object as a byte array
        val serializedObject = byteStream.toByteArray()
        // close the streams
        objectStream.close()
        byteStream.close()
        return serializedObject
    }
    fun <T> decode(bytes: ByteArray): T {
        val bis = ByteArrayInputStream(bytes)
        val ois = ObjectInputStream(bis)
        val obj = ois.readObject() as T
        ois.close()
        return obj
    }
}
