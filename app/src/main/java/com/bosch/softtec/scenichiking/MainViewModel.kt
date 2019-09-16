package com.bosch.softtec.scenichiking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng


/*
*
* Created By Goutham Iyyappan on 14/Sep/2019 for Bosch SoftTec.
*
* A ViewModel Class for MainActivity.
* All non-ui logic resides here, including variables and model classes.
*
* */
class MainViewModel : ViewModel() {

    var routeCoordinates = MutableLiveData<ArrayList<Point>>()
    var symbolLayerIconFeatureList = MutableLiveData<ArrayList<Feature>>()
    var listOfLatLng = MutableLiveData<ArrayList<LatLng>>()
    var featureArray = MutableLiveData<Array<Feature>>()
    var markers = MutableLiveData<ArrayList<MyMarker>>()
    var favMarkers = MutableLiveData<ArrayList<MyMarker>>()


    /*
    *
    * Create and Add a MyMarker Object into arraylist of MyMarker objects.
    *
    * */
    fun addMarker(sourceId: String, layerId: String, point: LatLng, isSelected: Boolean) {
        val marker = MyMarker(layerId, sourceId, point, isSelected)
        if (markers.value == null) markers.value = ArrayList()
        markers.value!!.add(marker)
    }

    /*
    *
    * update an existing MyMarker Object from arraylist of MyMarker objects.
    * If the marker object is not present in the list, then the method adds it.
    *
    * */
    fun updateMarker(sourceId: String, layerId: String, point: LatLng, isSelected: Boolean) {
        val marker = MyMarker(layerId, sourceId, point, isSelected)
        if (markers.value == null)
            markers.value = ArrayList()

        if (markers.value!!.size > 0)
            for (m in markers.value!!) {
                if (marker.fetchLatLng().equals(m.fetchLatLng())) {
                    m.isFavorite = marker.isFavorite
                }
            }
        else
            markers.value!!.add(marker)
    }

    /*
    *
    * Create and Add a Favorite MyMarker Object into arraylist of Favorite MyMarker objects.
    *
    * */
    fun createFavoriteMarker(
        sourceId: String,
        layerId: String,
        point: LatLng,
        isSelected: Boolean
    ) {
        val marker = MyMarker(layerId, sourceId, point, isSelected)
        if (favMarkers.value == null) favMarkers.value = ArrayList()
        favMarkers.value!!.add(marker)
    }

    /*
    *
    * Remove a MyMarker Object into arraylist of favorite MyMarker objects.
    *
    * */
    fun removeFavoriteMarker(marker: MyMarker): MyMarker? {
        if (favMarkers.value != null && favMarkers.value!!.size > 0) {
            val tempMarkers = favMarkers.value!!

            for (m in tempMarkers) {
                if (marker.fetchLatLng().equals(m.fetchLatLng())) {
                    favMarkers.value!!.remove(m)
                    return m
                }
            }
        }
        return null
    }

    /*
    *
    * Creates an arraylist of Feature objects without Duplicate entries.
    * Feature objects are used in Markers for locations purposes.
    *
    * */
    fun updateSymbolLayer(point: LatLng) {
        if (symbolLayerIconFeatureList.value == null) {
            symbolLayerIconFeatureList.value = ArrayList()
        }

        val feature = Feature.fromGeometry(
            Point.fromLngLat(point.getLongitude(), point.getLatitude())
        )
        val isDuplicateEntry = symbolLayerIconFeatureList.value!!.contains(feature)

        if (!isDuplicateEntry)
            symbolLayerIconFeatureList.value!!.add(feature)
    }

    /*
    *
    * Creates an arraylist of Point objects which inturn adds into array of Feature objects
    * featureArray is used in drawing straight polyline between markers.
    *
    * */
    fun updateRouteCoordinates(point: LatLng) {
        if (routeCoordinates.value == null)
            routeCoordinates.value = ArrayList()

        routeCoordinates.value!!.add(
            Point.fromLngLat(
                point.getLongitude(),
                point.getLatitude()
            )
        ) //Adds co-ordinates for lines

        featureArray.value = Array(routeCoordinates.value!!.size) {
            Feature.fromGeometry(LineString.fromLngLats(routeCoordinates.value!!))
        }
    }

    /*
    *
    * Initialises the  arraylist of LatLng.
    *
    * */
    fun initListOfLatLong() {
        if (listOfLatLng.value == null)
            listOfLatLng.value = ArrayList()
    }

    /*
    *
    * Creates an arraylist of LatLng objects.
    * List of LatLng objects are used to assist in zooming in/out to show all Markers locations .
    *
    * */
    fun updateListOfLatLong(point: LatLng) {
        initListOfLatLong()

        listOfLatLng.value!!.add(LatLng(point.getLatitude(), point.getLongitude()))
    }


}