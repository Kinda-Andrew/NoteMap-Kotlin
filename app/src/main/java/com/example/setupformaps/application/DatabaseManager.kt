package com.example.setupformaps.application

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.navigation.RouteSegment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DatabaseManager(
    code : String,
    private val onNoteChanged: ((String?) -> Unit) ?,
    private val onDestinationChanged: ((LatLng?) -> Unit)?,
    private val onRouteChanged : ((List<LatLng>) -> Unit)?,
    private val onLastLocationChanged : ((LatLng?) -> Unit)?
    ) {
    private val ref = FirebaseDatabase.getInstance(URL).reference.child(code);
    private val noteField = ref.child("note");
    private val destinationField = ref.child("destination")
    private val routeField = ref.child("route")
    private val lastLocationField = ref.child("lastLocation");

    init{
        // setup listeners for when data changes
        // im currently assuming there is no malformed data

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
                    onDestinationChanged((data.value as LatLng?));
                }

                override fun onCancelled(e: DatabaseError) {}
            })
        }

        if (onRouteChanged != null) {
            routeField.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    var list = ArrayList<LatLng>();
                    for (latLng in data.children) {
                        val lat = latLng.child("lat").value as Double?;
                        val lng = latLng.child("lng").value as Double?;
                        if (lat != null && lng != null) {
                            list.add(LatLng(lat, lng));
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

    public fun setDestination(dest: LatLng?){
        if (dest == null) destinationField.setValue(null);
        else{
            destinationField.setValue(
                mapOf(
                    "lat" to dest.latitude,
                    "lng" to dest.longitude
                )
            )
        }
    }

    public fun setRoute(route : RouteSegment?){
        if (route != null) {
            routeField.setValue(route.latLngs.map { mapOf("lat" to it.latitude, "lng" to it.longitude) })
        }
        else routeField.setValue(null);
    }

    public fun setLastLocation(loc : LatLng?){
        lastLocationField.setValue(loc);
    }
    companion object{
        private val URL = "https://notemap-database-default-rtdb.firebaseio.com/";
    }
}