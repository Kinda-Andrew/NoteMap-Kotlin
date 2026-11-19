package com.example.setupformaps.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

//LocationPermissions is responsible for getting location data permission from the user
//triggers a popup providing multiple options
class LocationPermissions {
    companion object {
        public fun isEnabled(context: Context): Boolean{
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED;
        }
        public fun requestPermissions(activity: Activity, requestCode : Int) {
            if (!isEnabled(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode
                )
            }
        }
    }
}