package com.example.setupformaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.setupformaps.application.Permissions
import com.example.setupformaps.maps.NavigationManager
import com.example.setupformaps.user.Commuter
import com.google.android.datatransport.BuildConfig
import com.google.android.gms.maps.GoogleMap.CameraPerspective
import com.google.android.libraries.navigation.AudioGuidanceSettings
import com.google.android.libraries.navigation.NavigationApi
import com.google.android.libraries.navigation.NavigationFragment
import com.google.android.libraries.navigation.Navigator
import com.google.android.libraries.navigation.RoutingOptions
import com.google.android.libraries.navigation.SimulationOptions
import com.google.android.libraries.navigation.Waypoint


/**
 * An activity that displays a map and a navigation UI, guiding the user from their current location
 * to a single, given destination.
 */
class NavigationActivity : AppCompatActivity() {
    private var mNavigator: Navigator? = null
    private var mNavFragment: NavigationFragment? = null
    private var mRoutingOptions: RoutingOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        if (Permissions.isEnabled(Manifest.permission.ACCESS_FINE_LOCATION)) {
            (user!! as Commuter).navigation.initializeNavigationSdk(this);
        } else {
            Permissions.request(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION, this)
        } //request permissions, initialize in callback
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
                    //initializeNavigationSdk()
                    (user!! as Commuter).navigation.initializeNavigationSdk(this);
                }
            }
        }
    }

    companion object {
        // Set fields for requesting location permission.
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}