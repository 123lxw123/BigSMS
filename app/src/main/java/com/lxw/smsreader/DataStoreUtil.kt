package com.lxw.smsreader

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

object DataStoreUtil {
    private val READ_STATUS_PRE = "read_status_"
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smsReadStatus")

    fun key(id: Long): String {
        return "${READ_STATUS_PRE}$id"
    }
}