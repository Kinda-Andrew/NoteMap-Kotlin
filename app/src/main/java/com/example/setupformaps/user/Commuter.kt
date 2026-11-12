package com.example.setupformaps.user

import com.example.setupformaps.location.Route
import com.google.android.gms.maps.model.LatLng

class Commuter : User() {
    private var shareableCode : String = "";
    private var note : String = "";
    private var route : Route? = null;


    override fun getCommuterNote(): String { return note; }

    override fun setCommuterNote(note: String) { this.note = note; }

    override fun getCommuterRoute(): Route? { return route; }

    override fun getCommuterLocation(): LatLng? {
        return null; // TODO: Implement
    }
}