package com.example.setupformaps.user

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.example.setupformaps.MapsActivity
import com.example.setupformaps.location.Route
import com.example.setupformaps.user
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
    public fun updateUserPosition(){ //we really dont need this anymore. it's just easier to type
        getCommuterLocation { pos ->
            if (pos != null)  lastLocation = LatLng(pos.latitude, pos.longitude);
        }
    }
}