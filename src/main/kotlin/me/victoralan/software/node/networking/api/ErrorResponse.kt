package me.victoralan.software.node.networking.api

class ErrorResponse(private val _status: Int, private val _message: String){
    val success: Boolean = false
    val status: Int = _status
    val message: String = _message
}