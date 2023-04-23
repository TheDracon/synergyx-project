package me.victoralan.entities

import java.io.Serializable

open class User(var userName: String, var money: Float = 0f) : Serializable{}
