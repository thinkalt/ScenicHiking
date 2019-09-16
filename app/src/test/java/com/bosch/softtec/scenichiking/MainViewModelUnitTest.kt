package com.bosch.softtec.scenichiking

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule

/**
 * MainViewModelUnitTest local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MainViewModelUnitTest {


    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val viewModel by lazy { MainViewModel() }


    /*
    *
    * Tests if LatLng objects are getting added to symbolLayerIconFeatureList
    *
    * */
    @Test
    fun updateSymbolLayer() {
        // given
        val point = LatLng(12.590, 70.590)
        val point1 = LatLng(13.590, 70.590)
        val point2 = LatLng(14.590, 70.590)

        // when
        viewModel.updateSymbolLayer(point)
        viewModel.updateSymbolLayer(point1)
        viewModel.updateSymbolLayer(point2)

        // then
        assertEquals(3, viewModel.symbolLayerIconFeatureList.value!!.size)
    }


    /*
    *
    * Tests if Duplicate LatLng objects are getting added to symbolLayerIconFeatureList
    *
    * */
    @Test
    fun updateSymbolLayer_filterDuplicateMarkerLocation() {
        // given
        val point = LatLng(12.590, 70.590)

        // when
        viewModel.updateSymbolLayer(point)
        viewModel.updateSymbolLayer(point)

        // then
        assertEquals(1, viewModel.symbolLayerIconFeatureList.value!!.size)
    }


    /*
    *
    * Tests if LatLng objects are getting added to routeCoordinates & then further into
    * featureArray object.
    *
    * */
    @Test
    fun updateRouteCoordinates() {
        // given
        val point = LatLng(12.590, 70.590)
        val point1 = LatLng(13.590, 70.590)
        val point2 = LatLng(14.590, 70.590)

        // when
        viewModel.updateRouteCoordinates(point)
        viewModel.updateRouteCoordinates(point1)
        viewModel.updateRouteCoordinates(point2)

        // then
        assertEquals(3, viewModel.routeCoordinates.value!!.size)
        assertEquals(3, viewModel.featureArray.value!!.size)
    }

}
