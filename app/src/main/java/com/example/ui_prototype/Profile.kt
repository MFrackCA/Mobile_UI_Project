package com.example.ui_prototype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ui_prototype.databinding.ActivityMainBinding
import com.example.ui_prototype.databinding.FragmentProfileBinding
import com.example.ui_prototype.databinding.ProfileTemplateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Profile : Fragment() {

    // Firebase Instances
    private lateinit var auth: FirebaseAuth;
    private lateinit var firestore : FirebaseFirestore;

    // Binding
    private lateinit var binding: FragmentProfileBinding;


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
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            // Use the user's UID to reference the correct document in the 'users' collection
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {

                        // Grab FireStore Fields
                        val username = document.getString("username") ?: "No Username"
                        val email = document.getString("email") ?: "No Email"

                        // Set Profile Fields
                        binding.profileTemplate.profileName.text = email
                        binding.profileTemplate.userName.text = username
                        // Populate other fields
                    }
                }
        }

        return view
    }

}