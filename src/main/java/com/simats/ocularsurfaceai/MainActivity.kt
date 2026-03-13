package com.simats.ocularsurfaceai

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.simats.ocularsurfaceai.data.SessionManager
import com.simats.ocularsurfaceai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- THEME INITIALIZATION ---
        // val session = SessionManager(this)
        // if (session.isDarkMode()) {
        //     AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        // } else {
        //     AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // }
        // -----------------------------

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Top-level destinations (no back arrow)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.historyFragment, R.id.dashboardFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != navController.currentDestination?.id) {
                when (item.itemId) {
                    R.id.homeFragment -> {
                        navController.navigate(R.id.homeFragment)
                        true
                    }
                    R.id.historyFragment -> {
                        navController.navigate(R.id.historyFragment)
                        true
                    }
                    R.id.dashboardFragment -> {
                        navController.navigate(R.id.dashboardFragment)
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }

        // Fix: bottom nav cut by gesture/nav bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootContainer) { _, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.bottomNav.updatePadding(bottom = sysBars.bottom)
            insets
        }

        // Hide/Show BottomNav and Toolbar based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->

            val hideBottomNavOn = setOf(
                R.id.splashFragment,
                R.id.onboardingFragment,
                R.id.loginFragment,
                R.id.signupFragment,
                R.id.forgotPasswordFragment,
                R.id.cameraFragment,
                R.id.analyzingFragment
            )

            val hideBottomNav = destination.id in hideBottomNavOn
            binding.bottomNav.visibility = if (hideBottomNav) View.GONE else View.VISIBLE
            
            // Sync bottom nav selection
            if (!hideBottomNav) {
                binding.bottomNav.menu.findItem(destination.id)?.isChecked = true
            }

            // Hide toolbar on auth screens for a clean experience
            val hideToolbarOn = setOf(
                R.id.splashFragment,
                R.id.onboardingFragment
            )
            if (destination.id in hideToolbarOn) supportActionBar?.hide() else supportActionBar?.show()

            // Set title correctly from nav graph
            supportActionBar?.title = destination.label
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
