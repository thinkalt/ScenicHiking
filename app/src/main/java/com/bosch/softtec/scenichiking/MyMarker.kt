package com.bosch.softtec.scenichiking

import com.mapbox.mapboxsdk.geometry.LatLng

/*
*
* Created By Goutham Iyyappan on 14/Sep/2019 for Bosch SoftTec.
*
* Model class to maintain marker ids, latLng and toggle favorites
* */
class MyMarker(
    var layerId: String,
    var sourceId: String,
    var point: LatLng,
    var isFavorite: Boolean
) {

    /*
    *
    * Combinational string of latitude and longitude to identify uniquiness of a marker.
    * Two markers cannot have same lat and long, in other words, duplicate entries are
    * not allowed.
    * This method only returns a string as an id to represent a unique marker.
    *
    * */
    fun fetchLatLng(): String {
        return "${point.latitude}:${point.longitude}"
    }

}