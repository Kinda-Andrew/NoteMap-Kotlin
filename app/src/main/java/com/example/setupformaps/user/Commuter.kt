package com.example.setupformaps.user

import android.Manifest
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.setupformaps.location.Route
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import java.util.PriorityQueue
import org.locationtech.jts.algorithm.MinimumBoundingCircle
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import kotlin.math.cos

class Commuter(context: Context) : User() {
    private val UPDATE_FREQUENCY = 1000L; // milliseconds
    private val IDLE_TIMEOUT = 60*5; // seconds
    private val IDLE_RADIUS = 25; // meters

    private var client =  LocationServices.getFusedLocationProviderClient(context);
    private var lastLocation : LatLng? = null;
    private var locationSnapshots = PriorityQueue<Location> { o1, o2 -> // sorted oldest to newest
        o1.time.compareTo(o2.time)
    } // Snapshots older than `IDLE_TIMEOUT` should be discarded using `poll()`


    private var shareableCode : String = "";
    private var note : String = "";
    private var route : Route? = null;


    override fun getCommuterNote(): String { return note; }

    override fun setCommuterNote(note: String) { this.note = note; }

    override fun getCommuterRoute(): Route? { return route; }

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

    /**
     * The update loop does the following tasks every `UPDATE_FREQUENCY` milliseconds:
     *  - Remove all snapshots older than `IDLE_TIMEOUT` from `locationSnapshots`
     *  - Check if the user is idle
     *  - Request a location snapshot (updates `locationSnapshots` and `lastLocation` on success)
     * (It is meant to be called from another thread while a route is active)
     * */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    public fun runUpdateLoop(){
        val startTimestamp = System.currentTimeMillis();
        while (true) { //change to `while (route != null)` later
            val time = System.currentTimeMillis();
            synchronized(locationSnapshots){ // locations are added asynchronously so there may be race conditions (however unlikely because the thread sleeps for a whole second)
                while (!locationSnapshots.isEmpty() && locationSnapshots.peek()!!.time < time - IDLE_TIMEOUT*1000){
                    locationSnapshots.poll()
                }
            }

            if (time - startTimestamp >= IDLE_TIMEOUT*1000 && isUserIdle()){
                Log.d("IdleDebug","User is idle");
                // TODO: send notification to user
            }

            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location ->
                if (location == null) return@addOnSuccessListener
                lastLocation = LatLng(location.latitude, location.longitude)
                synchronized(locationSnapshots){
                    locationSnapshots.add(location)
                }
            }

            Thread.sleep(UPDATE_FREQUENCY);
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
                coordinates[i] = toCoordinate(location, reference); //convert to Cartesian coordinates
            }
        }

        val minBoundingCircle = MinimumBoundingCircle(
            GeometryFactory().createMultiPointFromCoords(coordinates)
        ) // the smallest circle that can enclose every location snapshot

        return minBoundingCircle.radius <= IDLE_RADIUS;
    }


    /* HELPER FUNCTIONS */

    /**
     * Converts a GPS location into Cartesian coordinates, given a reference point.
     *
     * @param location The GPS location to convert
     * @param reference The reference point (used as the origin)
     * @return The corresponding Cartesian coordinate in meters
     */
    private fun toCoordinate(location: Location, reference: LatLng): Coordinate {
        val earthRadius = 6371000.0

        val lat0 = Math.toRadians(reference.latitude)
        val lat = Math.toRadians(location.latitude)
        val lon = Math.toRadians(location.longitude)
        val lon0 = Math.toRadians(reference.longitude)

        val x = earthRadius * (lon - lon0) * cos(lat0)
        val y = earthRadius * (lat - lat0)

        return Coordinate(x, y)
    } // if we end up with some sort of geolocation utility class, we can create a more generic version of this function there and use that instead
}