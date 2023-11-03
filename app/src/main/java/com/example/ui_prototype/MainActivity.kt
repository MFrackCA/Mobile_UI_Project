package com.example.ui_prototype

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ui_prototype.databinding.ActivityMainBinding
import androidx.appcompat.widget.Toolbar

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

        // Setup the navController from NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Define appBarConfiguration with top-level destinations
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.home,
            R.id.mapsFragment,
            R.id.camera,
            R.id.profile
        ))

        // Setup BottomNavigationView with navController
        binding.bottomNavigationView.setupWithNavController(navController)

        // Setup the ActionBar with navController and appBarConfiguration
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    // Override onSupportNavigateUp for proper navigation support with the ActionBar
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
