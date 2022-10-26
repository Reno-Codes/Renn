package com.example.renn.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

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
    fun getPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            101
        )
    }


    // Show Permission alert dialog
    fun showDialogAndGetPermission(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)

        // set message of alert dialog
        dialogBuilder.setMessage("To update your location, please allow location permission!")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Ok") { _, _ ->
                getPermission(context)
            }

        // create dialog box
        val alert = dialogBuilder.create()
        alert.setTitle("Location permission required!")
        alert.show()
    }
}