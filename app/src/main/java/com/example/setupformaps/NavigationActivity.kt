package com.example.setupformaps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.setupformaps.application.Permissions
import com.example.setupformaps.user.Commuter

/**
 * An activity that displays a map and a navigation UI, guiding the user from their current location
 * to a single, given destination.
 */
class NavigationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        if (Permissions.isEnabled(Manifest.permission.ACCESS_FINE_LOCATION)) {
            (user!! as Commuter).navigator.initializeNavigationSdk(this);
        } else {
            Permissions.request(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION, this)
        } //request permissions, then initialize in callback if granted
    }

    /** Handles the result of the request for location permissions.  */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is canceled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    (user!! as Commuter).navigator.initializeNavigationSdk(this);
                }
            }
        }
    }

    companion object {
        // Set fields for requesting location permission.
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}