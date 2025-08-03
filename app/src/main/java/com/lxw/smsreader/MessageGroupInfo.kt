package com.lxw.smsreader

data class MessageGroupInfo(
    val address: String,
    var unReadCount: Int = 0,
    val messages: MutableList<MessageInfo> =mutableListOf()
)
