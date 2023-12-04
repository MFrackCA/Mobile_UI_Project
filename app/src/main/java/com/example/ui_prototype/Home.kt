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


    // Initialize components database firestore and sqlite
    // Initialize VideoAdapter and recyclerview
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VideoAdapter
    private val db = Firebase.firestore
    private val dbHelper: MediaObjDBHelper by lazy { MediaObjDBHelper(requireContext()) }


    // Flag to only load mock items not all content from database
    // for testing and dev purposes
    private val isDevelopmentMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VideoAdapter(emptyList())
        recyclerView.adapter = adapter

        // Load media items from the database
        loadMediaItems()

        return view
    }

    private fun loadMediaItems() {
        if (isDevelopmentMode) {
            // Load mock data for testing and debuggign with less overhead
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
            )

            adapter.updateMediaItems(mockMediaItems)
        } else {
            // Load All Data from Firestore and SQ Lite
            loadFirestoreData()
            }
    }

    // get all content from SQ lite database
    private fun loadSQLiteData(mediaItems: MutableList<MediaObj>) {
        val sqliteData = dbHelper.getAllMediaObj()
        if (sqliteData != null) {
            mediaItems.addAll(sqliteData)
        }
        adapter.updateMediaItems(mediaItems)
    }
    // Load data from Fire store
    private fun loadFirestoreData() {
        val mediaItems = mutableListOf<MediaObj>()
        db.collection("usermedia")
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Call SQLite data loading if Firestore data is empty
                    loadSQLiteData(mediaItems)
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    val uid = document.getString("uid") ?: continue // Skip if UID is null

                    // Fetch user info
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDocument ->
                            val username = userDocument.getString("username") ?: "Unknown"
                            val profileImageResId = R.drawable.default_profile_picture // Use a default drawable resource

                            // Extract the media details
                            val title = document.getString("mediaName") ?: "Unknown"
                            val mediaUri = document.getString("mediaUrl") ?: ""
                            val mediaType = document.getString("mediaType") ?: ""
                            val description = document.getString("description") ?: "No description available"
                            val longitude = document.getDouble("longitude") ?: 0.0
                            val latitude = document.getDouble("latitude") ?: 0.0
                            val locationName = document.getString("locationName") ?: "Unknown location"

                            // Create a media object and add it to the list
                            mediaItems.add(
                                MediaObj(
                                    title,
                                    profileImageResId,
                                    description,
                                    mediaUri,
                                    mediaType,
                                    longitude,
                                    latitude,
                                    username,
                                    locationName
                                )
                            )

                            // Update adapter when all Firestore data is processed
                            if (mediaItems.size == documents.size()) {
                                loadSQLiteData(mediaItems)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error getting user data", e)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting media documents: ", exception)
                loadSQLiteData(mediaItems) // Call SQLite data loading if there is an error fetching Firestore data
            }
    }
}
