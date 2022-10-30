package com.example.renn.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.text.set
import com.example.renn.R
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
import com.google.maps.android.SphericalUtil
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


// Get user location and save it to User's table in db
@SuppressLint("MissingPermission")
fun updateCurrentUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient){
    // Current user location table
    val currentUserLocationRef = database
        .child("Users")
        .child(auth.currentUser!!.uid)
        .child("userLocation")

    @Suppress("DEPRECATION")
    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
        override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
        override fun isCancellationRequested() = false }).addOnSuccessListener { location: Location? ->
        if (location == null)
            Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
        else {
            val userLoc = LatLng(location.latitude, location.longitude)

            currentUserLocationRef.setValue(userLoc).addOnSuccessListener {
                Log.d("SettingUserLocation", "SettingUserLocation: Location Latitude saved to database!")
            }.addOnFailureListener {
                Log.d("SettingUserLocation", "SettingUserLocation: Location Latitude NOT SAVED!")
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
    etRadius: EditText
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
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLng(currentLocation)
                        )


                        Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()

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
fun correctInputRadius(etRadius: EditText, tvRadius: TextView, updateLocationBtn: MaterialButton): Boolean{
    var isCorrectRadius = true
    if (etRadius.text.toString().toDouble() < 0.5){
        etRadius.setTextColor(Color.RED)
        tvRadius.setTextColor(Color.RED)
        tvRadius.text = "Minimum radius is 0.5 km"
        updateLocationBtn.isEnabled = false
        isCorrectRadius = false
    }
    else if (etRadius.text.toString().toDouble() > 1000){
        etRadius.setTextColor(Color.RED)
        tvRadius.setTextColor(Color.RED)
        tvRadius.text = "Maximum radius is 1000 km"
        updateLocationBtn.isEnabled = false
        isCorrectRadius = false
    }
    else{
        etRadius.setTextColor(Color.parseColor("#353531"))
        tvRadius.setTextColor(Color.parseColor("#353531"))
        updateLocationBtn.isEnabled = true
    }
    return isCorrectRadius
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