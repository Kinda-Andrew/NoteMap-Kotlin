package com.example.setupformaps.user

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.example.setupformaps.MapsActivity
import com.example.setupformaps.location.Route
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class Commuter(context: Context) : User() {
    private var client =  LocationServices.getFusedLocationProviderClient(context);
    private var lastLocation : LatLng? = null;


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
    override fun moveCameraToCommuter(map: GoogleMap){
        client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location == null) return@addOnSuccessListener;

            val pos = LatLng(location.latitude, location.longitude);
            lastLocation = pos;

            map.moveCamera(CameraUpdateFactory.newLatLng(pos));
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    public fun updateUserPosition(){
        client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            lastLocation = LatLng(location.latitude, location.longitude);
        }
    }
}