package com.example.renn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivityFragment : AppCompatActivity() {
    private lateinit var navController: NavController

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_fragment)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        navController = Navigation.findNavController(this, R.id.activity_main_fragment_nav_host_fragment)
        setupWithNavController(bottomNavigationView, navController)
    }
}