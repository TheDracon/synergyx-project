package me.victoralan.entities


class Sender(userName: String) : User(userName) {


    companion object{
        fun empty(): Receiver{
            return Receiver("")
        }
    }
}