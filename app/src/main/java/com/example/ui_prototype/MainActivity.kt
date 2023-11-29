package com.example.ui_prototype

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ui_prototype.databinding.ActivityMainBinding
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the Toolbar as the ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar) // Replace with your Toolbar ID
        setSupportActionBar(toolbar)


        val imageButton = ImageButton(this)
        imageButton.setImageResource(R.drawable.baseline_logout_24) // Set your icon here
        imageButton.setBackgroundResource(R.color.blue_main)
        imageButton.setPadding(0, 0, 50, 0)

        imageButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut() // Sign out from Firebase
            val signInIntent = Intent(this, SignInActivity::class.java)
            signInIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(signInIntent)
            finish() // Close the MainActivity
            true
        }

        val params = Toolbar.LayoutParams(
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.END
        toolbar.addView(imageButton, params)

        // Setup the navController from NavHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Define appBarConfiguration with top-level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.home,
                R.id.mapsFragment,
                R.id.camera,
                R.id.profile
            )
        )

        // Setup BottomNavigationView with navController
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)
            true
        }

        // Customize BottomNavigationView colors
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_checked), // Unchecked state
            intArrayOf(android.R.attr.state_checked)    // Checked state
        )
        val colors = intArrayOf(
            Color.WHITE,  // Color for unchecked state
            Color.WHITE   // Color for checked state
        )
        val colorStateList = ColorStateList(states, colors)
        binding.bottomNavigationView.itemIconTintList = colorStateList
        binding.bottomNavigationView.itemTextColor = colorStateList


        // Setup the ActionBar with navController and appBarConfiguration
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    // Override onSupportNavigateUp for proper navigation support with the ActionBar
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
