package com.example.setupformaps.application

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.navigation.RouteSegment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * This class is used to manage the database. Data is stored in the following format:
 *
 * "PAIRING_CODE": {
 *   "note": "note for commuter",
 *   "destination": "place ID",
 *   "route": {
 *      0: {
 *          "lat": Double,
 *          "lng": Double
 *      }, ...
 *   },
 *   "lastLocation": {
 *     "lat": Double,
 *     "lng": Double
 *   }
 * }
 *
 * Intended usage:
 *  - `note` can be set by both the caretaker and commuter
 *  - `destination` can be set by both the caretaker and commuter; the caretaker can use this to indirectly set the route
 *  - `route` is calculated and written by the commuter and read by the caretaker
 *  - `lastLocation` is set by the commuter and read by the caretaker
 *
 * */
class DatabaseManager(
    code : String,
    private val onNoteChanged: ((String?) -> Unit) ?,
    private val onDestinationChanged: ((String?) -> Unit)?,
    private val onRouteChanged : ((List<LatLng>?) -> Unit)?,
    private val onLastLocationChanged : ((LatLng?) -> Unit)?
    ) {
    private val ref = FirebaseDatabase.getInstance(URL).reference.child(code);
    private val noteField = ref.child("note");
    private val destinationField = ref.child("destination")
    private val routeField = ref.child("route")
    private val lastLocationField = ref.child("lastLocation");

    init{
        noteField.setValue("placeholder"); //for debugging
    }

    init{
        // setup listeners for when data changes
        // for most of these im currently assuming there is no malformed data

        if (onNoteChanged != null) {
            noteField.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    onNoteChanged((data.value as String?));
                }

                override fun onCancelled(e: DatabaseError) {}
            })
        }

        if (onDestinationChanged != null) {
            destinationField.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    onDestinationChanged((data.value as String?));
                }

                override fun onCancelled(e: DatabaseError) {}
            })
        }

        if (onRouteChanged != null) {
            routeField.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    var list : ArrayList<LatLng>? = null;
                    list = ArrayList();
                    for (latLng in data.children) {
                        val lat = latLng.child("lat").value as Double?;
                        val lng = latLng.child("lng").value as Double?;
                        if (lat != null && lng != null) {
                            list!!.add(LatLng(lat, lng));
                        }
                        else{
                            list = null; //found malformed data
                            break;
                        }
                    }
                    onRouteChanged(list);
                }

                override fun onCancelled(e: DatabaseError) {}
            })
        }

        if (onLastLocationChanged != null) {
            lastLocationField.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    val lat = data.child("lat").value as Double?;
                    val lng = data.child("lng").value as Double?;
                    if (lat != null && lng != null) {
                        onLastLocationChanged(
                            LatLng(lat, lng)
                        );
                    }
                    else onLastLocationChanged(null);
                }

                override fun onCancelled(e: DatabaseError) {}
            })
        }
    }

    public fun setNote(note: String?){
        noteField.setValue(note);
    }

    public fun setDestination(placeID: String?){
        destinationField.setValue(placeID)
    }

    public fun setRoute(route : RouteSegment?){
        if (route != null) {
            routeField.setValue(route.latLngs.map {
                mapOf("lat" to it.latitude, "lng" to it.longitude)
            })
        }
        else routeField.setValue(null);
    }

    public fun setLastLocation(loc : LatLng?){
        if (loc == null) lastLocationField.setValue(null);
        else lastLocationField.setValue(
            mapOf(
                "lat" to loc.latitude,
                "lng" to loc.longitude
            )
        );
    }
    companion object{
        private val URL = "https://notemap-database-default-rtdb.firebaseio.com/";
    }
}