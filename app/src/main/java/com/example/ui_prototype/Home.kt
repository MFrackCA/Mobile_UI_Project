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
            title = "Video 1",
            profileImageResId = R.drawable.honeybee,
            description = "Description for Honeybee",
            mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.v1, // Example image URI
            mediaType = "image",
            long = -74.0060, // Example longitude
            latitude = 40.7120, // Example latitude
            username = "example_user",
            locationName = "New York"
        )

        val secondMockMediaObj = MediaObj(
            title = "Video 2",
            profileImageResId = R.drawable.default_profile_picture,
            description = "really cool stuff",
            mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.v2, // Example image URI
            mediaType = "video",
            long = 18.2529, // Example longitude
            latitude = 13.2046, // Example latitude
            username = "idk",
            locationName = "Unknown Location"
        )
        val thirdMockMediaObj = MediaObj(
            title = "Video 3",
            profileImageResId = R.drawable.default_profile_picture,
            description = "Pretty cool video",
            mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.v3, // Example image URI
            mediaType = "video",
            long = 138.2528, // Example longitude
            latitude = 36.2048, // Example latitude
            username = "just_a_dude",
            locationName = "Japan"
        )
        val fourthMockMediaObj = MediaObj(
            title = "Video 4",
            profileImageResId = R.drawable.default_profile_picture,
            description = "Pretty cool video",
            mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.v4, // Example image URI
            mediaType = "video",
            long =  48.8561, // Example longitude
            latitude = 2.3522, // Example latitude
            username = "just_a_dude",
            locationName = "Paris"
        )
        val fifthMockMediaObj = MediaObj(
            title = "Video 5",
            profileImageResId = R.drawable.default_profile_picture,
            description = "Pretty cool video",
            mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.v5, // Example image URI
            mediaType = "video",
            long =  2.3523, // Example longitude
            latitude = 48.8566, // Example latitude
            username = "just_a_dude",
            locationName = "Paris"
        )
        val sixthMockMediaObj = MediaObj(
            title = "Video 5",
            profileImageResId = R.drawable.default_profile_picture,
            description = "Pretty cool video",
            mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.v6, // Example image URI
            mediaType = "video",
            long =  126.9781, // Example longitude
            latitude = 37.5665, // Example latitude
            username = "just_a_dude",
            locationName = "Paris"
        )
        val seventhMockMediaObj = MediaObj(
            title = "Video 5",
            profileImageResId = R.drawable.default_profile_picture,
            description = "Pretty cool video",
            mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.v7, // Example image URI
            mediaType = "video",
            long =  55.2961, // Example longitude
            latitude = 25.2769, // Example latitude
            username = "just_a_dude",
            locationName = "Paris"
        )
        val eighthMockMediaObj = MediaObj(
            title = "Video 5",
            profileImageResId = R.drawable.default_profile_picture,
            description = "Pretty cool video",
            mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.v8, // Example image URI
            mediaType = "video",
            long =  100.5010, // Example longitude
            latitude = 13.7563, // Example latitude
            username = "just_a_dude",
            locationName = "Paris"
        )
        val ninthMockMediaObj = MediaObj(
            title = "Video 5",
            profileImageResId = R.drawable.default_profile_picture,
            description = "Pretty cool video",
            mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.v9, // Example image URI
            mediaType = "video",
            long =  2.3521, // Example longitude
            latitude = 48.8566, // Example latitude
            username = "just_a_dude",
            locationName = "Paris"
        )
        val tenthMockMediaObj = MediaObj(
            title = "Video 5",
            profileImageResId = R.drawable.default_profile_picture,
            description = "Pretty cool video",
            mediaUri = "android.resource://${requireContext().packageName}/" + R.raw.v10, // Example image URI
            mediaType = "video",
            long =  -79.3837, // Example longitude
            latitude = 43.6532, // Example latitude
            username = "just_a_dude",
            locationName = "Toronto"
        )

        dbHelper.insertMediaObj(firstMockMediaObj)
        dbHelper.insertMediaObj(secondMockMediaObj)
        dbHelper.insertMediaObj(thirdMockMediaObj)
        dbHelper.insertMediaObj(fourthMockMediaObj)
        dbHelper.insertMediaObj(fifthMockMediaObj)
        dbHelper.insertMediaObj(sixthMockMediaObj)
        dbHelper.insertMediaObj(seventhMockMediaObj)
        dbHelper.insertMediaObj(eighthMockMediaObj)
        dbHelper.insertMediaObj(ninthMockMediaObj)
        dbHelper.insertMediaObj(tenthMockMediaObj)


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
            val mediaItems = mutableListOf<MediaObj>() // Create a list to collect media objects

            db.collection("usermedia")
                .limit(3) // Fetch a limited number of documents
                .get()
                .addOnSuccessListener { documents ->
                    val expectedResponses = documents.size()
                    var receivedResponses = 0

                    // Check if there are no documents
                    if (documents.isEmpty) {
                        adapter.updateMediaItems(mediaItems) // Update the adapter with an empty list
                        return@addOnSuccessListener
                    }

                    for (document in documents) {
                        val uid = document.getString("uid") ?: continue // Skip if UID is null

                        // Fetch user info
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { userDocument ->
                                receivedResponses++

                                // Process the user document
                                val username = userDocument.getString("username") ?: "Unknown"
                                val profileImageResId = R.drawable.default_profile_picture // Use a default drawable resource

                                // Extract the media details
                                val title = document.getString("mediaName") ?: "Unknown"
                                val mediaUri = document.getString("mediaUrl") ?: ""
                                val mediaType = document.getString("mediaType") ?: ""
                                val description = document.getString("description") ?: "No description available"
                                val long = document.getDouble("longitude") ?: 0.0
                                val latitude = document.getDouble("latitude") ?: 0.0
                                val locationName = document.getString("locationName") ?: "Unknown location"

                                // Create a media object and add it to the list
                                mediaItems.add(MediaObj(
                                    title,
                                    profileImageResId,
                                    description,
                                    mediaUri,
                                    mediaType,
                                    long,
                                    latitude,
                                    username,
                                    locationName
                                ))

                                // If all responses are received, update the adapter
                                if (receivedResponses == expectedResponses) {
                                    adapter.updateMediaItems(mediaItems)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error getting user data", e)
                                receivedResponses++
                                if (receivedResponses == expectedResponses) {
                                    adapter.updateMediaItems(mediaItems)
                                }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting media documents: ")
        }
    }
    }
}
