package com.example.setupformaps.user

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import com.example.setupformaps.MapsActivity
import com.example.setupformaps.location.Route
import com.example.setupformaps.user
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import java.util.PriorityQueue

class Commuter(context: Context) : User() {
    private val UPDATE_FREQUENCY = 1000L; // ms
    private val IDLE_TIMEOUT = 5*60; // s
    private val IDLE_RADIUS = 25; // m

    private var client =  LocationServices.getFusedLocationProviderClient(context);
    private var lastLocation : LatLng? = null;
    private var locationSnapshots = PriorityQueue<Location>
    { o1, o2 -> (o1.time - o2.time).toInt() }
    // sorted oldest to newest; discard expired snapshots


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
    override fun getCommuterLocation(func: (Location) -> Unit) {
        client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener {
                location ->
            func(location)
        };
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    public fun update(){ //run this on another thread while a route is active
        while (route != null) {
            val cutoff = System.currentTimeMillis() - IDLE_TIMEOUT*1000;
            while (!locationSnapshots.isEmpty() && locationSnapshots.peek().time < cutoff){
                locationSnapshots.poll(); //remove old snapshots
            }

            getCommuterLocation { location ->
                lastLocation = LatLng(location.latitude, location.longitude)
                locationSnapshots.add(location);
            } // update location snapshots asynchronously

            Thread.sleep(UPDATE_FREQUENCY);
        }
    }
}