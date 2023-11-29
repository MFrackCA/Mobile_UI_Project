package com.example.ui_prototype

import MediaObjDBHelper
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment(), OnMapReadyCallback {
    private val dbHelper: MediaObjDBHelper by lazy { MediaObjDBHelper(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this::onMapReady)
    }

//    override fun onMapReady(googleMap: GoogleMap) {
//        val toronto = LatLng(43.6532, -79.3832)
//        googleMap.addMarker(MarkerOptions().position(toronto).title("Marker in Toronto"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(toronto))
//
//        googleMap.setOnMarkerClickListener {
//            findNavController().navigate(R.id.action_MapsFragment_to_LocationFeed)
//            false
//        }
//    }

    override fun onMapReady(googleMap: GoogleMap) {
        val maplist = dbHelper.getAllLocations()
        if (maplist != null) {
            val markerToMediaObjMap = mutableMapOf<Marker, MediaObj>()

            for (mediaObj in maplist) {
                val markerPosition = mediaObj.long?.let { mediaObj.latitude?.let { it1 -> LatLng(it1, it) } }
                val marker = markerPosition?.let { MarkerOptions().position(it).title(mediaObj.title) }
                    ?.let { googleMap.addMarker(it) }
                markerPosition?.let { CameraUpdateFactory.newLatLngZoom(it, 10f) }
                    ?.let { googleMap.moveCamera(it) }

                if (marker != null) {
                    markerToMediaObjMap[marker] = mediaObj
                }
            }

            googleMap.setOnMarkerClickListener { marker ->
                val mediaObj = markerToMediaObjMap[marker]
                mediaObj?.mediaUri?.let { playVideo(it) }
                false
            }
        }
    }
    private fun playVideo(mediaUri: String) {
        val dialogFragment = VideoPlaybackDialogFragment().apply {
            arguments = Bundle().apply {
                putString("mediaUri", mediaUri)
            }
        }
        dialogFragment.show(requireFragmentManager(), "VideoPlayback")
    }
}
