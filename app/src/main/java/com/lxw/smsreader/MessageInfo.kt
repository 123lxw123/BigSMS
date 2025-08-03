package com.lxw.smsreader

import java.io.Serializable

data class MessageInfo(
    val id: Long,
    val type: Int,
    val address: String,
    val date: Long,
    val read: Int,
    val time: String,
    val contract: String?,
    val body: String
): Serializable
