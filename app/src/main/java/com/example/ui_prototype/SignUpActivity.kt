package com.example.ui_prototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.ui_prototype.databinding.ActivitySignUpAcitvityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpAcitvityBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpAcitvityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.email.text.toString()
            val pass = binding.pass.text.toString()
            val confirmPass = binding.confirmPass.text.toString()
            val username = binding.username.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty() && username.isNotEmpty()) {
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {

                            // If account creation successful Grab User UID
                            val uid = it.result?.user?.uid
                            // Map User fields for collection
                            if(uid != null){
                                val userMap = hashMapOf(
                                    "UID" to uid,
                                    "username" to username,
                                    "email" to email,
                                    "firstname" to null,
                                    "lastname" to null,
                                    "photo" to null,
                                    "phoneNumber" to null
                                )
                            // add to users collection
                            firestore.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    finish()  // End this activity
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure
                                    Toast.makeText(this, "Firestore Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }

                            }
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

            }
        }
    }
}