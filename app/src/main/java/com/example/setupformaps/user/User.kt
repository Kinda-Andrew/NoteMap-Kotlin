package com.example.setupformaps.user

import com.example.setupformaps.location.Route
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

abstract class User {
    public abstract fun getCommuterNote() : String;
    public abstract fun setCommuterNote(note: String);
    public abstract fun getCommuterRoute() : Route?;
    public abstract fun getCommuterLocation() : LatLng?;
    public abstract fun moveCameraToCommuter(map: GoogleMap);

}