package com.example.renn.profile

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import com.example.renn.R
import com.example.renn.helpers.FirebaseRepository
import com.example.renn.helpers.MapsRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class ProfileActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val mapsRepository = MapsRepository()

    // Firebase authentication/database
    private val firebase = FirebaseRepository()

    private val usersRef = firebase.dbRef("Users")

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

        val userid = firebase.currentUserUid()

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
            if (etRadius.text.isNotEmpty()){
                if (etRadius.text.toString().toDouble() < 0.5){
                    etRadius.text.clear()
                    etRadius.hint = "min 0.5"
                }
                else if (etRadius.text.toString().toDouble() > 1000){
                    etRadius.text.clear()
                    etRadius.hint = "max 1000"
                }
                else{
                    // Set Users Location to db and update map
                    mapsRepository.getSetUserLocationAndUpdateMap(this, userid!!, usersRef, fusedLocationClient, mMap, tvAddress, tvRadius, etRadius)
                }
            }
            // Set Users Location to db and update map
            mapsRepository.getSetUserLocationAndUpdateMap(this, userid!!, usersRef, fusedLocationClient, mMap, tvAddress, tvRadius, etRadius)
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
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
                val userLat = snapshot.child("userLocation").child("latitude").getValue(Double::class.java)
                val userLon = snapshot.child("userLocation").child("longitude").getValue(Double::class.java)
                var userCircleRadius = snapshot.child("userCircleRadius").getValue(Double::class.java)
                if (userCircleRadius!! < 0.5) {
                    userCircleRadius = 0.5
                    uidRef.child("userCircleRadius").setValue(userCircleRadius).addOnCompleteListener {
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

                // Default zoom level
                val zoomLevel = 14.5f
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
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel))



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

                    override fun afterTextChanged(s: Editable) {

                        // Check if radius editText is empty
                        if (etRadius.text.isNotEmpty()){

                            // Check if radius editText is greater then minimum radius 0.5
                            if (etRadius.text.toString().toDouble() > 0.4){
                                val radiusToDouble = etRadius.text.toString().trim().toDouble()
                                // Round to 2 decimals
                                val roundRadius = BigDecimal(radiusToDouble).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                                tvRadius.text = convertMetersKm(roundRadius.toString())

                                circleOptionsNew.center(currentLocation)
                                    .radius(etRadius.text.toString().toDouble() * 1000)
                                    .strokeWidth(10f)
                                    .strokeColor(Color.argb(70,4, 194, 83))
                                    .fillColor(Color.argb(60,4, 194, 83))
                                mMap.clear()
                                mMap.addCircle(circleOptionsNew)
                                mMap.addMarker(MarkerOptions().position(currentLocation).title(getAddressInfo()))
                            }
                        }
                        else{
                            val radiusToDouble = userCircleRadius
                            // Round to 2 decimals
                            val roundRadius = BigDecimal(radiusToDouble).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                            tvRadius.text = convertMetersKm(roundRadius.toString())
                            mMap.clear()
                            mMap.addCircle(circleOptions)
                            mMap.addMarker(MarkerOptions().position(currentLocation).title(getAddressInfo()))
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
