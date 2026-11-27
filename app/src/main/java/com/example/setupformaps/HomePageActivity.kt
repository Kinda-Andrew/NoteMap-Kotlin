package com.example.setupformaps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.setupformaps.application.ButtonClickManager
import com.example.setupformaps.application.Permissions
import com.example.setupformaps.user.Commuter
import com.example.setupformaps.user.User
import com.google.android.libraries.navigation.NavigationFragment
import com.google.android.libraries.places.api.model.Place


var user : User? = null;
var buttonClickManager : ButtonClickManager? = null;
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

     //   val buttonClickManager = ButtonClickManager();

        /**
         * create Commuter object, which stores the users note and destination
         */
        if (user == null) user = Commuter(this);
        if (buttonClickManager == null) buttonClickManager = ButtonClickManager();
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
         * Initialize the three main buttons
         * The buttons are only clickable if the previous buttons'
         * activity has been completed
         * ie a destination must be set before writing a note
         */
        initializePlaceButton()

        initializeNoteButton()

        initializeNavButton()
    }


    fun initializePlaceButton(){
        val placeLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { intent ->
                    val place: Place = PlacePickerActivity.getPlace(intent)
                    /** the ?: is in case the string place.id is null. This should not happen,
                     * but could be a possibility with google places api? this is just safer
                     */
                    user?.setCommuterDestinationId(place.id?: "");
                    /**
                     * allow the user to write a note, as a destination has been selected
                     */
                    buttonClickManager?.setDestinationEntered(true)

                }
            }
        }

        /**
         * assigns a button to send the user to input their destination
         * No isButtonClickable check, as the destination is the first step
         * and should always be clickable
         */
        val sendPlaceButton: Button = findViewById(R.id.place_button_id)
        sendPlaceButton.setOnClickListener {
            val intent = Intent(this, PlacePickerActivity::class.java)
            placeLauncher.launch(intent)
        }
    }

    fun initializeNoteButton(){

        val noteLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { intent ->
                    /**
                     * handle other result codes? not sure if it is
                     * needed
                     * Set noteEntered to true, so the user can proceed
                     * to navigation
                     */
                    buttonClickManager?.setNoteEntered(true)
                }
            }
        }

        val sendNoteButton: Button = findViewById(R.id.note_button_id)
        sendNoteButton.setOnClickListener {
            /**
             * checks if the user should be allowed to enter a note
             * should only be true if a destination was set
             * On any result, allow the user to continue.
             * this is because the user does not have to enter a note,
             * so viewing the page is enough
             */
            if(buttonClickManager?.isClickAllowed(findViewById(R.id.note_button_id)) == true){
                val intent = Intent(this, NoteWriteActivity::class.java)
                noteLauncher.launch(intent)
            }
            else{
                /**
                 * indicate this button can't be pressed yet
                 */
                buttonClickManager?.animateButton(findViewById(R.id.note_button_id))
            }
        }
    }

    fun initializeNavButton(){
        /**
         * assigns a button to send the user to the navigation screen
         */
        val sendNavButton: Button = findViewById(R.id.navigation_button_id)
        sendNavButton.setOnClickListener {
            if (buttonClickManager?.isClickAllowed(findViewById(R.id.navigation_button_id)) == true) {
                val intent = Intent(this, NavigationActivity::class.java).apply {
                    putExtra("destinationId", user?.getCommuterDestinationId())
                }
                startActivity(intent)
                }

            else{
                /**
                 * indicate this button can't be pressed yet
                 */
                buttonClickManager?.animateButton(findViewById(R.id.navigation_button_id))
            }
        }
    }
}





