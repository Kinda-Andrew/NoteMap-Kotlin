package com.example.setupformaps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.setupformaps.databinding.ActivityMapsBinding
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.setupformaps.application.Permissions
import com.example.setupformaps.user.Commuter

//This activity is responsible for displaying the map,
//as well as routing the user to note input and destination input
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    public lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply { //hide the navigation bar at the bottom of the screen
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // buttons to navigate to the note making page and back to the home page from the map
        val sendMapButton: Button = findViewById(R.id.home_button_id)
        sendMapButton.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        //assigns button to send to note activity
        val sendNoteButton: Button = findViewById(R.id.note_button_id)
        sendNoteButton.setOnClickListener {
            val intent = Intent(this, NoteWriteActivity::class.java)
            startActivity(intent)
        }
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

        if (Permissions.request(Manifest.permission.ACCESS_FINE_LOCATION, Permissions.MAP_STARTUP_REQUEST_CODE, this)) tryShowUserOnMap();
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode,  permissions, grantResults) //optional

        if (requestCode == Permissions.MAP_STARTUP_REQUEST_CODE){
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
                mMap.uiSettings.isZoomControlsEnabled = true; // here for navigational ease, adds the + and - options onto the bottom right of the map
                if (pos != null)  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f))
                    // changed to animateCamera so zoom could be added, the higher the zoom the closer it zooms in
                Thread{ (user!! as Commuter).runUpdateLoop() }.start() // this should probably be done on route start
            } //requests commuter location, then moves the camera to that location when it becomes available
        } catch (e: SecurityException){}
    }
}