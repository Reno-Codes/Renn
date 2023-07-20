package com.example.renn

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.text.style.TypefaceSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEach
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.renn.register_login.LoginActivity
import com.example.renn.utils.auth
import com.example.renn.utils.isUserSignedIn
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivityFragment : AppCompatActivity() {
    private lateinit var navController: NavController

    private lateinit var bottomNavigationView: BottomNavigationView

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_fragment)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Check if user is signed in
        if(isUserSignedIn()){
            Log.d("checkLoggedInTAG", "checkLoggedIn: User logged in")
        }
        else{
            Log.d("checkLoggedInTAG", "checkLoggedIn: User not logged in")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(this,"Please sign in..", Toast.LENGTH_SHORT).show()
            finish()
        }

        class TypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
            override fun updateMeasureState(p: TextPaint) {
                p.typeface = typeface
            }

            override fun updateDrawState(tp: TextPaint) {
                tp.typeface = typeface
            }
        }


        navController = Navigation.findNavController(this, R.id.activity_main_fragment_nav_host_fragment)
        setupWithNavController(bottomNavigationView, navController)


        val typeface = ResourcesCompat.getFont(this, R.font.instagram_sans)
        val typefaceSpan = TypefaceSpan(typeface!!)

        bottomNavigationView.menu.forEach { menuItem ->
            val title = menuItem.title.toString()
            val spannableString = SpannableString(title)
            spannableString.setSpan(typefaceSpan, 0, title.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            menuItem.title = spannableString
        }
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            for (i in 0 until bottomNavigationView.menu.size()) {
                val menuItem = bottomNavigationView.menu.getItem(i)
                val view = bottomNavigationView.findViewById<View>(menuItem.itemId)

                if (menuItem.itemId == item.itemId) {
                    // Increase the size of the selected item
                    view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(300).start()
                    // Change the color of the icon to pink_red
                    menuItem.iconTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.pink_red))
                } else {
                    // Decrease the size of the unselected items
                    view.animate().scaleX(0.7f).scaleY(0.7f).setDuration(300).start()
                    // Change the color of the icon to white
                    menuItem.iconTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                }
            }
            // Change the color of the text
            val colorStateList = ContextCompat.getColorStateList(this, R.drawable.bottom_nav_item_color)
            bottomNavigationView.itemTextColor = colorStateList
            // Perform the navigation action
            NavigationUI.onNavDestinationSelected(item, navController)
        }


    }

}