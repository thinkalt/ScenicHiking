package com.bosch.softtec.scenichiking.framework

import android.app.Application
import com.bosch.softtec.scenichiking.R
import com.mapbox.mapboxsdk.Mapbox

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(
            getApplicationContext(),
            resources.getString(R.string.mapbox_access_token)
        )
    }
}