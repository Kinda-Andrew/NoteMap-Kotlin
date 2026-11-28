package com.example.setupformaps.user

import com.example.setupformaps.application.DatabaseManager
import com.google.android.gms.maps.model.LatLng

class Caretaker(private val code: String) : User() {
    private var note : String = "";
    private var destinationId : String = "";
    private var route : List<LatLng>? = null;
    private var lastLocation : LatLng? = null;
    private val database = DatabaseManager(
        code,
        this::onNoteChanged,
        this::onDestinationChanged,
        this::onRouteChanged,
        this:: onLastLocationChanged
    );

    override fun getCommuterNote(): String { return note; }
    override fun setCommuterNote(note: String) {
        database.setNote(note); // update database
    }
    private fun onNoteChanged(note: String?){ // update local `note` only when database is updated
        if (note != null) this.note = note;
        else this.note = "";
    }


    override fun getCommuterRoute(): List<LatLng>? {
        return route;
    }
    private fun onRouteChanged(route: List<LatLng>?){
        this.route = route;
        // TODO: draw polyline in MapsActivity
    }


    override fun getCommuterLocation(): LatLng? {
        return lastLocation;
    }
    override fun getCommuterLocation(func: (LatLng?) -> Unit) {
        func(lastLocation); // i dont think we need this anywhere
    }

    override fun setCommuterDestinationId(destinationId: String) {
        database.setDestination(destinationId); //update database
    }

    override fun getCommuterDestinationId(): String {
        return destinationId;
    }
    private fun onDestinationChanged(dest: String?){
        if (dest != null) this.destinationId = dest; // update local `destination` only when database is updated
        else this.destinationId = "";
    }

    override fun setCommuterDestinationName(destinationName: String) {

    }
    override fun getCommuterDestinationName(): String {
        return "placeholder";
    }

    private fun onLastLocationChanged(loc: LatLng?){
        lastLocation = loc;
        // TODO: move a dot on map representing the commuter
    }
}