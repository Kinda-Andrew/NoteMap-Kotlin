package com.example.setupformaps

import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.setupformaps.databinding.ActivityMapsBinding
import com.example.setupformaps.location.LocationPermissions
import com.example.setupformaps.user.Commuter
import com.example.setupformaps.user.User
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    public lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    //width of polyline displayed on map
    private val ROUTE_STROKE_WIDTH_PX = 5

    //test polyline coords
    private val adelaideLatLng = LatLng(-34.92873, 138.59995)
    private val darwinLatLng = LatLng(-12.4258647, 130.7932231)
    private val melbourneLatLng = LatLng(-37.81319, 144.96298)
    private val perthLatLng = LatLng(-31.95285, 115.85734)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (LocationPermissions.isEnabled(this)) tryShowUserOnMap();
        else LocationPermissions.requestPermissions(this, 1);

    //send destination to tryShowUserOnMap() ? not sure best place to do it

        googleMap.addPolyline(
            PolylineOptions().apply {
                add(adelaideLatLng, darwinLatLng, melbourneLatLng, perthLatLng)
                width(ROUTE_STROKE_WIDTH_PX.toFloat())
                color(Color.BLUE)
            },
        )


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode,  permissions, grantResults) //optional

        if (requestCode == 1){
            // request code 1 indicates that location permissions were requested during MapsActivity start (this is completely arbitrary)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("LocationDebug","Granted");
                tryShowUserOnMap();
            }
        }
    }

    fun tryShowUserOnMap(){
        try {
            mMap.isMyLocationEnabled = true //enables the blue dot on the map that shows the user's location. If permissions are not enabled, this will throw an exception
            user!!.getCommuterLocation { pos ->
                if (pos != null)  mMap.moveCamera(CameraUpdateFactory.newLatLng(pos))
            } //requests commuter location, then moves the camera to that location when it becomes available
        } catch (e: SecurityException){}





    }
}