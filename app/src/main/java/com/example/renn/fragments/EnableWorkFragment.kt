package com.example.renn.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.renn.R
import com.example.renn.settings.SettingsActivity


/**
 * A simple [Fragment] subclass.
 * Use the [EnableWorkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EnableWorkFragment : Fragment() {

    private lateinit var btnStartWorking: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_enable_work, container, false)

        btnStartWorking = view.findViewById(R.id.btnStartWorking)

        btnStartWorking.setOnClickListener {
            val intent = Intent(context, SettingsActivity::class.java)
            startActivity(intent)
        }
        // Inflate the layout for this fragment
        return view

    }
}