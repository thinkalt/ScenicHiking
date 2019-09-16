package com.bosch.softtec.scenichiking

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import kotlinx.android.synthetic.main.custom_dialog_layout.*


/*
*
* Created By Goutham Iyyappan on 14/Sep/2019 for Bosch SoftTec.
*
* Dialog to display list of user marked locations on map.
* List items are also enabled to be toggled as favorites
*
* */
class CustomListViewDialog(
    activity: Activity,
    internal var adapter: MarkersRecyclerViewAdapter
) :
    Dialog(activity),
    View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_dialog_layout)

        recycler.adapter = adapter
        no.setOnClickListener(this)
    }

    fun notifyDatasetChanges(adapter: MarkersRecyclerViewAdapter) {
        this.adapter = adapter
        recycler.adapter = adapter
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.no -> dismiss()
        }
        dismiss()
    }
}