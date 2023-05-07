package me.victoralan.utils

enum class NodeRequests(val value: Int) {
    NEW_BLOCKITEM(0),
    NEW_BLOCK(1),
    GET_BLOCKCHAIN(2),
    RESOLVE_ISSUE(3),
}