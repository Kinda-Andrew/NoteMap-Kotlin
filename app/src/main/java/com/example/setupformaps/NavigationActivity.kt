package com.example.setupformaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    private var mLocationPermissionGranted = false

    private var destinationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        //set destinationId.
        destinationId = intent.getStringExtra("destinationId").toString()

        // Initialize the Navigation SDK.
        initializeNavigationSdk()
    }

    /**
     * Starts the Navigation SDK and sets the camera to follow the device's location. Calls the
     * navigateToPlace() method when the navigator is ready.
     */
    private fun initializeNavigationSdk() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }

        if (!mLocationPermissionGranted) {
            displayMessage(
                "Error loading Navigation SDK: " + "The user has not granted location permission."
            )
            return
        }

        // Get a navigator.
        NavigationApi.getNavigator(
            this,
            object : NavigationApi.NavigatorListener {
                /** Sets up the navigation UI when the navigator is ready for use.  */

                //suppressing for now. The initializeNavigationSdk() guarantees permissions, but it wants
                //it again here for some reason.
                @SuppressLint("MissingPermission")
                public override fun onNavigatorReady(navigator: Navigator) {
                    displayMessage("Navigator ready.")
                    mNavigator = navigator
                    mNavFragment =
                        supportFragmentManager.findFragmentById(R.id.navigation_fragment) as NavigationFragment?

                    // Set the camera to follow the device location with 'TILTED' driving view.
                    mNavFragment?.getMapAsync { googleMap ->
                        googleMap.followMyLocation(
                            CameraPerspective.TILTED
                        )
                    }

                    // Set the travel mode (DRIVING, WALKING, CYCLING, or TWO_WHEELER).
                    mRoutingOptions = RoutingOptions()
                    mRoutingOptions?.travelMode(RoutingOptions.TravelMode.WALKING)

                    // Navigate to a place, specified by Place ID.
                    navigateToPlace(destinationId, mRoutingOptions)
                }

                /**
                 * Handles errors from the Navigation SDK.
                 *
                 * @param errorCode The error code returned by the navigator.
                 */
                public override fun onError(@NavigationApi.ErrorCode errorCode: Int) {
                    when (errorCode) {
                        NavigationApi.ErrorCode.NOT_AUTHORIZED -> displayMessage(
                            "Error loading Navigation SDK: Your API key is "
                                    + "invalid or not authorized to use the Navigation SDK."
                        )

                        NavigationApi.ErrorCode.TERMS_NOT_ACCEPTED -> displayMessage(
                            "Error loading Navigation SDK: User did not accept "
                                    + "the Navigation Terms of Use."
                        )

                        NavigationApi.ErrorCode.NETWORK_ERROR -> displayMessage("Error loading Navigation SDK: Network error.")
                        NavigationApi.ErrorCode.LOCATION_PERMISSION_MISSING -> displayMessage(
                            "Error loading Navigation SDK: Location permission " + "is missing."
                        )

                        else -> displayMessage("Error loading Navigation SDK: $errorCode")
                    }
                }
            })
    }

    /**
     * Requests directions from the user's current location to a specific place (provided by the
     * Google Places API).
     */
    private fun navigateToPlace(placeId: String?, travelMode: RoutingOptions?) {
        val destination: Waypoint?
        try {
            destination = Waypoint.builder().setPlaceIdString(placeId).build()
        } catch (e: Waypoint.UnsupportedPlaceIdException) {
            displayMessage("Error starting navigation: Place ID is not supported.")
            return
        }

        // Create a future to await the result of the asynchronous navigator task.
        mNavigator?.setDestination(destination, travelMode) { routeDescription ->
            when (routeDescription.routeStatus) {
                Navigator.RouteStatus.OK -> {
                    // Hide the toolbar to maximize the navigation UI.
                    actionBar?.hide()

                    // Enable voice audio guidance (through the device speaker).
                    val audioSettings = AudioGuidanceSettings.builder()
                        .setGuidanceMode(
                            AudioGuidanceSettings.GuidanceMode.VOICE_ALERTS_AND_GUIDANCE
                        )
                        .build()
                    mNavigator?.setAudioGuidanceSettings(audioSettings)

                    // Simulate vehicle progress along the route for demo/debug builds.
                    if (BuildConfig.DEBUG) {
                        mNavigator?.simulator?.simulateLocationsAlongExistingRoute(
                            SimulationOptions().speedMultiplier(5f)
                        )
                    }

                    // Start turn-by-turn guidance along the current route.
                    mNavigator!!.startGuidance()
                }

                Navigator.RouteStatus.NO_ROUTE_FOUND -> displayMessage("Error starting navigation: No route found.")
                Navigator.RouteStatus.NETWORK_ERROR -> displayMessage("Error starting navigation: Network error.")
                Navigator.RouteStatus.ROUTE_CANCELED -> displayMessage("Error starting navigation: Route canceled.")
                Navigator.RouteStatus.ROUTING_TIMEOUT -> displayMessage("Error starting navigation: Routing timed out.")
                else -> displayMessage("Error starting navigation: ${routeDescription.routeStatus}")
            }
        }
    }

    /** Handles the result of the request for location permissions.  */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is canceled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    /**
     * Shows a message on screen and in the log. Used when something goes wrong.
     *
     * @param errorMessage The message to display.
     */
    private fun displayMessage(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        Log.d(TAG, errorMessage)
    }

    companion object {
        private val TAG: String = NavigationActivity::class.java.getSimpleName()

        // Set fields for requesting location permission.
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}