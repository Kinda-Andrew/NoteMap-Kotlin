package com.example.setupformaps.maps

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.setupformaps.CONTEXT
import com.example.setupformaps.NavigationActivity
import com.example.setupformaps.R
import com.google.android.datatransport.BuildConfig
import com.google.android.gms.maps.GoogleMap.CameraPerspective
import com.google.android.libraries.navigation.AudioGuidanceSettings
import com.google.android.libraries.navigation.NavigationApi
import com.google.android.libraries.navigation.NavigationFragment
import com.google.android.libraries.navigation.Navigator
import com.google.android.libraries.navigation.RoutingOptions
import com.google.android.libraries.navigation.SimulationOptions
import com.google.android.libraries.navigation.Waypoint

class NavigationManager {
    private var mNavigator: Navigator? = null
    private var mNavFragment: NavigationFragment? = null
    private var mRoutingOptions: RoutingOptions? = null

    /**
     * Starts the Navigation SDK and sets the camera to follow the device's location.
     */
    public fun initializeNavigationSdk(activity : AppCompatActivity) {
        // Get a navigator.
        NavigationApi.getNavigator(
            activity,
            object : NavigationApi.NavigatorListener {
                /** Sets up the navigation UI when the navigator is ready for use.  */

                //suppressing for now. The initializeNavigationSdk() guarantees permissions, but it wants
                //it again here for some reason.

                @SuppressLint("MissingPermission")
                public override fun onNavigatorReady(navigator: Navigator) {
                    displayMessage("Navigator ready.")
                    mNavigator = navigator

                    mNavFragment =
                        activity.supportFragmentManager.findFragmentById(R.id.navigation_fragment) as NavigationFragment?

                    // Set the camera to follow the device location with 'TILTED' driving view.
                    mNavFragment?.getMapAsync { googleMap ->
                        googleMap.followMyLocation(
                            CameraPerspective.TILTED
                        )
                    }

                    // Set the travel mode (DRIVING, WALKING, CYCLING, or TWO_WHEELER).
                    mRoutingOptions = RoutingOptions()
                    mRoutingOptions?.travelMode(RoutingOptions.TravelMode.WALKING)
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
            }
        )
    }

    /**
     * Requests directions from the user's current location to a specific place (provided by the
     * Google Places API).
     */
    public fun calculateRoute(placeId: String?, callback: (Navigator) -> Unit) {
        if (mNavigator == null) return;
        if (placeId == null){
            mNavigator!!.stopGuidance()
            mNavigator!!.clearDestinations()
            return;
        }

        val destination: Waypoint?
        try {
            destination = Waypoint.builder().setPlaceIdString(placeId).build()
        } catch (e: Waypoint.UnsupportedPlaceIdException) {
            displayMessage("Error starting navigation: Place ID is not supported.")
            return
        }

        // Create a future to await the result of the asynchronous navigator task.
        mNavigator?.setDestination(destination, mRoutingOptions) { routeDescription ->
            when (routeDescription.routeStatus) {
                Navigator.RouteStatus.OK -> {
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

                    callback(mNavigator!!); // e.g., start guidance
                }

                Navigator.RouteStatus.NO_ROUTE_FOUND -> displayMessage("Error starting navigation: No route found.")
                Navigator.RouteStatus.NETWORK_ERROR -> displayMessage("Error starting navigation: Network error.")
                Navigator.RouteStatus.ROUTE_CANCELED -> displayMessage("Error starting navigation: Route canceled.")
                Navigator.RouteStatus.ROUTING_TIMEOUT -> displayMessage("Error starting navigation: Routing timed out.")
                else -> displayMessage("Error starting navigation: ${routeDescription.routeStatus}")
            }
        }
    }

    /**
     * Shows a message on screen and in the log. Used when something goes wrong.
     *
     * @param errorMessage The message to display.
     */
    private fun displayMessage(errorMessage: String) {
        Toast.makeText(CONTEXT, errorMessage, Toast.LENGTH_LONG).show()
        Log.d(TAG, errorMessage)
    }

    companion object {
        private val TAG: String = NavigationActivity::class.java.getSimpleName()

        //ChIJm7NJkla3j4AR8vR-HWRxgOo computer history museum
        //ChIJpUEO1irYhVQRuIGaYe40bQQ surrey central
    }
}