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
import org.locationtech.jts.algorithm.MinimumBoundingCircle
import java.util.PriorityQueue
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import kotlin.math.cos

class Commuter(context: Context) : User() {
    private val UPDATE_FREQUENCY = 1000L; // ms
    private val IDLE_TIMEOUT = 15; // s
    private val IDLE_RADIUS = 10; // m

    private var client =  LocationServices.getFusedLocationProviderClient(context);
    private var lastLocation : LatLng? = null;
    private var locationSnapshots = PriorityQueue<Location> { o1, o2 ->
        o1.time.compareTo(o2.time)
    } // sorted oldest to newest; discard expired snapshots


    private var shareableCode : String = "";
    private var note : String = "";
    private var route : Route? = null;


    override fun getCommuterNote(): String { return note; }

    override fun setCommuterNote(note: String) { this.note = note; }

    override fun getCommuterRoute(): Route? { return route; }

    override fun getCommuterLocation(): LatLng? {
        return lastLocation;
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getCommuterLocation(func: (Location?) -> Unit) {
        client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener {
                location ->
            func(location)
        };
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    public override fun update(){ //run this on another thread while a route is active
        val startTimestamp = System.currentTimeMillis();
        while (true) { //while (route != null)
            Log.d("IdleDebug","update() called");
            val time = System.currentTimeMillis();
            synchronized(locationSnapshots){
                while (!locationSnapshots.isEmpty() && locationSnapshots.peek().time < time - IDLE_TIMEOUT*1000){
                    locationSnapshots.poll()
                }
            }

            if (time - startTimestamp >= IDLE_TIMEOUT*1000 && isUserIdle()){
                Log.d("IdleDebug","User is idle");
            }

            getCommuterLocation { location ->
                if (location == null) return@getCommuterLocation
                lastLocation = LatLng(location.latitude, location.longitude)
                synchronized(locationSnapshots){
                    locationSnapshots.add(location)
                }
            } // update location snapshots asynchronously

            Thread.sleep(UPDATE_FREQUENCY);
        }
    }

    private fun isUserIdle() : Boolean{ //can be made public if needed elsewhere
        if (lastLocation == null) return false;
        val reference = lastLocation!!;
        var coordinates : Array<Coordinate?>;
        synchronized(locationSnapshots){
            coordinates = Array<Coordinate?>(locationSnapshots.size) { null }
            for ((i, location) in locationSnapshots.withIndex()) {
                coordinates[i] = toCoordinate(location, reference);
            }
        }
        Log.d("IdleDebug", coordinates.contentToString());

        val minBoundingCircle = MinimumBoundingCircle(
            GeometryFactory().createMultiPointFromCoords(coordinates)
        ) // the smallest circle that can enclose every location snapshot

        Log.d("IdleDebug", "Radius: " + minBoundingCircle.radius);
        val loc = Location("manual").apply {
            latitude = reference.latitude
            longitude = reference.longitude
        }
        Log.d("IdleDebug", "Displacement: " + loc.distanceTo(locationSnapshots.peek()));
        //if you walk in a straight line, radius should be about 1/2 * displacement

        return minBoundingCircle.radius <= IDLE_RADIUS;
    }


    /* HELPERS */
    private fun toCoordinate(location: Location, reference: LatLng): Coordinate {
        val earthRadius = 6371000.0

        val lat0 = Math.toRadians(reference.latitude)
        val lat = Math.toRadians(location.latitude)
        val lon = Math.toRadians(location.longitude)
        val lon0 = Math.toRadians(reference.longitude)

        val x = earthRadius * (lon - lon0) * cos(lat0)
        val y = earthRadius * (lat - lat0)

        return Coordinate(x, y)
    }
}