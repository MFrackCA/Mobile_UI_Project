package com.example.ui_prototype

import MediaObjDBHelper
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
    private val dbHelper: MediaObjDBHelper by lazy { MediaObjDBHelper(requireContext()) }


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

        // Insert the first mock media object into the database
        val firstMockMediaObj = MediaObj(
            title = "Honeybee",
            profileImageResId = R.drawable.honeybee,
            description = "Description for Honeybee",
            mediaUri = "android.resource://com.example.ui_prototype/drawable/honeybee", // Example image URI
            mediaType = "image",
            long = 0.0, // Example longitude
            latitude = 0.0, // Example latitude
            username = "example_user",
            locationName = "Example Location"
        )
        dbHelper.insertMediaObj(firstMockMediaObj)

        // Load media items from the database
        loadMediaItems()

        return view
    }

    private fun loadMediaItems() {
        if (isDevelopmentMode) {
            // Load mock data
            val mockMediaItems = listOf(
                MediaObj(
                    title = "Honeybee",
                    profileImageResId = R.drawable.honeybee,
                    description = "Description for Honeybee",
                    mediaUri = "android.resource://com.example.ui_prototype/drawable/honeybee", // Example image URI
                    mediaType = "image",
                    long = 0.0, // Example longitude
                    latitude = 0.0, // Example latitude
                    username = "example_user",
                    locationName = "Example Location"
                ),
                MediaObj(
                    title = "Dog Video",
                    profileImageResId = R.drawable.default_profile_picture,
                    description = "Description for Dog Video",
                    mediaUri = "android.resource://com.example.ui_prototype/" + R.raw.dog, // Example video URI
                    mediaType = "video",
                    long = 0.0, // Example longitude
                    latitude = 0.0, // Example latitude
                    username = "example_user",
                    locationName = "Example Location"
                ),
                MediaObj(
                    title = "Falls Video",
                    profileImageResId = R.drawable.default_profile_picture,
                    description = "Description for Falls Video",
                    mediaUri = "android.resource://com.example.ui_prototype/" + R.raw.falls, // Example video URI
                    mediaType = "video",
                    long = 0.0, // Example longitude
                    latitude = 0.0, // Example latitude
                    username = "example_user",
                    locationName = "Example Location"
                )
                // Add more mock items as needed
            )

            // Add more mock items as needed
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
                        val long = document.getDouble("longitude") ?: 0.0 // Provide a default value for long
                        val latitude = document.getDouble("latitude") ?: 0.0 // Provide a default value for latitude
                        val username = document.getString("username") ?: "Unknown" // Provide a default value for username
                        val locationName = document.getString("locationName") ?: "Unknown" // Provide a default value for locationName

                        MediaObj(title, profileImageResId, description, mediaUri, mediaType, long, latitude, username, locationName)
                    }
                    adapter.updateMediaItems(mediaItems)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting media documents: ", exception)
                }
        }
    }
}
