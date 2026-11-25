package com.example.setupformaps

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.setupformaps.application.Permissions
import com.example.setupformaps.user.Commuter
import com.example.setupformaps.user.User
import com.google.android.libraries.navigation.NavigationFragment
import com.google.android.libraries.places.api.model.Place


var user : User? = null;
lateinit var CONTEXT : Context;

/**
 * Main Activity that opens on launch of the app.
 * Eventually will contain options for commuter or caretaker.
 * Once a route is finished, the commuter is returned here.
 * */
class HomePageActivity : AppCompatActivity() {

    private lateinit var navFragment: NavigationFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        /**
         * create Commuter object, which stores the users note and destination
         */
        if (user == null) user = Commuter(this);
        CONTEXT = applicationContext;

        Permissions.request(Manifest.permission.POST_NOTIFICATIONS, 0, this)

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        /**
         * assigns button to send the user to map activity
         */
        val sendMapButton: Button = findViewById(R.id.map_button_id)
        sendMapButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        /**
         * assigns a button to send the user to the navigation screen
         */
        val sendNavButton: Button = findViewById(R.id.navigation_button_id)
        sendNavButton.setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java)
            startActivity(intent)
        }

        val placeResult = null



        val placeLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { intent ->
                    val place: Place = PlacePickerActivity.getPlace(intent)
                    // Use place.name, place.latLng, etc.
                    //IT WORK WOOOO
                    Log.i("Main Activity",place.id );

                }
            }
        }

        /**
         * assigns a button to send the user to input their destination
         */
        val sendPlaceButton: Button = findViewById(R.id.place_button_id)
        sendPlaceButton.setOnClickListener {
            val intent = Intent(this, PlacePickerActivity::class.java)
            placeLauncher.launch(intent)
        }
    }

    /**
     * Uses the Google Places API Place Picker to choose a destination to navigate to.
     *
     * This method is referenced by the "Set Destination" item in menu_default.xml
     */
/*
    fun showPlacePickerForDestination(v: MenuItem?): Boolean {
        try {
            startActivityForResult(Intent(this, PlacePickerActivity::class.java), PLACE_PICKER_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(this,
                "Could not display Place Picker. Check your API key has the Google" + "Places API enabled.",Toast.LENGTH_SHORT
            ).show()
            Log.e(TAG, Log.getStackTraceString(e))
        }
        return true
    }
*/

//dont think this is needed. will keep for now, so I dont forget
    /*
  /*   If the Place Picker activity returns a destination, starts navigation to that place. */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            data?.let {
                val place: Place = PlacePickerActivity.getPlace(it)
                navigateToPlace(place)
            }
        }


*/

    }





