package com.example.ui_prototype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.material3.Snackbar

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.ui_prototype.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class Profile : Fragment() {

    // Firebase Instances
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: VideoAdapter
    private lateinit var myGridView: GridView
    private lateinit var gridAdapter: GridVAdapter


    // Binding
    private lateinit var binding: FragmentProfileBinding

    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment using View Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize Firebase Database
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Get current user from FireAuth
        currentUser = auth.currentUser

        myGridView = view.findViewById(R.id.myGridView)
        val mediaList = mutableListOf<GridVModel>()

        currentUser?.let { user ->
            firestore.collection("usermedia").whereEqualTo("uid", user.uid).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val mediaUrl = document.get("mediaUrl") as String
                        val mediaType = document.get("mediaType") as String

                        mediaList.add(GridVModel(mediaUrl, mediaType))
                    }
                    gridAdapter = GridVAdapter(mediaList, requireContext())

                    myGridView.adapter = gridAdapter

                    myGridView.onItemClickListener =
                        AdapterView.OnItemClickListener { parent, gview, position, id ->

                            // get videoView from gview
                            val videoView = gview.findViewById<VideoView>(R.id.video_preview)
                            val imageView = gview.findViewById<ImageView>(R.id.image_preview)

                            if (mediaList[position].mediaType == "video") {
                                videoView.start()
                            }
                        }
                }
        }


        return view
    }

    private fun loadMediaItems() {
        currentUser?.let { user ->
            firestore.collection("usermedia").whereEqualTo("uid", user.uid).get()
                .addOnSuccessListener { documents ->
                    val mediaItems = mutableListOf<MediaObj>()
                    for (document in documents) {
                        val mediaItem = document.toObject(MediaObj::class.java)
                        mediaItems.add(mediaItem)
                    }
                    adapter.mediaItems = mediaItems
                    adapter.notifyDataSetChanged()
                }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser?.let { user ->
            // Use the user's UID to reference the correct document in the 'users' collection
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {

                        // Grab FireStore Fields
                        val username = document.getString("username") ?: "No Username"
                        var name = document.getString("firstname") ?: "GeoPic User"
                        val bio = document.getString("bio") ?: ""
                        val profileImage = document.getString("photo") ?: ""
                        // Set Profile Fields
                        if (name.isEmpty()) {
                            name = "GeoPic User"
                        }
                        binding.profileTemplate.profileName.text = name
                        binding.profileTemplate.userName.text = "@$username"
                        binding.profileTemplate.profileDescription.text = bio
                        profileImage?.let {
                            Glide.with(requireContext())
                                .load(it)
                                .placeholder(R.drawable.default_profile_picture) // Add a placeholder if needed
                                .error(R.drawable.default_profile_picture) // Add an error placeholder if needed
                                .into(binding.profileImage)
                        }

                    }
                }
        }
//        loadMediaItems()
        binding.profileTemplate.editProfileButton.setOnClickListener {

            findNavController().navigate(R.id.action_Profile_to_EditProfile)

        }

    }


}