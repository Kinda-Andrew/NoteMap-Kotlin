package com.example.setupformaps.location

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.navigation.RouteSegment

//not implemented yet
class Route ( 
    private var start: LatLng,
    private var destination: LatLng
    ) {
    init{
        //calculate route and store the RouteSegment in `route`
    }
    private lateinit var route : RouteSegment

    public fun getStart(): LatLng{ return start; }
    public fun getDestination(): LatLng { return destination; }
    public fun getRoute() : List<LatLng> { return route.latLngs }
}