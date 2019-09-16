package com.bosch.softtec.scenichiking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_marker.view.*

/*
*
* Created By Goutham Iyyappan on 14/Sep/2019 for Bosch SoftTec.
*
* Adapter class to handle and represent list of markers added by user.
*
* */
class MarkersRecyclerViewAdapter(
    val context: Context,
    val markers: ArrayList<MyMarker>,
    val listener: MarkerClickListener
) :
    RecyclerView.Adapter<MarkersRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marker, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = markers.get(position)
        holder.name.text = "Marker ${position}"

        // displaying lat long only upto 4 decimal points for better look and feel
        holder.latitude.text = "Latitude: ${String.format("%.4f", item.point.latitude)}"
        holder.longitude.text = "Longitude ${String.format("%.4f", item.point.longitude)}"


        val itemSelected = item.isFavorite
        holder.favorite.visibility = if (itemSelected) View.VISIBLE else View.INVISIBLE

        with(holder.mView) {
            tag = item
            setOnClickListener {
                item.isFavorite = !itemSelected
                listener.onSelectMarker(item)
            }
        }
    }

    override fun getItemCount(): Int = markers.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val name: TextView = mView.tv_name
        val latitude: TextView = mView.lati
        val longitude: TextView = mView.longi
        val favorite: ImageView = mView.fav_image

        override fun toString(): String {
            return super.toString() + " '" + name.text + "'"
        }
    }
}
