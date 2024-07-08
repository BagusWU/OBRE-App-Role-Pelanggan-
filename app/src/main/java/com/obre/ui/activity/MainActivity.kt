package com.obre.ui.activity

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.obre.databinding.ActivityMainBinding
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.obre.R
import com.obre.ui.fragment.HistoryFragment
import com.obre.ui.fragment.HomeFragment
import com.obre.ui.fragment.ProfileFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentManager: FragmentManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.background = null

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.menu_home -> openFragement(HomeFragment())
                R.id.menu_history -> openFragement(HistoryFragment())
                R.id.menu_account -> openFragement(ProfileFragment())
            }
            true
        }

        fragmentManager = supportFragmentManager
        openFragement(HomeFragment())

        val fragmentToOpen = intent.getStringExtra("fragment_to_open")
        when (fragmentToOpen) {
            "history" -> {
                binding.bottomNavigation.selectedItemId = R.id.menu_history
            }
            "account" -> {
                binding.bottomNavigation.selectedItemId = R.id.menu_account
            }
            else -> {
                binding.bottomNavigation.selectedItemId = R.id.menu_home
            }
        }
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.menu_home -> {
                openFragement(HomeFragment())
            }
            R.id.menu_history -> {
                openFragement(HistoryFragment())
            }
            R.id.menu_account -> {
                openFragement(ProfileFragment())
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun openFragement(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 1001
        const val REQUEST_CHECK_SETTINGS = 1002
    }



}