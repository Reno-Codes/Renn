package com.example.renn.profile

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.renn.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ProfileActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Bindings
    private lateinit var backBtn: ImageView
    private lateinit var ivProfileImage: CircleImageView
    private lateinit var tvAddress: TextView
    private lateinit var updateLocationBtn: MaterialButton

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Find views
        backBtn = findViewById(R.id.backBtn)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvAddress = findViewById(R.id.tvAddress)
        updateLocationBtn = findViewById(R.id.updateLocationBtn)

        // FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Back button
        backBtn.setOnClickListener {
            finish()
        }

        updateLocationBtn.setOnClickListener {
            updateUsersLocation()
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        tvAddress = findViewById(R.id.tvAddress)
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
                    @Suppress("DEPRECATION") val addresses: List<Address> = geocoder
                        .getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)
                            as List<Address>

                    val address: String = addresses[0].getAddressLine(0)
                    tvAddress.text = address
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

    private fun updateMap(googleMap: GoogleMap){
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
                    @Suppress("DEPRECATION") val addresses: List<Address> = geocoder
                        .getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)
                            as List<Address>

                    val address: String = addresses[0].getAddressLine(0)
                    tvAddress.text = address
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
    @SuppressLint("MissingPermission")
    private fun updateUsersLocation() {
        Toast.makeText(this@ProfileActivity, "Updating location...", Toast.LENGTH_SHORT).show()
        database = FirebaseDatabase.getInstance().getReference("Users")
        val userid = FirebaseAuth.getInstance().currentUser!!.uid
        @Suppress("DEPRECATION")
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
            if (location == null)
                Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
            else {

                // Set user's location latitude
                database.child(userid).child("locationLatitude").setValue(location.latitude)
                    .addOnSuccessListener {
                        Log.d(
                            "SettingUserLocation",
                            "SettingUserLocation: Location Latitude saved to database!"
                        )
                        // Set user's location longitude
                        database.child(userid).child("locationLongitude")
                            .setValue(location.longitude)
                            .addOnSuccessListener {
                                Toast.makeText(this@ProfileActivity, "Location updated!", Toast.LENGTH_SHORT).show()
                                mMap.clear()
                                updateMap(mMap)
                                Log.d(
                                    "SettingUserLocation",
                                    "SettingUserLocation: Location Longitude saved to database!"
                                )
                            }.addOnFailureListener {
                                Log.d(
                                    "SettingUserLocation",
                                    "SettingUserLocation: Location Longitude NOT SAVED!"
                                )
                            }.addOnFailureListener {
                                Log.d(
                                    "SettingUserLocation",
                                    "SettingUserLocation: Location Latitude NOT SAVED!"
                                )
                            }
                    }
            }
        }
    }

}
