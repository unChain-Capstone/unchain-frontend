package com.unchain.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unchain.R
import com.unchain.databinding.ActivityMainBinding
import com.unchain.utils.NavigationUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var lastSelectedItemId = R.id.navigation_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_chat,
                R.id.navigation_settings,
            )
        )

        // Setup navigation with animation based on menu position
        navView.setOnItemSelectedListener { menuItem ->
            // Determine animation direction based on menu position
            val currentIndex = when (lastSelectedItemId) {
                R.id.navigation_home -> 0
                R.id.navigation_chat -> 1
                R.id.navigation_settings -> 2
                else -> 0
            }
            
            val newIndex = when (menuItem.itemId) {
                R.id.navigation_home -> 0
                R.id.navigation_chat -> 1
                R.id.navigation_settings -> 2
                else -> 0
            }

            // Use left animation when moving right (to higher index)
            val navOptions = if (newIndex > currentIndex) {
                NavigationUtils.getSlideLeftAnimation()
            } else {
                NavigationUtils.getSlideRightAnimation()
            }
            
            lastSelectedItemId = menuItem.itemId
            navController.navigate(menuItem.itemId, null, navOptions)
            true
        }

        // Initial setup
        navView.selectedItemId = R.id.navigation_home
    }
}