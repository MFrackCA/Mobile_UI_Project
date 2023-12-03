package com.example.ui_prototype

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ui_prototype.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException


class EditProfile : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    private var filePath: Uri? = null
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        storageReference = FirebaseStorage.getInstance().reference
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        currentUser = auth.currentUser
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userNameField = binding.username
        val bioField = binding.bio
        val firstNameField = binding.firstName
        val lastNameField = binding.lastName
        val phoneNumberField = binding.phoneNumber
        val profilePicEdit = binding.editProfilePicture


        currentUser?.let { user ->
            // Use the user's UID to reference the correct document in the 'users' collection
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {

                        // Grab FireStore Fields
                        val username = document.getString("username")
                        val bio = document.getString("bio")
                        val firstName = document.getString("firstname")
                        val lastName = document.getString("lastname")
                        val phone = document.getString("phoneNumber")
                        val profileImage = document.getString("photo") ?: ""

                        userNameField.setText(username)
                        bioField.setText(bio)
                        firstNameField.setText(firstName)
                        lastNameField.setText(lastName)
                        phoneNumberField.setText(phone)
                        profileImage?.let {
                            Glide.with(requireContext())
                                .load(it)
                                .placeholder(R.drawable.default_profile_picture) // Add a placeholder if needed
                                .error(R.drawable.default_profile_picture) // Add an error placeholder if needed
                                .into(binding.editProfilePicture)
                        }


                    }
                }
        }

        binding.editProfilePicture.setOnClickListener {
            launchGallery()
        }

        binding.bannerImageEdit.setOnClickListener {

        }


        binding.saveProfileButton.setOnClickListener {

            if (userNameField.text.toString() == "") {
                Toast.makeText(context, "Username cannot be empty.", Toast.LENGTH_SHORT).show()
            } else {

                if (filePath != null) {
                    // Upload the selected image to Firebase Storage
                    uploadProfilePicture(filePath!!)
                }
                val data = HashMap<String, String?>()
                data["username"] = userNameField.text.toString()
                data["bio"] = bioField.text.toString()
                data["firstname"] = firstNameField.text.toString()
                data["lastname"] = lastNameField.text.toString()
                data["phoneNumber"] = phoneNumberField.text.toString()
                if (filePath != null) {
                    data["photo"] = filePath.toString()
                }

                currentUser?.let { user ->
                    firestore.collection("users").document(user.uid)
                        .update(data as MutableMap<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Updated profile!", Toast.LENGTH_SHORT).show()
                        }

                }
            }

        }

    }

    private fun launchGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private var imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                filePath = it
                try {
                    Glide.with(requireContext())
                        .load(it)
                        .placeholder(R.drawable.default_profile_picture) // Add a placeholder if needed
                        .error(R.drawable.default_profile_picture) // Add an error placeholder if needed
                        .into(binding.editProfilePicture)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private fun uploadProfilePicture(uri: Uri) {
        // Get a reference to the storage location
        val storageRef = storageReference.child("profilePictures/${currentUser?.uid}.jpg")

        // Upload the file
        storageRef.putFile(uri)
            .addOnSuccessListener {
                // Get the download URL for the uploaded image
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Update the 'photo' field in Firestore with the download URL
                    currentUser?.let { user ->
                        firestore.collection("users").document(user.uid)
                            .update("photo", downloadUri.toString())
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Profile picture updated!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Failed to update profile picture.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to upload profile picture: $e", Toast.LENGTH_SHORT)
                    .show()
            }
    }


}