package com.lxw.smsreader

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

object DataStoreUtil {
    val SMS_ADDRESS_ALL = "ALL"
    private val READ_STATUS_PRE = "read_status_"
    private val DELETE_STATUS_PRE = "delete_status_"
    val Context.readDataStore: DataStore<Preferences> by preferencesDataStore(name = "smsReadStatus")
    val Context.deleteDataStore: DataStore<Preferences> by preferencesDataStore(name = "smsDeleteStatus")

    fun readKey(id: Long): String {
        return "${READ_STATUS_PRE}$id"
    }

    fun deleteKey(id: Long): String {
        return "${DELETE_STATUS_PRE}$id"
    }
}