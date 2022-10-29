package com.example.renn.categories

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.renn.R

class CategoryActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        backBtn = findViewById(R.id.backBtn)

        backBtn.setOnClickListener{
            finish()
        }
    }
}