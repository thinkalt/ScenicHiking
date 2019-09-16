package com.bosch.softtec.scenichiking

import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
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

/*
*
* Created By Goutham Iyyappan on 14/Sep/2019 for Bosch SoftTec.
*
* MainActivity class that represents all the UI related the Scenic Hiking App.
* 1. Displays Map
* 2. Displays User position and Bearing
* 3. Has ability to add a Marker on the Map
* 4. Displays all markers in a list using a recyclerview
* 5. Adds ability to Toggle between Favorite marker locations from the list.
*
* */
class MainActivity : AppCompatActivity(), PermissionsListener, OnMapReadyCallback,
    MapboxMap.OnMapLongClickListener,
    MapboxMap.OnMapClickListener, MarkerClickListener, LocationListeningCallback.LocationReceived {

    companion object { //Static references

        private val SOURCE_ID = "destination-source-id"
        private val SELECTED_SOURCE_ID = "selected-source-id"
        private val SYMBOL_LAYER_ID = "destination-symbol-layer-id"
        private val SELECTED_SYMBOL_LAYER_ID = "selected-symbol-layer-id"
        private val ICON_ID = "destination-icon-id"
        private val SELECTED_ICON_ID = "selected-icon-id"
    }

    var mMapboxMap: MapboxMap? = null
    private var mLocationComponent: LocationComponent? = null
    private var mLocationEngine: LocationEngine? = null
    private var mCustomDialog: CustomListViewDialog? = null

    private lateinit var mPermissionsManager: PermissionsManager
    private lateinit var mViewModel: MainViewModel
    private lateinit var mLocationcallback: LocationListeningCallback

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
            mPermissionsManager = PermissionsManager(this)
            mPermissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onStop() {
        super.onStop()
        mLocationEngine?.removeLocationUpdates(mLocationcallback)
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLocationReceived(location: Location) {
        val position = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(25.0)
            .tilt(20.0)
            .build()

        if (mMapboxMap != null)
            mMapboxMap!!.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    position
                ), 7000
            )
    }

    /*
        *
        * Method acts as entry point to UI elements, setting up databinding and initializing mapviews,
        * location callback and permission manager.
        *
        * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).let {
            it.setLifecycleOwner(this)
            it.viewModel = mViewModel
        }

        mPermissionsManager = PermissionsManager(this)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        mLocationcallback = LocationListeningCallback(this, this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    /*
    *
    * Hides showlist option item until any markers are added
    * */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu!!.setGroupVisible(
            R.id.group,
            (mViewModel.markers.value != null && mViewModel.markers.value!!.size > 0)
        )
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                updateReceyclerView()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*
    *
    * Called when the map is ready to be used.
    *
    * @param mapboxMap An instance of MapboxMap.
    *
    * */
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
    *
    * @param point An instance of LatLng.
    *
    * */
    override fun onMapLongClick(point: LatLng): Boolean {

        /*
       * Add Markers
       * */
        mViewModel.updateSymbolLayer(point)
        mViewModel.updateListOfLatLong(point)

        val source = mMapboxMap!!.style!!.getSourceAs<GeoJsonSource>(SOURCE_ID)
        if (source != null)
            source.setGeoJson(FeatureCollection.fromFeatures(mViewModel.symbolLayerIconFeatureList.value!!))

        addStraightPolylines(point)
        mViewModel.addMarker(SYMBOL_LAYER_ID, SOURCE_ID, point, false)

        invalidateOptionsMenu()
        return true
    }

    /*
    *
    * Displays a custom Dialog with a list of markers added by user.
    *
    * */
    fun updateReceyclerView() {
        if (mViewModel.markers.value != null && mViewModel.markers.value!!.size > 0) {
            val adapter = MarkersRecyclerViewAdapter(this, mViewModel.markers.value!!, this)

            //when list is already shown, just update the list instead of recreating the dialog.
            if (mCustomDialog != null) {
                mCustomDialog!!.notifyDatasetChanges(adapter)
            } else {
                mCustomDialog = CustomListViewDialog(this, adapter)
            }

            mCustomDialog!!.show()
            mCustomDialog!!.setCanceledOnTouchOutside(true)
        }
    }

    /*
    *
    * onclick method handling to toggle marker as favorite or not.
    *
    * */
    override fun onSelectMarker(marker: MyMarker) {
        if (marker.isFavorite) {
            addFavoriteMarkerOnMap(marker.point)
        } else {
            removeFavMarkerOnMap(marker)
        }

        mViewModel.updateMarker(marker.layerId, marker.sourceId, marker.point, marker.isFavorite)
        updateReceyclerView()
    }


    /*
   *
   * Adds a Straight Polyline between Markers in the order of creation.
   *
   * */
    fun addStraightPolylines(point: LatLng) {

        mViewModel.updateRouteCoordinates(point) //Adds co-ordinates for lines

        val lineSource = "line-source-${System.currentTimeMillis()}"
        val lineLayer = "linelayer${System.currentTimeMillis()}"
        mMapboxMap!!.style!!.addSource(
            GeoJsonSource(
                lineSource,
                FeatureCollection.fromFeatures(mViewModel.featureArray.value!!)
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
    }

    /*
    *
    * method to handle user click action on the map.
    * zooms out/in to display all markers created by user.
    *
    * */
    override fun onMapClick(point: LatLng): Boolean {
        zoomOutCameraToShowAllMarkers()
        return true
    }

    fun zoomOutCameraToShowAllMarkers(): Boolean {
        mViewModel.initListOfLatLong()
        if (mViewModel.listOfLatLng.value!!.size > 1) {
            var latLngBounds =
                LatLngBounds.Builder().includes(mViewModel.listOfLatLng.value!!).build()

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
    }

    /*
    *
    * Adds favorite icons on Marker locations in the map.
    *
    * */
    fun addFavoriteMarkerOnMap(point: LatLng) {
        val uniqueSourceId = SELECTED_SOURCE_ID + System.currentTimeMillis()
        val uniqueLayerId = SELECTED_SYMBOL_LAYER_ID + System.currentTimeMillis()

        val feature = Feature.fromGeometry(
            Point.fromLngLat(point.getLongitude(), point.getLatitude())
        )
        mViewModel.createFavoriteMarker(uniqueSourceId, uniqueLayerId, point, true)

        val selectedSource = GeoJsonSource(uniqueSourceId, FeatureCollection.fromFeature(feature))
        mMapboxMap!!.style!!.addSource(selectedSource)

        val selectedSymbolLayer = SymbolLayer(uniqueLayerId, uniqueSourceId)

        selectedSymbolLayer.withProperties(
            iconImage(SELECTED_ICON_ID),
            iconAllowOverlap(true),
            iconIgnorePlacement(true),
            iconOffset(arrayOf(0f, -9f))
        )
        mMapboxMap!!.style!!.addLayer(selectedSymbolLayer)
    }

    /*
    *
    * Removes favorite icons from Marker locations in the map.
    *
    * */
    fun removeFavMarkerOnMap(marker: MyMarker) {
        val favMarker = mViewModel.removeFavoriteMarker(marker)
        if (favMarker != null) {
            val selectedMarkerSymbolLayer =
                mMapboxMap!!.style!!.getLayer(favMarker.layerId) as? SymbolLayer
            val source = mMapboxMap!!.style!!.getSource(favMarker.sourceId)

            if (selectedMarkerSymbolLayer != null && source != null) {
                mMapboxMap!!.style!!.removeLayer(selectedMarkerSymbolLayer)
                mMapboxMap!!.style!!.removeSource(source)
            }
        }
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


            mLocationComponent = mMapboxMap?.locationComponent

            // Activate with a built LocationComponentActivationOptions object
            mLocationComponent?.activateLocationComponent(locationComponentActivationOptions)

            // Enable to make component visible
            mLocationComponent?.isLocationComponentEnabled = true

            // Set the component's camera mode
            mLocationComponent?.cameraMode = CameraMode.TRACKING

            // Set the component's render mode
            mLocationComponent?.renderMode = RenderMode.COMPASS

        } else {
            mPermissionsManager = PermissionsManager(this)
            mPermissionsManager.requestLocationPermissions(this)
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
        mPermissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
