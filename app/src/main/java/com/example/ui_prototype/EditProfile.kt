package com.example.ui_prototype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ui_prototype.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class EditProfile : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

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

                        userNameField.setText(username)
                        bioField.setText(bio)
                        firstNameField.setText(firstName)
                        lastNameField.setText(lastName)
                        phoneNumberField.setText(phone)

                    }
                }
        }




        binding.editProfilePicture.setOnClickListener {

        }

        binding.bannerImageEdit.setOnClickListener {

        }


        binding.saveProfileButton.setOnClickListener {

            if (userNameField.text.toString() == "") {
                Toast.makeText(context, "Username cannot be empty.", Toast.LENGTH_SHORT).show()
            } else {
                val data = HashMap<String, String?>()
                    data["username"] = userNameField.text.toString()
                    data["bio"] = bioField.text.toString()
                    data["firstname"] = firstNameField.text.toString()
                    data["lastname"] = lastNameField.text.toString()
                    data["phoneNumber"] = phoneNumberField.text.toString()
                    data["photo"] = null

                currentUser?.let { user ->
                    firestore.collection("users").document(user.uid).update(data as MutableMap<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Updated profile!", Toast.LENGTH_SHORT).show()
                        }

                }
            }


        }


    }


}