package com.bosch.softtec.scenichiking

import android.location.Location
import android.util.Log
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import java.lang.ref.WeakReference

/*
*
* Created By Goutham Iyyappan on 14/Sep/2019 for Bosch SoftTec.
*
* Listens to location callbacks and inform the activity about the received location.
*
* */
class LocationListeningCallback internal constructor(private val listener: LocationListeningCallback.LocationReceived, activity: MainActivity) :
    LocationEngineCallback<LocationEngineResult> {

    private val activityWeakReference: WeakReference<MainActivity>

    init {
        this.activityWeakReference = WeakReference(activity)
    }

    override fun onSuccess(result: LocationEngineResult?) {
        var location: Location? = null
        if (result != null)
            location = result.lastLocation

        if (location != null) {
            listener.onLocationReceived(location)
        }
    }

    override fun onFailure(exception: Exception) {
        Log.d("LOCATION", "Failed: ${exception.message}")
    }

    interface LocationReceived {
        fun onLocationReceived(location: Location)
    }
}