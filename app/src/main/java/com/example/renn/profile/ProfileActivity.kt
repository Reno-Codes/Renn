package com.example.renn.profile

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.renn.R
import com.example.renn.utils.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import de.hdodenhof.circleimageview.CircleImageView
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class ProfileActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    // Bindings
    private lateinit var backBtn: ImageView
    private lateinit var ivProfileImage: CircleImageView
    private lateinit var tvAddress: TextView
    private lateinit var updateLocationBtn: MaterialButton
    private lateinit var tvRadius: TextView
    private lateinit var etRadius: EditText
//    private lateinit var seek: SeekBar

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        // Find views
        backBtn = findViewById(R.id.backBtn)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvAddress = findViewById(R.id.tvAddress)
        updateLocationBtn = findViewById(R.id.updateLocationBtn)
        tvRadius = findViewById(R.id.tvRadius)
        etRadius = findViewById(R.id.etRadius)

        // FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check location settings
        fun isLocationEnabled(): Boolean {
            val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }


        // Back button
        backBtn.setOnClickListener {
            finish()
        }




        updateLocationBtn.setOnClickListener {
            if (!checkPermission(this)) {
                showDialogAndGetPermission(this, this@ProfileActivity)
            }
            else if (!isLocationEnabled()){
                Toast.makeText(this,"Location is disabled. please enable!", Toast.LENGTH_SHORT).show()
            }
            else{
                if (etRadius.text.isNotEmpty()){
                    if (!correctInputRadius(etRadius, tvRadius, updateLocationBtn)){
                        return@setOnClickListener
                    }
                    else{
                        // Set Users Location to db and update map
                        updateCurrentUserLocationAndUpdateMap(this, fusedLocationClient, mMap, tvAddress, tvRadius, etRadius, updateLocationBtn)
                    }
                }
                // Set Users Location to db and update map
                updateCurrentUserLocationAndUpdateMap(this, fusedLocationClient, mMap, tvAddress, tvRadius, etRadius, updateLocationBtn)
            }
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onMapReady(googleMap: GoogleMap) {
        // User Id table ref
        val currentUserIdRef = database
            .child("Users")
            .child(auth.currentUser!!.uid)
        // Current user circle radius ref
        val currentUserCircleRadiusRef = database
            .child("Users")
            .child(auth.currentUser!!.uid).
            child("userCircleRadius")

        // Current user location ref
        val currentUserLocationRef = database
            .child("Users")
            .child(auth.currentUser!!.uid).
            child("userLocation")



        tvAddress = findViewById(R.id.tvAddress)
        mMap = googleMap

        var currentLocation: LatLng

        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(this, R.raw.google_style)
        )



        currentUserIdRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get user's location LatLng from db
                val snapshot = task.result
                val userLat = snapshot.child("userLocation").child("latitude").getValue(Double::class.java)
                val userLon = snapshot.child("userLocation").child("longitude").getValue(Double::class.java)
                var userCircleRadius = snapshot.child("userCircleRadius").getValue(Double::class.java)
                if (userCircleRadius!! < 0.5) {
                    userCircleRadius = 0.5
                    currentUserCircleRadiusRef.setValue(userCircleRadius).addOnCompleteListener {
                        Log.d("Radius 0.5", "Radius 0.5: Minimum radius set to user's db ")
                    }
                }
                else if (userCircleRadius < 1.0){
                    tvRadius.text = "Radius: ${userCircleRadius * 1000} meters"
                }
                else{
                    tvRadius.text = "Radius: $userCircleRadius km"
                }

                currentLocation = LatLng(userLat!!, userLon!!)

                // Instantiates a new CircleOptions object and defines the center, radius and attrs
                val circleOptions = CircleOptions()
                    .center(currentLocation)
                    .radius(userCircleRadius * 1000)
                    .strokeWidth(10f)
                    .strokeColor(Color.argb(60,4, 83, 194))
                    .fillColor(Color.argb(50,4, 83, 194))



                tvAddress.text = getAddressInfo(this, currentLocation)


                // Get back the mutable Circle
                mMap.addCircle(circleOptions)
                // Add Marker
                mMap.addMarker(MarkerOptions().position(currentLocation).title(getAddressInfo(this, currentLocation)))
                // Move camera to user's location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, getZoomLevel(userCircleRadius)), 1000, null)



                // Update Circle
                etRadius.addTextChangedListener(object : TextWatcher {
                    // Initialize a new circle
                    var circleOptionsNew = CircleOptions()

                    // Convert to meters or kilometers
                    fun convertMetersKm(radius: String): String{
                        val radiusString = if (radius.toDouble() < 1.0){
                            "Radius: ${radius.toDouble() * 1000} meters"
                        }
                        else{
                            "Radius: ${radius.toDouble()} km"
                        }
                        return radiusString
                    }


                    // On radius text changed
                    override fun afterTextChanged(s: Editable) {

                        currentUserIdRef.get().addOnCompleteListener {
                            val snapshotNew = it.result
                            val userLatNew = snapshotNew.child("userLocation").child("latitude")
                                .getValue(Double::class.java)
                            val userLonNew = snapshotNew.child("userLocation").child("longitude")
                                .getValue(Double::class.java)

                            val userCircleRadiusNew = snapshotNew.child("userCircleRadius").getValue(Double::class.java)
                            val newestLocation = LatLng(userLatNew!!, userLonNew!!)



                            // Check if radius editText is empty
                            if (etRadius.text.isNotEmpty()) {

                                // Check if radius editText is greater then minimum radius 0.5
                                if (correctInputRadius(etRadius, tvRadius, updateLocationBtn)) {
                                    // Round to 2 decimals
                                    val roundRadius = BigDecimal(etRadius.text.toString().trim().toDouble()).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                                    tvRadius.text = convertMetersKm(roundRadius.toString())


                                    circleOptionsNew.center(newestLocation)
                                        .radius(etRadius.text.toString().toDouble() * 1000)
                                        .strokeWidth(10f)
                                        .strokeColor(Color.argb(70, 4, 194, 83))
                                        .fillColor(Color.argb(60, 4, 194, 83))
                                    mMap.clear()
                                    mMap.addCircle(circleOptionsNew)
                                    mMap.addMarker(MarkerOptions().position(newestLocation).title(getAddressInfo(this@ProfileActivity, newestLocation)))
                                    mMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            newestLocation,
                                            getZoomLevel(etRadius.text.toString().toDouble())
                                        ), 1000, null
                                    )
                                }
                            } else {
                                tvRadius.setTextColor(Color.parseColor("#353531"))
                                updateLocationBtn.isEnabled = true
                                // Round to 2 decimals
                                val roundRadius =
                                    BigDecimal(userCircleRadiusNew!!).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                                tvRadius.text = convertMetersKm(roundRadius.toString())


                                circleOptionsNew.center(newestLocation)
                                    .radius(userCircleRadiusNew * 1000)
                                    .strokeWidth(10f)
                                    .strokeColor(Color.argb(70, 4, 194, 83))
                                    .fillColor(Color.argb(60, 4, 194, 83))
                                mMap.clear()
                                mMap.addCircle(circleOptionsNew)
                                mMap.addMarker(MarkerOptions().position(newestLocation)
                                    .title(getAddressInfo(this@ProfileActivity, newestLocation)))
                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        newestLocation,
                                        getZoomLevel(roundRadius)), 1000, null)
                            }
                        }
                    }

                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                        // do some work?
                    }
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        // do some work?
                    }
                })

            } else {
                Log.d("TAG", task.exception!!.message!!) //Don't ignore potential errors!
            }
        }
    }
}
