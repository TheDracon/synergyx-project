package me.victoralan.entities

import java.io.Serializable

open class User(var userName: String, var balance: Float = 0f) : Serializable{}
