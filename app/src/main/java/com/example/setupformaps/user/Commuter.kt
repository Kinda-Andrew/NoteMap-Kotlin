package com.example.setupformaps.user

import android.Manifest
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.setupformaps.CONTEXT
import com.example.setupformaps.application.DatabaseManager
import com.example.setupformaps.application.NavigationManager
import com.example.setupformaps.application.Notifications
import com.example.setupformaps.location.Route
import com.example.setupformaps.user
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.navigation.Navigator
import com.google.android.libraries.navigation.RouteSegment
import java.util.PriorityQueue
import org.locationtech.jts.geom.Coordinate
import util.CoordinateMath

class Commuter() : User() {
    @Volatile private var lastLocation : LatLng? = null;
    private var locationSnapshots = PriorityQueue<Location> { o1, o2 -> // sorted oldest to newest
        o1.time.compareTo(o2.time)
    } // Snapshots older than `IDLE_TIMEOUT` should be discarded using `poll()`

    private var destinationId : String = ""

    private var destinationName : String = ""

    private var shareableCode : String = "placeholder";
    @Volatile private var note : String = "";
    private var route : RouteSegment? = null;

    private var client =  LocationServices.getFusedLocationProviderClient(CONTEXT);
    private val database = DatabaseManager(
        shareableCode,
        this::onNoteChanged,
        this::onDestinationChanged,
        null, // The caretaker is not able to change the route. They can do it indirectly by changing `destination` instead.
        null // The caretaker also cannot change `lastLocation`
    );
    public val navigator = NavigationManager();


    override fun getCommuterNote(): String { return note; }
    override fun setCommuterNote(note: String) {
        database.setNote(note); // update database
    }
    private fun onNoteChanged(note: String?){ // update local `note` only when database is updated
        if (note != null) this.note = note;
        else this.note = "";
    }


    override fun getCommuterRoute(): List<LatLng>? {
        if (route == null) return null;
        else return route!!.latLngs;
    }
    private fun setRoute(route: RouteSegment?){
        this.route = route;
        database.setRoute(route);
    }


    override fun setCommuterDestinationId(destinationId: String) {
        database.setDestination(destinationId);
    }

    override fun getCommuterDestinationId(): String { return destinationId; }

    override fun setCommuterDestinationName(destinationName: String) { this.destinationName = destinationName; }

    override fun getCommuterDestinationName(): String { return destinationName; }
    private fun onDestinationChanged(destinationId: String?){
        onRouteEnd();
        if (destinationId != null) {
            this.destinationId = destinationId;
            navigator.calculateRoute(this::onRouteStart);
        }
    }


    /**
     * Immediately returns the commuter's last known location.
     *
     * @return The commuter's last known location
     * */
    override fun getCommuterLocation(): LatLng? {
        return lastLocation;
    }

    /**
     * Asynchronously requests the user's current location. When location becomes available, it is
     * passed to a function provided by the caller.
     *
     * @param func The function to be called when location becomes available
     * */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getCommuterLocation(func: (LatLng?) -> Unit) {
        client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener {
            location ->
                if (location == null) func(null)
                else func(LatLng(location.latitude, location.longitude))
        };
    }

    @Volatile private var isRunning = false; //keeps update loop thread alive
    /**
     * Starts the commuter's update loop.
     *
     * This function should be called when a route is started.
     * */
    public fun onRouteStart(navigator: Navigator){
        Log.d("routeDebug", "onRouteStart()");
        this.setRoute(navigator.currentRouteSegment);
        navigator.startGuidance() // Start turn-by-turn guidance along the current route.

        isRunning = true;
        Thread{
            try {
                runUpdateLoop()
            }
            catch (e: SecurityException){}
        }.start()
    }

    /**
     * Sets `route` to `null`.
     * Stops the commuter's update loop.
     *
     * This function should be called when the route is finished.
     * */
    public fun onRouteEnd(){
        this.isRunning = false; //kills the thread
        this.route = null; // `route` COULD be used as the condition instead, but the caller might accidentally stop the update loop by setting it to null
    }

    /**
     * The update loop does the following tasks every `UPDATE_FREQUENCY` milliseconds:
     *  - Remove all snapshots older than `IDLE_TIMEOUT` from `locationSnapshots`
     *  - Send user a notification every `IDLE_TIMEOUT` seconds if they are idle
     *  - Request a location snapshot (updates `locationSnapshots` and `lastLocation` on success)
     *
     * (This is meant to be called from another thread while a route is active)
     * */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun runUpdateLoop(){
        val idleNotifications = Notifications("idle", "idle");
        var idleNotificationTimestamp = System.currentTimeMillis();
        val deviationNotifications = Notifications("deviation", "deviation");
        var deviationNotificationTimestamp = idleNotificationTimestamp;
        while (isRunning) {
            val time = System.currentTimeMillis();
            synchronized(locationSnapshots){ // locations are added asynchronously so there may be race conditions (however unlikely because the thread sleeps for a whole second)
                while (!locationSnapshots.isEmpty() && locationSnapshots.peek()!!.time < time - IDLE_TIMEOUT*1000){
                    locationSnapshots.poll()
                }
            }

            if (time - idleNotificationTimestamp >= IDLE_TIMEOUT*1000 && isUserIdle()){
                try {
                    idleNotifications.sendNotification("You are idle.", user!!.getCommuterNote());
                } catch (e: SecurityException) {}
                idleNotificationTimestamp = time; // notify every `IDLE_TIMEOUT` seconds
            }

            try { //DEBUGGING
                deviationNotifications.sendNotification("Distance", "${distanceFromRoute()}");
            } catch (e: SecurityException) {}

            if (time - deviationNotificationTimestamp >= DEVIATION_NOTIFICATION_FREQUENCY*1000 && distanceFromRoute() > DEVIATION_DISTANCE){
                try {
                    deviationNotifications.sendNotification("You're going the wrong way!", "Please return to the route.");
                } catch (e: SecurityException) {}
                deviationNotificationTimestamp = time; // notify every `DEVIATION_NOTIFICATION_FREQUENCY` seconds
            }

            updateLocation();

            Thread.sleep(UPDATE_FREQUENCY);
        }
    }

    /**
     * Attempts to update the commuter's location snapshots and last known location.
     *
     * The user's last known location is written to the database.
     * */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun updateLocation(){
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location ->
            if (location == null) return@addOnSuccessListener
            lastLocation = LatLng(location.latitude, location.longitude)
            synchronized(locationSnapshots){
                locationSnapshots.add(location)
            }

            database.setLastLocation(lastLocation);
        }
    }

    /**
     * Checks if the user is idle.
     *
     * This function converts all `Location` entries in `locationSnapshots` into Cartesian coordinates
     * using `lastLocation` as a reference point, then calculates the smallest circle bounding these
     * coordinates. The circle's radius is compared with `IDLE_RADIUS` to decide if the user is idle.
     *
     * @return true if the user is idle, false otherwise
     * */
    private fun isUserIdle() : Boolean{ //can be made public if needed elsewhere
        if (lastLocation == null) return false;
        val reference = lastLocation!!;
        var coordinates : Array<Coordinate?>;
        synchronized(locationSnapshots){
            coordinates = Array<Coordinate?>(locationSnapshots.size) { null }
            for ((i, location) in locationSnapshots.withIndex()) {
                coordinates[i] = CoordinateMath.toCoordinate(LatLng(location.latitude, location.longitude), reference); //convert to Cartesian coordinates
            }
        }

        return CoordinateMath.minBoundingCircleRadius(coordinates as Array<Coordinate>) <= IDLE_RADIUS;
    }

    /**
     * Calculates the user's distance from the route.
     *
     * The route is treated as a series of line segments, and the perpendicular distance of
     * `lastLocation` from each line segment is calculated.
     *
     * @return Distance from route in meters
     * */
    private fun distanceFromRoute() : Double{
        if (route == null || lastLocation == null) return Double.NaN; // `Double.NaN` fails any comparison
        val points = route!!.latLngs;
        val reference = lastLocation!!;

        var min = Double.POSITIVE_INFINITY
        var start = CoordinateMath.toCoordinate(points[0], reference);
        for (i in 1 until points.size) {
            val end = CoordinateMath.toCoordinate(points[i], reference);
            val dist = CoordinateMath.lineSegmentDistanceFromOrigin(start, end) // `reference` (`lastLocation`) becomes origin in this coordinate system

            if (dist < min) min = dist;
            start = end;
        }

        Log.d("distanceDebug", "dist: $min");
        return min;
    }

    companion object {
        private val UPDATE_FREQUENCY = 1000L; // milliseconds
        private val IDLE_TIMEOUT = 5 * 60; // seconds
        private val IDLE_RADIUS = 25; // meters
        private val DEVIATION_DISTANCE = 20; // meters
        private val DEVIATION_NOTIFICATION_FREQUENCY = 10; //seconds
    }
}