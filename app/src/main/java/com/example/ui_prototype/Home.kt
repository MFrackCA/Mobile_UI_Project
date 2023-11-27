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
    private var cachedMediaItems: List<MediaObj>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VideoAdapter(emptyList())
        recyclerView.adapter = adapter

        // TODO: see if this works lol
        // Load media items from cache or fetch from Firestore if not cached
        if (cachedMediaItems == null) {
            loadMediaItems()
        } else {
            // Load from cache
            adapter.updateMediaItems(cachedMediaItems!!)
        }

        return view
    }

    private fun loadMediaItems() {
        db.collection("usermedia")
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
