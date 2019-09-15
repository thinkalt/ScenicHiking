package com.bosch.softtec.scenichiking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng

class MainViewModel : ViewModel() {

    var routeCoordinates = MutableLiveData<ArrayList<Point>>()
    var symbolLayerIconFeatureList = MutableLiveData<ArrayList<Feature>>()
    var listOfLatLng = MutableLiveData<ArrayList<LatLng>>()
    var featureArray = MutableLiveData<Array<Feature>>()

    fun updateSymbolLayer(point: LatLng) {
        if (symbolLayerIconFeatureList.value == null) {
            symbolLayerIconFeatureList.value = ArrayList()
        }

        val feature = Feature.fromGeometry(
            Point.fromLngLat(point.getLongitude(), point.getLatitude())
        )
        feature.addBooleanProperty("selected", false)
        feature.addStringProperty("name", "${point.getLatitude()}:${point.getLongitude()}")
        val isDuplicateEntry = symbolLayerIconFeatureList.value!!.contains(feature)

        if (!isDuplicateEntry)
            symbolLayerIconFeatureList.value!!.add(feature)
    }

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

    fun initListOfLatLong() {
        if (listOfLatLng.value == null)
            listOfLatLng.value = ArrayList()
    }

    fun updateListOfLatLong(point: LatLng) {
        initListOfLatLong()

        listOfLatLng.value!!.add(LatLng(point.getLatitude(), point.getLongitude()))
    }


}