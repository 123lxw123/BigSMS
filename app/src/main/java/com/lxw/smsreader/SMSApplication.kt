package com.lxw.smsreader

import android.app.Application
import android.content.Context

class SMSApplication : Application() {
    companion object {
        lateinit var application: Application
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }
}