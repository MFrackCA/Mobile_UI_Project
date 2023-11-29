package com.example.ui_prototype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.ui_prototype.databinding.FragmentProfileBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class Profile : Fragment() {

    // Firebase Instances
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: VideoAdapter
    private lateinit var recyclerView: RecyclerView


    // Binding
    private lateinit var binding: FragmentProfileBinding

    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment using View Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize Firebase Database
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Get current user from FireAuth
        currentUser = auth.currentUser

        recyclerView = view.findViewById(R.id.image_container)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VideoAdapter(emptyList())
        recyclerView.adapter = adapter

        return view
    }

    private fun loadMediaItems() {
        currentUser?.let { user ->
            // Use the user's UID to reference the correct document in the 'users' collection
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {

                        // Grab FireStore Fields
                        val name = document.getString("firstname") ?: "GeoPic User"


                        val mockMediaItems = listOf(
                            MediaObj(
                                title = "Honeybee",
                                profileImageResId = R.drawable.honeybee,
                                description = "Description for Honeybee",
                                mediaUri = "android.resource://com.example.ui_prototype/drawable/honeybee",
                                mediaType = "image",
                                long = 0.0, // Provide a default value for longitude
                                latitude = 0.0, // Provide a default value for latitude
                                username = "default_user", // Provide a default value for username
                                locationName = "default_location" // Provide a default value for locationName
                            ),
                            // Add more mock items as needed
                            )
                        adapter.updateMediaItems(mockMediaItems)
                    }
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

                        // Set Profile Fields
                        if (name.isEmpty()) {
                            name = "GeoPic User"
                        }
                        binding.profileTemplate.profileName.text = name
                        binding.profileTemplate.userName.text = "@$username"
                        binding.profileTemplate.profileDescription.text = bio

                    }
                }
        }
        loadMediaItems()
        binding.profileTemplate.editProfileButton.setOnClickListener {

            findNavController().navigate(R.id.action_Profile_to_EditProfile)

        }

    }


}