package com.example.renn.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.renn.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.database.DatabaseReference
import java.util.*

class MapsRepository {


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
    fun getSetUserCurrentLocation(context: Context, userid: String, usersRef: DatabaseReference, fusedLocationClient: FusedLocationProviderClient){
        @Suppress("DEPRECATION")
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false }).addOnSuccessListener { location: Location? ->
            if (location == null)
                Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
            else {
                val userLoc = LatLng(location.latitude, location.longitude)
                usersRef.child(userid).child("userLocation").setValue(userLoc).addOnSuccessListener {
                    Log.d("SettingUserLocation", "SettingUserLocation: Location Latitude saved to database!")
                }.addOnFailureListener {
                    Log.d("SettingUserLocation", "SettingUserLocation: Location Latitude NOT SAVED!")
                }
            }
        }
    }


    // Get user location and save it to User's table in db AND UPDATE MAP
    @SuppressLint("MissingPermission")
    fun getSetUserLocationAndUpdateMap(context: Context, userid: String, usersRef: DatabaseReference, fusedLocationClient: FusedLocationProviderClient, googleMap: GoogleMap, tvAddress: TextView){
        @Suppress("DEPRECATION")
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false }).addOnSuccessListener { location: Location? ->
            if (location == null)
                Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
            else {
                val userLoc = LatLng(location.latitude, location.longitude)
                usersRef.child(userid).child("userLocation").setValue(userLoc).addOnSuccessListener {

                    // Update Map
                    var currentLocation: LatLng

                    googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(context, R.raw.google_style)
                    )


                    // Getting the location
                    val uidRef = usersRef.child(userid)

                    uidRef.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Get user's location LatLng from db
                            val snapshot = task.result
                            val userLat = snapshot.child("userLocation").child("latitude")
                                .getValue(Double::class.java)
                            val userLon = snapshot.child("userLocation").child("longitude")
                                .getValue(Double::class.java)

                            currentLocation = LatLng(userLat!!, userLon!!)

                            // Default zoom level
                            val zoomLevel = 14.5f
                            // Instantiates a new CircleOptions object and defines the center, radius and attrs
                            val circleOptions = CircleOptions()
                                .center(currentLocation)
                                .radius(1000.0) // In meters
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
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLocation,
                                    zoomLevel
                                )
                            )

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
}