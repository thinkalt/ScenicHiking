package com.bosch.softtec.scenichiking

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.bosch.softtec.scenichiking.databinding.ActivityMainBinding
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PermissionsListener, OnMapReadyCallback,
    MapboxMap.OnMapLongClickListener,
    MapboxMap.OnMapClickListener {

    companion object {

        private val SOURCE_ID = "destination-source-id"
        private val SELECTED_SOURCE_ID = "selected-source-id"
        private val SYMBOL_LAYER_ID = "destination-symbol-layer-id"
        private val SELECTED_SYMBOL_LAYER_ID = "selected-symbol-layer-id"
        private val ICON_ID = "destination-icon-id"
        private val SELECTED_ICON_ID = "selected-icon-id"
    }

    private var mMapboxMap: MapboxMap? = null
    private var locationComponent: LocationComponent? = null
    private var locationEngine: LocationEngine? = null

    private lateinit var permissionsManager: PermissionsManager
    private lateinit var viewModel: MainViewModel
    private lateinit var callback: LocationListeningCallback
//    private var source: GeoJsonSource? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onStop() {
        super.onStop()
        locationEngine?.removeLocationUpdates(callback)
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).let {
            it.setLifecycleOwner(this)
            it.viewModel = viewModel
        }

        callback = LocationListeningCallback(this)
        permissionsManager = PermissionsManager(this)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mMapboxMap = mapboxMap
        mMapboxMap!!.setStyle(Style.OUTDOORS) { style ->

            enableLocationComponent(style) // Enables user Location and Bearing

            addSymbolLayers(style) // Prepares Markers to be added

            mMapboxMap!!.addOnMapLongClickListener(this) // Adds markers to the map

            mMapboxMap!!.addOnMapClickListener(this)
        }
    }

    /*
    *
    * Markers and Polylines get added here based on user long click action
    * */
    override fun onMapLongClick(point: LatLng): Boolean {
        /*
       * Add Markers
       * */
        viewModel.updateSymbolLayer(point)
        viewModel.updateListOfLatLong(point)

        val source = mMapboxMap!!.style!!.getSourceAs<GeoJsonSource>(SOURCE_ID)
        if (source != null)
            source!!.setGeoJson(FeatureCollection.fromFeatures(viewModel.symbolLayerIconFeatureList.value!!))

        val selectedsource = mMapboxMap!!.style!!.getSourceAs<GeoJsonSource>(SELECTED_SOURCE_ID)
        /*if (selectedsource != null)
            selectedsource!!.setGeoJson(FeatureCollection.fromFeatures(viewModel.symbolLayerIconFeatureList.value!!))*/

        /*
        * Add Straight Polyline between Markers
        * */
        viewModel.updateRouteCoordinates(point) //Adds co-ordinates for lines

        val lineSource = "line-source-${System.currentTimeMillis()}"
        val lineLayer = "linelayer${System.currentTimeMillis()}"
        mMapboxMap!!.style!!.addSource(
            GeoJsonSource(
                lineSource,
                FeatureCollection.fromFeatures(viewModel.featureArray.value!!)
            )
        )

        mMapboxMap!!.style!!.addLayer(
            LineLayer(lineLayer, lineSource).withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(2f),
                lineColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            )
        )
        return true
    }

    override fun onMapClick(point: LatLng): Boolean {

        val screenPoint = mMapboxMap!!.getProjection().toScreenLocation(point)

        val features =
            mMapboxMap!!.queryRenderedFeatures(screenPoint, SYMBOL_LAYER_ID)

        val selectedFeature =
            mMapboxMap!!.queryRenderedFeatures(screenPoint, SELECTED_SYMBOL_LAYER_ID)

        var selectedMarkerSymbolLayer =
            mMapboxMap!!.style!!.getLayer(SELECTED_SYMBOL_LAYER_ID) as? SymbolLayer

        if (features.isEmpty()) {
            return false
        }

        if (selectedFeature.size > 0 && selectedMarkerSymbolLayer != null) {
            mMapboxMap!!.style!!.removeLayer(selectedMarkerSymbolLayer)
            mMapboxMap!!.style!!.removeSource(mMapboxMap!!.style!!.getSource(SELECTED_SOURCE_ID)!!)
            return true
        }

        if (selectedMarkerSymbolLayer == null) {
            addSelectedSourceLayer(mMapboxMap!!.style!!)
            selectedMarkerSymbolLayer =
                mMapboxMap!!.style!!.getLayer(SELECTED_SYMBOL_LAYER_ID) as? SymbolLayer
        }

        mMapboxMap!!.style!!.getSourceAs<GeoJsonSource>(SELECTED_SOURCE_ID)?.setGeoJson(
            FeatureCollection.fromFeatures(
                arrayOf(Feature.fromGeometry(features.get(0).geometry()))
            )
        )

        if (features.size > 0 && selectedMarkerSymbolLayer != null) {
            features.get(0).addBooleanProperty("selected", true)
            selectedMarkerSymbolLayer.setProperties(
                iconImage(SELECTED_ICON_ID)
            )
        }

        return true
    }

    var markerSelected = false
    lateinit var markerAnimator: ValueAnimator

    fun selectMarker(iconLayer: SymbolLayer) {
        markerAnimator = ValueAnimator()
        markerAnimator.setObjectValues(1f, 2f)
        markerAnimator.setDuration(300)
        markerAnimator.addUpdateListener {
            iconLayer.setProperties(
                iconImage(SELECTED_ICON_ID),
                iconSize((it.getAnimatedValue()).toString().toFloat())
            )
        }
        markerAnimator.start()
        markerSelected = true
    }

    fun deselectMarker(iconLayer: SymbolLayer) {
        markerAnimator.setObjectValues(2f, 1f)
        markerAnimator.setDuration(300)
        markerAnimator.addUpdateListener {
            iconLayer.setProperties(
                iconSize((it.getAnimatedValue()).toString().toFloat())
            )
        }
        markerAnimator.start()
        markerSelected = false
    }

    fun zoomOutCameraToShowAllMarkers(): Boolean {
        viewModel.initListOfLatLong()
        if (viewModel.listOfLatLng.value!!.size > 1) {
            var latLngBounds =
                LatLngBounds.Builder().includes(viewModel.listOfLatLng.value!!).build()

            mMapboxMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100))

            return true
        }
        return false
    }

    /*
      *
      * Prepares the Markers to be ready to be added in the map.
      *
      * */
    fun addSymbolLayers(loadedMapStyle: Style) {

        loadedMapStyle.addImage(
            ICON_ID,
            BitmapFactory.decodeResource(this.getResources(), R.drawable.map_marker_light)
        )

        loadedMapStyle.addImage(
            SELECTED_ICON_ID,
            BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_favorite)
        )

        val source = GeoJsonSource(SOURCE_ID)
        loadedMapStyle.addSource(source)

        val destinationSymbolLayer =
            SymbolLayer(SYMBOL_LAYER_ID, SOURCE_ID)

        destinationSymbolLayer.withProperties(
            iconImage(ICON_ID),
            iconAllowOverlap(true),
            iconIgnorePlacement(true),
            iconOffset(arrayOf(0f, -9f))
        )

        loadedMapStyle.addLayer(destinationSymbolLayer)
        addSelectedSourceLayer(loadedMapStyle)
    }

    fun addSelectedSourceLayer(loadedMapStyle: Style) {
        val selectedSource = GeoJsonSource(SELECTED_SOURCE_ID)
        loadedMapStyle.addSource(selectedSource)

        val selectedSymbolLayer = SymbolLayer(SELECTED_SYMBOL_LAYER_ID, SELECTED_SOURCE_ID)

        selectedSymbolLayer.withProperties(
            iconImage(SELECTED_ICON_ID),
            iconAllowOverlap(true),
            iconIgnorePlacement(true),
            iconOffset(arrayOf(0f, -9f))
        )

        loadedMapStyle.addLayer(selectedSymbolLayer)
    }

    /*
   *
   * Displays user location position with a bearing.
   *
   * */
    fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            val locationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.mapboxGreen))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(this, loadedMapStyle)
                .locationComponentOptions(locationComponentOptions)
                .build()


            locationComponent = mMapboxMap?.locationComponent

            // Activate with a built LocationComponentActivationOptions object
            locationComponent?.activateLocationComponent(locationComponentActivationOptions)

            // Enable to make component visible
            locationComponent?.isLocationComponentEnabled = true

            // Set the component's camera mode
            locationComponent?.cameraMode = CameraMode.TRACKING

            // Set the component's render mode
            locationComponent?.renderMode = RenderMode.COMPASS

        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }


    /*
    *
    * Location Permission request and result with an explanation for the need of location permission
    *
    * */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Log.d("Explanation", "Explanation: ${permissionsToExplain!!.get(0)}")
        Toast.makeText(this, R.string.loc_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted)
            enableLocationComponent(mMapboxMap?.style!!)
        else
            Toast.makeText(this, R.string.loc_permission_not_granted, Toast.LENGTH_LONG).show()
    }
}
