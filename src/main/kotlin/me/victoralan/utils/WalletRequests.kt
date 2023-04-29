package me.victoralan.utils

enum class WalletRequests(val value: Int) {
    NEW_TRANSACTION(0),
    CHECK_IF_TRANSACTION_VALIDATED(1),
    CHECK_BALANCE(3),
}