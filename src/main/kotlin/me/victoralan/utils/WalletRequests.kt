package me.victoralan.utils

enum class WalletRequests(val value: Int) {
    NEW_BLOCKITEM(0),
    CHECK_IF_BLOCKITEM_VALIDATED(1),
    CHECK_BALANCE(3),
}