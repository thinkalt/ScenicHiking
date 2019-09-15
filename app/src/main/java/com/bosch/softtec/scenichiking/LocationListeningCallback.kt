package com.bosch.softtec.scenichiking

import android.location.Location
import android.util.Log
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import java.lang.Exception
import java.lang.ref.WeakReference

class LocationListeningCallback internal constructor(activity: MainActivity) :
    LocationEngineCallback<LocationEngineResult> {

    private val activityWeakReference: WeakReference<MainActivity>

    init {
        this.activityWeakReference = WeakReference(activity)
    }

    override fun onSuccess(result: LocationEngineResult?) {
        var location: Location? = null
        if (result != null)
            location = result.lastLocation

        if (location != null)
            Log.d("LOCATION", "Lat:${location.latitude}   Long:${location.longitude}")


    }

    override fun onFailure(exception: Exception) {
        Log.d("LOCATION", "Failed: ${exception.message}")
    }
}