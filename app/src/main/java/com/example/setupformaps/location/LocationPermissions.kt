package com.example.setupformaps.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LocationPermissions {
    companion object {
        public fun requestPermissions(activity: Activity, requestCode : Int) : Boolean {
            // 1. Check if permissions are granted, if so, enable the My Location layer
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
            ) return true;

            // 2. Otherwise, request permission
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                requestCode,
            )
            return false;
        }
    }
}