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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    public fun update(){ //run this on another thread while a route is active
        val startTimestamp = System.currentTimeMillis();
        while (true) { //change to `while (route != null)` later
            Log.d("IdleDebug","update() called");
            val time = System.currentTimeMillis();
            synchronized(locationSnapshots){
                while (!locationSnapshots.isEmpty() && locationSnapshots.peek().time < time - IDLE_TIMEOUT*1000){
                    locationSnapshots.poll()
                }
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

}