package com.example.setupformaps.location

import com.google.android.gms.maps.model.LatLng

class Route ( 
    private var start: LatLng,
    private var destination: LatLng
    ) {
    public fun getStart(): LatLng{ return start; }
    public fun getDestination(): LatLng { return destination; }
    public fun setDestination(dest: LatLng) { this.destination = destination };
}