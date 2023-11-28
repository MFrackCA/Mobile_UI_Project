package com.example.ui_prototype

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
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment(), OnMapReadyCallback {

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
        // Dummy MediaObj
        val dummyMediaObj = object {
            val title = "Dog Video"
            val profileImageResId = R.drawable.default_profile_picture // Your default image resource
            val description = "Sample video of a dog"
            val mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.dog // Your raw resource
            val mediaType = "video"
            val latitude = 43.6532 // Dummy latitude, e.g., Toronto
            val longitude = -79.3832 // Dummy longitude
        }

        // Add marker for the dummy object
        val markerPosition = LatLng(dummyMediaObj.latitude, dummyMediaObj.longitude)
        googleMap.addMarker(MarkerOptions().position(markerPosition).title(dummyMediaObj.title))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 10f))

        googleMap.setOnMarkerClickListener { marker ->
            if (marker.position == markerPosition) {
                playVideo(dummyMediaObj.mediaUri)
            }
            false
        }
    }

    private fun playVideo(mediaUri: String) {
        val dialogFragment = VideoPlaybackDialogFragment().apply {
            arguments = Bundle().apply {
                putString("mediaUri", mediaUri)
            }
        }
        dialogFragment.show(parentFragmentManager, "VideoPlayback")
    }
}