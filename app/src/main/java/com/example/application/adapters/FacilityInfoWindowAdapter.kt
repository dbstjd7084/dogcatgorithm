package com.example.application.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.application.R
import com.example.application.structs.googlemap.Facility
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class FacilityInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        // 기본 윈도우 대신 전체를 커스텀하려면 이 메서드를 사용
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.facility_info_window, null)
        val facility = marker.tag as? Facility ?: return view

        val titleView = view.findViewById<TextView>(R.id.window_facility_title)
        val categoryView = view.findViewById<TextView>(R.id.window_facility_category)
        val addressView = view.findViewById<TextView>(R.id.window_facility_address)

        titleView.text = facility.placeName
        categoryView.text = facility.category
        addressView.text = facility.roadAddress ?: facility.numAddress

        return view
    }
}