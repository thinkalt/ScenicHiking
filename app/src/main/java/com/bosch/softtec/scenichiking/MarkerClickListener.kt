package com.bosch.softtec.scenichiking

/*
*
* Created By Goutham Iyyappan on 14/Sep/2019 for Bosch SoftTec.
*
* Handler for List view clicks to toggle between favorite markers.
*
* */
interface MarkerClickListener {

    fun onSelectMarker(marker: MyMarker)
}