package com.example.renn.profile

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.renn.R
import com.example.renn.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
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

        // Back button
        backBtn.setOnClickListener {
            finish()
        }


//        seek = findViewById(R.id.radiusSeekBar)
//        seek.setOnSeekBarChangeListener(object :
//            SeekBar.OnSeekBarChangeListener {
//            @SuppressLint("SetTextI18n")
//            override fun onProgressChanged(seek: SeekBar,
//                                           progress: Int, fromUser: Boolean) {
//                tvRadius.text = "${seek.progress} km"
//            }
//
//            override fun onStartTrackingTouch(seek: SeekBar) {
//                tvRadius.text = "${seek.progress} km"
//            }
//
//            override fun onStopTrackingTouch(seek: SeekBar) {
//                tvRadius.text = "${seek.progress} km"
//            }
//        })

        /* XML */
//        <SeekBar
//        android:id="@+id/radiusSeekBar"
//        android:layout_width="match_parent"
//        android:layout_height="0dp"
//        android:layout_weight="1"
//        android:min="0"
//        android:max="1000"/>


        updateLocationBtn.setOnClickListener {
            if (!checkPermission(this)) {
                showDialogAndGetPermission(this, this@ProfileActivity)
            }
            else{
                if (etRadius.text.isNotEmpty()){
                    if (!correctInputRadius(etRadius, tvRadius, updateLocationBtn)){
                        return@setOnClickListener
                    }
                    else{
                        // Set Users Location to db and update map
                        updateCurrentUserLocationAndUpdateMap(this, fusedLocationClient, mMap, tvAddress, tvRadius, etRadius)
                    }
                }
                // Set Users Location to db and update map
                updateCurrentUserLocationAndUpdateMap(this, fusedLocationClient, mMap, tvAddress, tvRadius, etRadius)
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


                // Get details about coordinates
                fun getAddressInfo(): String{
                    val geocoder = Geocoder(this, Locale.getDefault())
                    @Suppress("DEPRECATION") val addresses: List<Address> = geocoder
                        .getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)
                            as List<Address>

                    var address = "No address for this location"
                    /*val city: String = addresses[0].locality
                    val state: String = addresses[0].adminArea
                    val country: String = addresses[0].countryName
                    val postalCode: String = addresses[0].postalCode
                    val knownName: String = addresses[0].featureName*/

                    if (addresses.isEmpty()){
                        tvAddress.text = address
                    }
                    else{
                        address = addresses[0].getAddressLine(0)
                        tvAddress.text = address
                    }

                    return address
                }

                // Get back the mutable Circle
                mMap.addCircle(circleOptions)
                // Add Marker
                mMap.addMarker(MarkerOptions().position(currentLocation).title(getAddressInfo()))
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

                        // Check if radius editText is empty
                        if (etRadius.text.isNotEmpty()){

                            // Check if radius editText is greater then minimum radius 0.5
                            if (correctInputRadius(etRadius, tvRadius, updateLocationBtn)){
                                // Round to 2 decimals
                                val roundRadius = BigDecimal(etRadius.text.toString().trim().toDouble()).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                                tvRadius.text = convertMetersKm(roundRadius.toString())


                                circleOptionsNew.center(currentLocation)
                                    .radius(etRadius.text.toString().toDouble() * 1000)
                                    .strokeWidth(10f)
                                    .strokeColor(Color.argb(70,4, 194, 83))
                                    .fillColor(Color.argb(60,4, 194, 83))
                                mMap.clear()
                                mMap.addCircle(circleOptionsNew)
                                mMap.addMarker(MarkerOptions().position(currentLocation).title(getAddressInfo()))
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, getZoomLevel(etRadius.text.toString().toDouble())), 1000, null)
                            }
                        }
                        else{
                            tvRadius.setTextColor(Color.parseColor("#353531"))
                            updateLocationBtn.isEnabled = true
                            // Round to 2 decimals
                            val roundRadius = BigDecimal(userCircleRadius).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                            tvRadius.text = convertMetersKm(roundRadius.toString())
                            mMap.clear()
                            mMap.addCircle(circleOptions)
                            mMap.addMarker(MarkerOptions().position(currentLocation).title(getAddressInfo()))
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, getZoomLevel(userCircleRadius)), 1000, null)
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
