package com.example.ui_prototype

import MediaObjDBHelper
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MapsFragment : Fragment(), OnMapReadyCallback {
    private val db = Firebase.firestore
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

    // When map loads call on add localdb and firestore videos
    override fun onMapReady(googleMap: GoogleMap) {
        // start location for map
        val toronto = LatLng(43.651070, -79.347015)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 10f))


        // Call on database functions for content
        addLocalDatabaseMarkers(googleMap)
        addFireStoreMarkers(googleMap)


        // On click listener for map markers to play video
        googleMap.setOnMarkerClickListener { marker ->
            val mediaUri = marker.tag as? String
            mediaUri?.let {
                playVideo(it)
            }
            true
        }
    }

    // local database grab content
    private fun addLocalDatabaseMarkers(googleMap: GoogleMap) {
        val dbHelper = MediaObjDBHelper(requireContext())
        val maplist = dbHelper.getAllMediaObj()
        maplist?.forEach { mediaObj ->
            mediaObj.long?.let { longitude ->
                mediaObj.latitude?.let { latitude ->
                    val markerPosition = LatLng(latitude, longitude)
                    val marker = googleMap.addMarker(
                        MarkerOptions().position(markerPosition).title(mediaObj.title)
                    )
                    marker?.tag = mediaObj.mediaUri
                }
            }
        }
    }

    // Firestore database markers
    private fun addFireStoreMarkers(googleMap: GoogleMap) {
        // get usermedia collection from firestore
        db.collection("usermedia")
            .whereEqualTo("mediaType", "video")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")
                    val mediaUri = document.getString("mediaUrl")
                    val description = document.getString("description") ?: "No Description"

                    // create markers
                    if (latitude != null && longitude != null && mediaUri != null) {
                        val position = LatLng(latitude, longitude)
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(position).title(description)
                        )
                        marker?.tag = mediaUri
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    // when user clicks on marker call on dialog fragment and play video
    private fun playVideo(mediaUri: String) {
        val dialogFragment = VideoPlaybackDialogFragment().apply {
            arguments = Bundle().apply {
                putString("mediaUri", mediaUri)
            }
        }
        dialogFragment.show(requireFragmentManager(), "VideoPlayback")
    }
}
