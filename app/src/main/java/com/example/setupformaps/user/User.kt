package com.example.setupformaps.user

import android.location.Location
import com.example.setupformaps.location.Route
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

abstract class User {
    public abstract fun getCommuterNote() : String;
    public abstract fun setCommuterNote(note: String);
    public abstract fun getCommuterRoute() : Route?;
    public abstract fun getCommuterLocation() : LatLng?;
    public abstract fun getCommuterLocation(func: (Location) -> Unit);
    //get commuter location (probably asynchronously), then do something with it
}