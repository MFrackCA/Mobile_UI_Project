package com.example.ui_prototype

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Home : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VideoAdapter
    private val db = Firebase.firestore

    // Flag to indicate whether the app is in development mode
    private val isDevelopmentMode = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VideoAdapter(emptyList())
        recyclerView.adapter = adapter

        loadMediaItems()

        return view
    }

    private fun loadMediaItems() {
        if (isDevelopmentMode) {
            // Load mock data
            val mockMediaItems = listOf(
                MediaObj(
                    "Honeybee",
                    R.drawable.honeybee,
                    "Description for Honeybee",
                    "android.resource://com.example.ui_prototype/drawable/honeybee",
                    "image"
                ),
                MediaObj(
                    "Dog Video",
                    R.drawable.default_profile_picture,
                    "Description for Dog Video",
                    "android.resource://com.example.ui_prototype/" + R.raw.dog,
                    "video"
                ),
                MediaObj(
                    "Falls Video",
                    R.drawable.default_profile_picture,
                    "Description for Falls Video",
                    "android.resource://com.example.ui_prototype/" + R.raw.falls,
                    "video"
                ),


                // Add more mock items as needed
            )
            adapter.updateMediaItems(mockMediaItems)
        } else {
            // Production mode, load data from Firestore
            db.collection("usermedia")
                .limit(10) // Fetch a limited number of documents
                .get()
                .addOnSuccessListener { documents ->
                    val mediaItems = documents.mapNotNull { document ->
                        val title = document.getString("mediaName") ?: "Unknown"
                        val mediaUri = document.getString("mediaUrl") ?: return@mapNotNull null
                        val mediaType = document.getString("mediaType") ?: return@mapNotNull null
                        val profileImageResId = R.drawable.default_profile_picture
                        val description = "Some description"
                        MediaObj(title, profileImageResId, description, mediaUri, mediaType)
                    }
                    adapter.updateMediaItems(mediaItems)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting media documents: ", exception)
                }
        }
    }
}
