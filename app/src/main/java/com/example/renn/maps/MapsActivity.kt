package com.example.renn.maps

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.renn.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

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


        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(this, R.raw.google_style)
        )


        // Getting the location
        val uidRef = database.child(userid)

        uidRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get user's location LatLng from db
                val snapshot = task.result
                val userLat = snapshot.child("locationLatitude").getValue(Double::class.java)
                val userLon = snapshot.child("locationLongitude").getValue(Double::class.java)

                currentLocation = LatLng(userLat!!, userLon!!)

                // Default zoom level
                val zoomLevel = 14.5f
                // Instantiates a new CircleOptions object and defines the center, radius and attrs
                val circleOptions = CircleOptions()
                    .center(currentLocation)
                    .radius(1000.0) // In meters
                    .strokeWidth(10f)
                    .strokeColor(Color.argb(60,4, 83, 194))
                    .fillColor(Color.argb(50,4, 83, 194))


                // Get details about coordinates
                fun getAddressInfo(): String{
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses: List<Address> = geocoder
                        .getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)
                            as List<Address>

                    val address: String = addresses[0].getAddressLine(0)
                    /*val city: String = addresses[0].locality
                    val state: String = addresses[0].adminArea
                    val country: String = addresses[0].countryName
                    val postalCode: String = addresses[0].postalCode
                    val knownName: String = addresses[0].featureName*/

                    return address
                }

                // Get back the mutable Circle
                mMap.addCircle(circleOptions)
                // Add Marker
                mMap.addMarker(MarkerOptions().position(currentLocation).title(getAddressInfo()))
                // Move camera to user's location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel))

            } else {
                Log.d("TAG", task.exception!!.message!!) //Don't ignore potential errors!
            }
        }

    }

}
