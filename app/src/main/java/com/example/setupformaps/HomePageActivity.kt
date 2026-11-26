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
import com.example.setupformaps.application.ButtonUIHandler
import com.example.setupformaps.application.Permissions
import com.example.setupformaps.databinding.ActivityMainBinding
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

        val sendNoteButton: Button = findViewById(R.id.note_button_id)
        sendNoteButton.setOnClickListener {
            val intent = Intent(this, NoteWriteActivity::class.java)
            startActivity(intent)
        }

        /**
         * assigns a button to send the user to the navigation screen
         */
        val sendNavButton: Button = findViewById(R.id.navigation_button_id)
        sendNavButton.setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java).apply {
                if(user?.getCommuterDestinationId() != ""){
                    putExtra("destinationId",user?.getCommuterDestinationId())
                }
                else{
                    //either cancel the click, cause no destination is set
                    // or block the user from even clicking on this if destination isn't set?
                }
            }
            startActivity(intent)
        }


        /**
         * assigns a callback to set the user's placeId once they have completed the navigation
         * activity.
         */
        val placeLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { intent ->
                    val place: Place = PlacePickerActivity.getPlace(intent)
                    /** the ?: is in case the string place.id is null. This should not happen,
                      * but could be a possibility with google places api? this is just safer
                      */
                    Log.i("Main Activity",place.id?: "" );
                    user?.setCommuterDestinationId(place.id?: "");

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

}





