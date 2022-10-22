package com.example.renn.maps

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.renn.R
import com.example.renn.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        database = FirebaseDatabase.getInstance().getReference("Users")
        val userid = FirebaseAuth.getInstance().currentUser!!.uid

        var currentLocation: LatLng

        // Getting the location
        val uidRef = database.child(userid)

        uidRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get snapshot
                val snapshot = task.result
                val userLat = snapshot.child("locationLatitude").getValue(Double::class.java)
                val userLon = snapshot.child("locationLongitude").getValue(Double::class.java)

                currentLocation = LatLng(userLat!!, userLon!!)

                val zoomLevel = 15.0f;
                // Instantiates a new CircleOptions object and defines the center and radius
                val circleOptions = CircleOptions()
                    .center(currentLocation)
                    .radius(200.0) // In meters
                    .strokeWidth(10f)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.argb(65, 0, 0, 150))


                // Get back the mutable Circle
                mMap.addCircle(circleOptions)

                mMap.addMarker(MarkerOptions().position(currentLocation).title("Current location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel))

            } else {
                Log.d("TAG", task.exception!!.message!!) //Don't ignore potential errors!
            }
        }

    }

}
