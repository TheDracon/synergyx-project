package me.victoralan.entities

open class Receiver(userName: String) : User(userName) {

    companion object{
        fun empty(): Receiver{
            return Receiver("")
        }
    }
}