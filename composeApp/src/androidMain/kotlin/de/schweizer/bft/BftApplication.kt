package de.schweizer.bft

import android.app.Application
import android.content.Context

class BftApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationContextProvider.setup(applicationContext)
    }
}

object ApplicationContextProvider {
    fun setup(context: Context) {
        applicationContext = context
    }

    lateinit var applicationContext: Context
        private set
}
