package com.example.renn.profile

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.renn.AddressDetailsActivity
import com.example.renn.MainActivityFragment
import com.example.renn.R
import com.example.renn.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import de.hdodenhof.circleimageview.CircleImageView
import java.math.BigDecimal
import java.math.RoundingMode


class ProfileActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Animation
    private lateinit var flLottieAnimation: FrameLayout
    private lateinit var animationView: LottieAnimationView


    // Bindings
    private lateinit var backBtn: ImageView
    private lateinit var ivProfileImage: CircleImageView
    private lateinit var tvAddress: TextView
    private lateinit var updateLocationBtn: MaterialButton
    private lateinit var tvRadius: TextView
    private lateinit var tvCurrentRadius: TextView
    private lateinit var etRadius: TextInputEditText
    private lateinit var etRadiusInputLayout: TextInputLayout
    private lateinit var inputCircleRadius: LinearLayout
    private lateinit var tvYourAddress: TextView
    private lateinit var tvCurrentAddress: TextView
    private lateinit var tvAddressPre: TextView
    private lateinit var locationPin: ImageView
    private lateinit var dialogAbovePin: RelativeLayout
    private lateinit var tvUseThisPoint: TextView
    private lateinit var customMyLocationButton: CircleImageView
    private lateinit var switchMapBtn: CircleImageView

    private var currentRadius: Double = 0.0


    // Data from result
    private lateinit var streetName: String
    private lateinit var houseNumber: String
    private lateinit var postalCode: String
    private lateinit var city: String
    private lateinit var country: String
    private lateinit var fullAddress: String
    private lateinit var coordinates: LatLng

//    private lateinit var seek: SeekBar

    private lateinit var cameraCurrentLocation: LatLng

    @SuppressLint("MissingPermission", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        locationPin = findViewById(R.id.locationPin)
        dialogAbovePin = findViewById(R.id.dialogAbovePin)
        tvUseThisPoint = findViewById(R.id.tvUseThisPoint)
        customMyLocationButton = findViewById(R.id.customMyLocationButton)
        switchMapBtn = findViewById(R.id.switchMapBtn)

        flLottieAnimation = findViewById(R.id.flLottieAnimation)
        animationView = findViewById(R.id.animationView)


        // Find views
        backBtn = findViewById(R.id.backBtn)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvAddress = findViewById(R.id.tvAddress)
        tvAddressPre = findViewById(R.id.tvAddressPre)
        updateLocationBtn = findViewById(R.id.updateLocationBtn)
        updateLocationBtn.isEnabled = false

        tvCurrentRadius = findViewById(R.id.tvCurrentRadius)
        tvRadius = findViewById(R.id.tvRadius)
        etRadius = findViewById(R.id.etRadius)
        etRadiusInputLayout = findViewById(R.id.etRadiusInputLayout)
        inputCircleRadius = findViewById(R.id.inputCircleRadius)
        tvYourAddress = findViewById(R.id.tvYourAddress)
        tvYourAddress.paintFlags = tvYourAddress.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tvCurrentAddress = findViewById(R.id.tvCurrentAddress)


        // Loading activity animation
        playLoadingAnimation(flLottieAnimation, animationView)



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
            intentLauncher.launch(Intent(intent))

        }



        updateLocationBtn.setOnClickListener {
            if (!checkPermission(this)) {
                showDialogAndGetPermission(this, this@ProfileActivity)
            }
            else if (!isLocationEnabled()){
                Toast.makeText(this,"Location is disabled. please enable it!", Toast.LENGTH_SHORT).show()
            }
            else{
                if (etRadius.text!!.isNotEmpty()){
                    if (!correctInputRadius(etRadius, tvRadius, updateLocationBtn, etRadiusInputLayout)){
                        return@setOnClickListener
                    }
                    else{
                        // Set Users Location to db and update map
                        updateLocationBasedOnPin(mMap, tvCurrentRadius, etRadius, updateLocationBtn,
                            tvRadius, tvAddressPre, tvAddress, inputCircleRadius,
                            streetName, houseNumber, postalCode, city, country, fullAddress, coordinates, flLottieAnimation, animationView)

                        tvCurrentAddress.text = fullAddress
                        tvCurrentRadius.text = etRadius.text.toString()
                        currentRadius = etRadius.text.toString().toDouble()
                    }
                }
                // Set Users Location to db and update map
                updateLocationBasedOnPin(mMap, tvCurrentRadius, etRadius, updateLocationBtn,
                    tvRadius, tvAddressPre, tvAddress, inputCircleRadius,
                    streetName, houseNumber, postalCode, city, country, fullAddress, coordinates, flLottieAnimation, animationView)
                tvCurrentAddress.text = fullAddress
            }

        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)




        // On Custom Location Button click
        customMyLocationButton.setOnClickListener {

            //Acquire a reference to the system Location Manager
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            //Acquire the user's location
            val selfLocation: Location? = locationManager
                .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            //Move the map to the user's location
            val selfLoc = LatLng(selfLocation!!.latitude, selfLocation.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selfLoc, getZoomLevel(currentRadius)), 500, null)
        }
    }



    @SuppressLint("MissingPermission", "SetTextI18n", "UseCompatLoadingForDrawables",
        "ResourceType"
    )
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

        switchMapBtn.setOnClickListener {
            if (mMap.mapType == GoogleMap.MAP_TYPE_SATELLITE){
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            else{
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
        }

        mMap = googleMap


        // Enable my location button
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false

        //val mapView = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).view
        //val btnMyLocation = (mapView!!.findViewById<View>(1).parent as View).findViewById<View>(2)

        var currentLocation: LatLng

        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(this, R.raw.google_style)
        )



        currentUserIdRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get user's location LatLng from db
                val snapshot = task.result
                val userLat = snapshot.child("Location_details").child("userLocation").child("latitude").getValue(Double::class.java)
                val userLon = snapshot.child("Location_details").child("userLocation").child("longitude").getValue(Double::class.java)
                val userFullAddress = snapshot.child("Location_details").child("userFullAddress").getValue(String::class.java)
                var userCircleRadius = snapshot.child("userCircleRadius").getValue(Double::class.java)
                if (userCircleRadius!! < 0.5) {
                    userCircleRadius = 0.5
                    currentUserCircleRadiusRef.setValue(userCircleRadius).addOnCompleteListener {
                        Log.d("Radius 0.5", "Radius 0.5: Minimum radius set to user's db ")
                    }
                }
                else if (userCircleRadius < 1.0){
                    tvCurrentRadius.text = "Radius: ${userCircleRadius * 1000} meters"
                }
                else{
                    tvCurrentRadius.text = "Radius: $userCircleRadius km"
                }

                currentLocation = LatLng(userLat!!, userLon!!)

                // Instantiates a new CircleOptions object and defines the center, radius and attrs
                val circleOptions = CircleOptions()
                    .center(currentLocation)
                    .radius(userCircleRadius * 1000)
                    .strokeWidth(10f)
                    .strokeColor(Color.argb(60,4, 83, 194))
                    .fillColor(Color.argb(50,4, 83, 194))


                tvCurrentAddress.text = userFullAddress


                // Get back the mutable Circle
                mMap.addCircle(circleOptions)
                // Add Marker
                //mMap.addMarker(MarkerOptions().position(currentLocation).title(getAddressInfo(this, currentLocation)))
                // Move camera to user's location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, getZoomLevel(userCircleRadius)), 1000, null)

                // Stop animation
                flLottieAnimation.visibility = View.GONE
                animationView.cancelAnimation()


                // On Camera Move
                mMap.setOnCameraMoveListener {
                    dialogAbovePin.visibility = View.INVISIBLE
                    updateLocationBtn.isEnabled = false
                    tvAddressPre.visibility = View.GONE
                    tvAddress.visibility = View.GONE
                    inputCircleRadius.visibility = View.GONE
                    mMap.clear()
                }


                // On Camera Idle
                mMap.setOnCameraIdleListener {
                    val cameraLoc = mMap.cameraPosition.target
                    cameraCurrentLocation = cameraLoc
                    dialogAbovePin.visibility = View.VISIBLE


                    // Check if radius editText is empty
                    if (etRadius.text!!.isNotEmpty()) {
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

                        if (etRadius.text!!.isNotEmpty()) {
                            // Check if radius editText is greater then minimum radius 0.5
                            if (correctInputRadius(etRadius, tvRadius, updateLocationBtn, etRadiusInputLayout)) {
                                // Round to 2 decimals
                                val roundRadius =
                                    BigDecimal(etRadius.text.toString().trim().toDouble()).setScale(
                                        2,
                                        RoundingMode.HALF_EVEN
                                    ).toDouble()
                                tvRadius.visibility = View.VISIBLE
                                tvRadius.text = tvRadiusConverter(roundRadius.toString())
                                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraCurrentLocation, getZoomLevel(roundRadius)), 500, null)
                            }
                        }

                        else {
                            tvRadius.setTextColor(Color.parseColor("#353531"))
                            tvRadius.text = tvRadiusConverter(currentRadius.toString())
                            // Round to 2 decimals
                            val roundRadius =
                                BigDecimal(currentRadius).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                            tvRadius.visibility = View.GONE
                            etRadiusInputLayout.hint = "Circle radius (kilometers)"
                            tvRadius.text = tvRadiusConverter(roundRadius.toString())
                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraCurrentLocation, getZoomLevel(currentRadius)), 500, null)
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

    private val intentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val streetNameResult = result.data?.getStringExtra("streetNameKey")
                val houseNumberResult = result.data?.getStringExtra("houseNumberKey")
                val postalCodeResult = result.data?.getStringExtra("postalCodeKey")
                val cityResult = result.data?.getStringExtra("cityKey")
                val countryResult = result.data?.getStringExtra("countryKey")
                val fullAddressResult = result.data?.getStringExtra("fullAddressKey")

                val latLngResult = result.data?.getBundleExtra("latLngKey")
                val currentPinLocationLatLng = LatLng(latLngResult!!.getDouble("latitudeKey"), latLngResult.getDouble("longitudeKey"))

                //mMap.animateCamera(CameraUpdateFactory.newLatLng(currentPinLocationLatLng), 500, null)

                streetName = streetNameResult.toString()
                houseNumber = houseNumberResult.toString()
                postalCode = postalCodeResult.toString()
                city = cityResult.toString()
                country = countryResult.toString()
                fullAddress = fullAddressResult.toString()
                coordinates = currentPinLocationLatLng

                tvAddressPre.visibility = View.VISIBLE
                tvAddress.visibility = View.VISIBLE
                tvAddress.text = fullAddressResult
                inputCircleRadius.visibility = View.VISIBLE
                updateLocationBtn.isEnabled = true

                // TODO: DONE  (No usernames yet) Signup should store all the info from the result (consider adding username field too)
            }
        }
}
