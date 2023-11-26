package com.example.ui_prototype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Home : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        // Create a list of video items (you can replace this with your actual data)
        val videoItems: List<VideoFeed> = createVideoItems()

        // Create an adapter and set it to the RecyclerView
        val adapter = VideoAdapter(videoItems)
        recyclerView.adapter = adapter

        return view
    }

    private fun createVideoItems(): List<VideoFeed> {
        // Create a list of VideoItem objects with data for each cell
        // You can replace this with your actual data retrieval logic
        val videoItems = mutableListOf<VideoFeed>()

        // Add video items to the list
        videoItems.add(VideoFeed("Video 1", R.drawable.default_profile_picture, "Video description 1"))
        videoItems.add(VideoFeed("Video 2", R.drawable.default_profile_picture, "Video description 2"))
        // Add more video items as needed

        return videoItems
    }
}