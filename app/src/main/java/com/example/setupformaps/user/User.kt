package com.example.setupformaps.user

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.navigation.RouteSegment

abstract class User {
    public abstract fun setCommuterNote(note: String);
    public abstract fun getCommuterNote() : String;
    public abstract fun setCommuterRoute(route: RouteSegment?);
    public abstract fun getCommuterRoute() : RouteSegment?;
    public abstract fun getCommuterLocation() : LatLng?;
    public abstract fun getCommuterLocation(func: (LatLng?) -> Unit);
    //get commuter location (probably asynchronously), then do something with it
}