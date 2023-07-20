package com.example.renn.utils

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.airbnb.lottie.LottieAnimationView
import com.example.renn.R
import com.example.renn.User
import com.example.renn.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.ktx.getValue
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.delay
import org.w3c.dom.Text
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.ln
import kotlin.math.sqrt


// Check permission - returns Boolean
@SuppressLint("MissingPermission")
@Suppress("RedundantIf")
fun checkPermission(context: Context): Boolean {
    return if (ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        false
    } else {
        true
    }
}


// Get permission
@SuppressLint("MissingPermission")
fun getPermission(activity: Activity) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ),
        101
    )
}


// Show Permission alert dialog and ask for permission
fun showDialogAndGetPermission(context: Context, activity: Activity) {
    val dialogBuilder = AlertDialog.Builder(context)

    dialogBuilder.setMessage("Location permission is required, please allow location permission!")
        .setCancelable(false)
        .setPositiveButton("Ok") { _, _ ->
            getPermission(activity)
        }

    val alert = dialogBuilder.create()
    alert.setTitle("Location required!")
    alert.show()
}



fun updateLocationBasedOnPin(
    map: GoogleMap,
    tvCurrentRadius: TextView,
    etRadius: EditText,
    updateLocationBtn: MaterialButton,
    tvRadius: TextView,
    tvAddressPre: TextView,
    tvAddress: TextView,
    inputCircleRadius: LinearLayout,

    streetName: String,
    houseNumber: String,
    postalCode: String,
    city: String,
    country: String,
    fullAddress: String,
    coordinates: LatLng,
    flAnimationView: FrameLayout,
    animationView: LottieAnimationView
){

    val user = UserLocation(
        userStreetName = streetName,
        userHouseNumber = houseNumber,
        userPostalCode = postalCode,
        userCity = city,
        userCountry = country,
        userFullAddress = fullAddress
    )

    val currentUserLocationDetailsRef = database
        .child("Users")
        .child(auth.currentUser!!.uid)
        .child("Location_details")

    val currentUserCircleRadiusRef = database
        .child("Users")
        .child(auth.currentUser!!.uid)
        .child("userCircleRadius")


    // Update Location details
    currentUserLocationDetailsRef.setValue(user).addOnSuccessListener {

        playSuccessAnimation(flAnimationView, animationView)

        updateLocationBtn.isEnabled = false
        tvRadius.visibility = View.GONE
        tvAddressPre.visibility = View.GONE
        tvAddress.visibility = View.GONE
        inputCircleRadius.visibility = View.GONE

        // Update Radius
        if (etRadius.text.isNotEmpty()) {
            val radiusToDouble = etRadius.text.toString().trim().toDouble()
            // Round to 2 decimals
            val roundRadius =
                BigDecimal(radiusToDouble).setScale(2, RoundingMode.HALF_EVEN).toDouble()

            currentUserCircleRadiusRef.setValue(roundRadius)
        }

        // Update coordinates
        currentUserLocationDetailsRef.child("userLocation").setValue(coordinates)

        currentUserCircleRadiusRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val radius = snapshot.getValue(Double::class.java)
                tvCurrentRadius.text = tvRadiusConverter(radius.toString())

                animationView.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        Log.e("Animation:", "start")
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        Log.e("Animation:", "end")
                        //Ex: here the layout is removed!

                        if (!animation.isRunning){
                            flAnimationView.visibility= View.GONE
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, getZoomLevel(radius!!)), 1000, null)
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        Log.e("Animation:", "cancel")
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        Log.e("Animation:", "repeat")
                    }

                })
            }
        }
    }.addOnFailureListener {

        playFailedAnimation(flAnimationView, animationView)
    }
}


// Get user location and save it to User's table in db
@SuppressLint("MissingPermission")
fun updateCurrentUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient){

    var streetName = "No street name detected"
    var houseNumber = "No house number detected"
    var postalCode = "No house number detected"
    var city = "No city detected"
    var country = "No country detected"
    var fullAddress: String
    var coordinates: LatLng


    // Current user location table
    val currentUserLocationRef = database
        .child("Users")
        .child(auth.currentUser!!.uid)
        .child("Location_details")

    @Suppress("DEPRECATION")
    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
        override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
        override fun isCancellationRequested() = false }).addOnSuccessListener { location: Location? ->
        if (location == null)
            Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
        else {

            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION") val addresses: List<Address> = geocoder
                .getFromLocation(location.latitude, location.longitude, 1)
                    as List<Address>

            if (addresses.isNotEmpty()){
                streetName = addresses[0].thoroughfare ?: streetName
                houseNumber = addresses[0].subThoroughfare ?: houseNumber
                postalCode = addresses[0].postalCode ?: postalCode
                city = addresses[0].locality ?: city
                country = addresses[0].countryName ?: country
                fullAddress = addresses[0].getAddressLine(0)
                coordinates = LatLng(location.latitude, location.longitude)

                val user = UserLocation(
                    userStreetName = streetName,
                    userHouseNumber = houseNumber,
                    userPostalCode = postalCode,
                    userCity = city,
                    userCountry = country,
                    userFullAddress = fullAddress,
                    userLocation = coordinates
                )

                currentUserLocationRef.setValue(user).addOnSuccessListener {
                    Log.d("SignupUserLocation", "SettingUserLocation: Location saved to database!")
                }.addOnFailureListener {
                    Log.d("SignupUserLocation", "SettingUserLocation: Location NOT SAVED!")
                }
            }
        }
    }
}


// Get user location and save it to User's table in db AND UPDATE MAP
@SuppressLint("MissingPermission", "SetTextI18n")
fun updateCurrentUserLocationAndUpdateMap(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    googleMap: GoogleMap,
    tvAddress: TextView,
    tvRadius: TextView,
    etRadius: EditText,
    updateLocationBtn: MaterialButton
){
    // Current user location table ref
    val currentUserLocationRef = database
        .child("Users")
        .child(auth.currentUser!!.uid)
        .child("userLocation")

    // Current user circle radius table ref
    val currentUserCircleRadiusRef = database
        .child("Users")
        .child(auth.currentUser!!.uid)
        .child("userCircleRadius")

    updateLocationBtn.isEnabled = false
    etRadius.isEnabled = false

    tvAddress.text = "Please wait..."
    tvRadius.text = ""

    @Suppress("DEPRECATION")
    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
        override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
        override fun isCancellationRequested() = false }).addOnSuccessListener { location: Location? ->
        if (location == null)
            Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
        else {
            val userLoc = LatLng(location.latitude, location.longitude)
            currentUserLocationRef.setValue(userLoc).addOnSuccessListener {

                if (etRadius.text.isNotEmpty()){
                    val radiusToDouble = etRadius.text.toString().trim().toDouble()
                    // Round to 2 decimals
                    val roundRadius = BigDecimal(radiusToDouble).setScale(2, RoundingMode.HALF_EVEN).toDouble()

                    currentUserCircleRadiusRef.setValue(roundRadius)
                }

                // Clear map
                googleMap.clear()
                // Update Map
                var currentLocation: LatLng

                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.google_style)
                )

                // Current user ID table ref
                val currentUserIdRef = database
                    .child("Users")
                    .child(auth.currentUser!!.uid)

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

                        else if (userCircleRadius > 1000){
                            userCircleRadius = 1000.0
                            currentUserCircleRadiusRef.setValue(userCircleRadius).addOnCompleteListener {
                                Log.d("Radius 1000", "Radius 1000: Maximum radius (1000 km) set to user's db ")
                            }
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
                            .strokeColor(Color.argb(60, 4, 83, 194))
                            .fillColor(Color.argb(50, 4, 83, 194))


                        // Get details about coordinates
                        fun getAddressInfo(): String {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            @Suppress("DEPRECATION") val addresses: List<Address> = geocoder
                                .getFromLocation(
                                    currentLocation.latitude,
                                    currentLocation.longitude,
                                    1
                                )
                                    as List<Address>

                            var address = "No address for this location"
                            /*val city: String = addresses[0].locality
                            val state: String = addresses[0].adminArea
                            val country: String = addresses[0].countryName
                            val postalCode: String = addresses[0].postalCode
                            val knownName: String = addresses[0].featureName*/

                            if (addresses.isEmpty()) {
                                tvAddress.text = address
                            } else {
                                address = addresses[0].getAddressLine(0)
                                tvAddress.text = address
                            }

                            return address
                        }

                        // Get back the mutable Circle
                        googleMap.addCircle(circleOptions)
                        // Add Marker
                        googleMap.addMarker(
                            MarkerOptions().position(currentLocation).title(getAddressInfo())
                        )
                        // Move camera to user's location
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation), 1000, null)


                        //Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()

                    } else {
                        Log.d(
                            "TAG",
                            task.exception!!.message!!
                        ) //Don't ignore potential errors!
                    }
                }
                Log.d(
                    "SettingUserLocation",
                    "SettingUserLocation: Location Latitude saved to database!"
                )
            }.addOnFailureListener {
                Log.d("SettingUserLocation", "SettingUserLocation: Location Latitude NOT SAVED!")
            }
        }
        updateLocationBtn.isEnabled = true
        etRadius.isEnabled = true
    }
}


// Currently working
fun getZoomLevel(radius: Double): Float {
    val zoomLevel: Float

    val scale: Double = (radius * 1000) / 500

    zoomLevel = (15 - ln(scale) / ln(2.0)).toFloat()

    return zoomLevel
}

// Check is input radius correct (min 0.5 km - max 1000 km)
@SuppressLint("SetTextI18n")
fun correctInputRadius(etRadius: EditText, tvRadius: TextView, updateLocationBtn: MaterialButton, etRadiusInputLayout: TextInputLayout): Boolean{
    var isCorrectRadius = true
    if (etRadius.text.toString().toDouble() < 0.5){
        etRadius.setTextColor(Color.RED)
        tvRadius.setTextColor(Color.RED)
        etRadiusInputLayout.hint = "Circle radius (kilometers) - Minimum 0.5 km"
        updateLocationBtn.isEnabled = false
        isCorrectRadius = false
    }
    else if (etRadius.text.toString().toDouble() > 1000){
        etRadius.setTextColor(Color.RED)
        tvRadius.setTextColor(Color.RED)
        etRadiusInputLayout.hint = "Circle radius (kilometers) - Maximum 1000 km"
        updateLocationBtn.isEnabled = false
        isCorrectRadius = false
    }
    else{
        etRadius.setTextColor(Color.parseColor("#353531"))
        tvRadius.setTextColor(Color.parseColor("#353531"))
        etRadiusInputLayout.hint = "Circle radius (kilometers)"
        updateLocationBtn.isEnabled = true
    }
    return isCorrectRadius
}

// Get full address info
fun getAddressInfo(context: Context, location: LatLng): String{
    val geocoder = Geocoder(context, Locale.getDefault())
    @Suppress("DEPRECATION") val addresses: List<Address> = geocoder
        .getFromLocation(location.latitude, location.longitude, 1)
            as List<Address>

    var address = "No address detected"
    /*val city: String = addresses[0].locality
    val state: String = addresses[0].adminArea
    val country: String = addresses[0].countryName
    val postalCode: String = addresses[0].postalCode
    val knownName: String = addresses[0].featureName*/

    if (addresses.isNotEmpty()){
        address = addresses[0].getAddressLine(0)
    }

    return address
}


// Get street name, city and house number
fun getStreetNameCityAndHouseNumber(context: Context, location: LatLng): Triple<String, String, String>{
    val geocoder = Geocoder(context, Locale.getDefault())
    @Suppress("DEPRECATION") val addresses: List<Address> = geocoder
        .getFromLocation(location.latitude, location.longitude, 1)
            as List<Address>

    var address = ""
    var city = ""
    var houseNumber = ""


    if (addresses.isNotEmpty()){
        address = addresses[0].thoroughfare ?: ""
        city = addresses[0].locality ?: ""
        houseNumber = addresses[0].subThoroughfare ?: ""
    }

    return Triple(address, city, houseNumber)
}

// Get Country and postal code
fun getCountryAndPostalCode(context: Context, location: LatLng): Pair<String, String>{
    val geocoder = Geocoder(context, Locale.getDefault())
    @Suppress("DEPRECATION") val addresses: List<Address> = geocoder
        .getFromLocation(location.latitude, location.longitude, 1)
            as List<Address>

    var country = ""
    var postalCode = ""

    if (addresses.isNotEmpty()){
        country = addresses[0].countryName ?: ""
        postalCode = addresses[0].postalCode ?: ""
    }

    return Pair(country, postalCode)
}


// Get LatLng from given address
@Suppress("DEPRECATION")
fun getLatLngFromAddress(context: Context, address: String): Pair<Boolean, LatLng>{
    val geocoder = Geocoder(context)
    val addresses: List<Address> = geocoder
        .getFromLocationName(address, 1)
            as List<Address>

    var latLng = LatLng(0.0, 0.0)
    var validAddress = false


    if (addresses.isNotEmpty()){
        latLng = LatLng(addresses[0].latitude, addresses[0].longitude)
        validAddress = true
    }

    Log.d("GET_LAT_LNG_FROM_ADDRESS", "getLatLngFromAddress: $latLng, $validAddress")
    return Pair(validAddress, latLng)
}

// Add required asterisk
fun TextInputLayout.markRequiredInRed() {
    hint = buildSpannedString {
        append(hint)
        color(Color.RED) { append(" *") } // Mind the space prefix.
    }
}




suspend fun getAddressInfoo(context: Context, location: LatLng): String{
    delay(100L)
    val geocoder = Geocoder(context, Locale.getDefault())
    @Suppress("DEPRECATION") val addresses: List<Address> = geocoder
        .getFromLocation(location.latitude, location.longitude, 1)
            as List<Address>

    var address = "No address for this location"
    /*val city: String = addresses[0].locality
    val state: String = addresses[0].adminArea
    val country: String = addresses[0].countryName
    val postalCode: String = addresses[0].postalCode
    val knownName: String = addresses[0].featureName*/

    if (addresses.isNotEmpty()){
        address = addresses[0].getAddressLine(0)
    }

    return address
}




// Convert to meters or kilometers to textView Text radius
fun tvRadiusConverter(radius: String): String{
    val radiusString = if (radius.toDouble() < 1.0){
        "Radius: ${radius.toDouble() * 1000} meters"
    }
    else{
        "Radius: ${radius.toDouble()} km"
    }
    return radiusString
}



fun getBounds(map: GoogleMap): List<Double> {
    val bounds: LatLngBounds = map.projection.visibleRegion.latLngBounds
    val boundNeLat = bounds.northeast.latitude
    val boundSwLat = bounds.southwest.latitude
    val boundNeLng = bounds.northeast.longitude
    val boundSwLng = bounds.southwest.longitude

    return listOf(boundNeLat, boundNeLng, boundSwLat, boundSwLng)
}



fun circleBounds(radius: Double, location: LatLng): RectangularBounds{
// optional: to get distance to circle radius, not the edge
    val distance = radius * sqrt(2.0)

    val ne = SphericalUtil.computeOffset(location, distance, 45.0)
    val sw = SphericalUtil.computeOffset(location, distance, 225.0)

    return RectangularBounds.newInstance(ne, sw)
}