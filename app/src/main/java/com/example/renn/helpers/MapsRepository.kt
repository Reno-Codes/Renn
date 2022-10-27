package com.example.renn.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.renn.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

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

    // Get user location
    @SuppressLint("MissingPermission")
    fun getUserLocation(context: Context, activity: Activity, fusedLocationClient: FusedLocationProviderClient) : LatLng {
        var userLocation = LatLng(0.0, 0.0)


        @Suppress("DEPRECATION")
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false }).addOnSuccessListener { location: Location? ->
            if (location == null) {
                Toast.makeText(context, "Cannot get location!", Toast.LENGTH_SHORT).show()
            }
            else {
                userLocation = LatLng(location.latitude, location.longitude)
            }
        }.addOnFailureListener {
            Log.d("getUserLocation", "getUserLocation: $it")
        }
        return userLocation
    }
}