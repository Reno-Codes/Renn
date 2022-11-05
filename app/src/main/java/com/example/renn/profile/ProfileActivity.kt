package com.example.renn.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.renn.AddressDetailsActivity
import com.example.renn.R
import com.example.renn.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView
import java.math.BigDecimal
import java.math.RoundingMode

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
    private lateinit var tvYourAddress: TextView
    private lateinit var tvCurrentAddress: TextView
    private var currentRadius: Double = 0.0
    private lateinit var locationPin: ImageView
    private lateinit var dialogAbovePin: RelativeLayout
    private lateinit var tvUseThisPoint: TextView

//    private lateinit var seek: SeekBar

    private lateinit var cameraCurrentLocation: LatLng

    @SuppressLint("MissingPermission", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        locationPin = findViewById(R.id.locationPin)
        dialogAbovePin = findViewById(R.id.dialogAbovePin)
        tvUseThisPoint = findViewById(R.id.tvUseThisPoint)


        // Find views
        backBtn = findViewById(R.id.backBtn)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvAddress = findViewById(R.id.tvAddress)
        updateLocationBtn = findViewById(R.id.updateLocationBtn)
        tvRadius = findViewById(R.id.tvRadius)
        etRadius = findViewById(R.id.etRadius)
        tvYourAddress = findViewById(R.id.tvYourAddress)
        tvYourAddress.paintFlags = tvYourAddress.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tvCurrentAddress = findViewById(R.id.tvCurrentAddress)


        // Get radius

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



        tvUseThisPoint.setOnClickListener {
            val intent = Intent(this, AddressDetailsActivity::class.java)
            val bundle = Bundle()
            bundle.putDouble("Latitude", cameraCurrentLocation.latitude)
            bundle.putDouble("Longitude", cameraCurrentLocation.longitude)
            intent.putExtras(bundle)
            startActivity(intent)
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
                        updateLocationBasedOnPin(cameraCurrentLocation, mMap, tvRadius, etRadius, updateLocationBtn)
                        tvCurrentAddress.text = getAddressInfo(this, cameraCurrentLocation)
                        currentRadius = etRadius.text.toString().toDouble()
                    }
                }
                // Set Users Location to db and update map
                updateLocationBasedOnPin(cameraCurrentLocation, mMap, tvRadius, etRadius, updateLocationBtn)
                tvCurrentAddress.text = getAddressInfo(this, cameraCurrentLocation)
            }
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }



    @SuppressLint("MissingPermission", "SetTextI18n", "UseCompatLoadingForDrawables")
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

        currentUserCircleRadiusRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val radiusSnapshot = task.result
                currentRadius = radiusSnapshot.getValue(Double::class.java)!!
            }
        }

        // Current user location ref
        val currentUserLocationRef = database
            .child("Users")
            .child(auth.currentUser!!.uid).
            child("userLocation")



        tvAddress = findViewById(R.id.tvAddress)
        mMap = googleMap

        // Enable my location button
        mMap.isMyLocationEnabled = true

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


                tvCurrentAddress.text = getAddressInfo(this, currentLocation)
                //tvAddress.text = getAddressInfo(this, currentLocation)


                // Get back the mutable Circle
                mMap.addCircle(circleOptions)
                // Add Marker
                //mMap.addMarker(MarkerOptions().position(currentLocation).title(getAddressInfo(this, currentLocation)))
                // Move camera to user's location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, getZoomLevel(userCircleRadius)), 1000, null)


                // On Camera Move
                mMap.setOnCameraMoveListener {
                    //CustomDialogClass(this).dismiss()
                    dialogAbovePin.visibility = View.INVISIBLE
                    mMap.clear()
                }


                // On Camera Idle
                mMap.setOnCameraIdleListener {
                    val cameraLoc = mMap.cameraPosition.target
                    cameraCurrentLocation = cameraLoc
                    dialogAbovePin.visibility = View.VISIBLE

                    // Check if radius editText is empty
                    if (etRadius.text.isNotEmpty()) {
                        circleOptions.center(cameraLoc)
                            .radius(etRadius.text.toString().toDouble() * 1000)
                            .strokeWidth(10f)
                            .strokeColor(Color.argb(60,4, 83, 194))
                            .fillColor(Color.argb(50,4, 83, 194))
                        mMap.clear()
                        mMap.addCircle(circleOptions)
                    }

                    else{
                        circleOptions.center(cameraLoc)
                            .radius(currentRadius * 1000)
                            .strokeWidth(10f)
                            .strokeColor(Color.argb(60, 4, 83, 194))
                            .fillColor(Color.argb(50, 4, 83, 194))
                        mMap.clear()
                        mMap.addCircle(circleOptions)
                    }
                }
//                mMap.setOnCameraIdleListener {
//                    GlobalScope.async(Dispatchers.IO) {
//                        val addressResult = async { getAddressInfoo(this@ProfileActivity,cameraLoc) }
//                        tvAddress.text = addressResult.await()
//                    }
//                    //tvAddress.text = getAddressInfo(this, cameraLoc)




                // Update Circle
                etRadius.addTextChangedListener(object : TextWatcher {

                    // On radius text changed
                    override fun afterTextChanged(s: Editable) {

                        if (etRadius.text.isNotEmpty()) {
                            // Check if radius editText is greater then minimum radius 0.5
                            if (correctInputRadius(etRadius, tvRadius, updateLocationBtn)) {
                                // Round to 2 decimals
                                val roundRadius =
                                    BigDecimal(etRadius.text.toString().trim().toDouble()).setScale(
                                        2,
                                        RoundingMode.HALF_EVEN
                                    ).toDouble()
                                tvRadius.text = tvRadiusConverter(roundRadius.toString())
                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        cameraCurrentLocation,
                                        getZoomLevel(roundRadius)
                                    ), 1000, null
                                )
                            }
                        }

                        else {
                            tvRadius.setTextColor(Color.parseColor("#353531"))
                            updateLocationBtn.isEnabled = true
                            tvRadius.text = tvRadiusConverter(currentRadius.toString())
                            // Round to 2 decimals
                            val roundRadius =
                                BigDecimal(currentRadius).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                            tvRadius.text = tvRadiusConverter(roundRadius.toString())
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraCurrentLocation, getZoomLevel(currentRadius)), 1000, null
                            )
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
