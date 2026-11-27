package com.example.setupformaps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        val sendPlaceButton: Button = findViewById(R.id.place_button_id)
        val sendNoteButton: Button = findViewById(R.id.note_button_id)
        val sendNavButton: Button = findViewById(R.id.navigation_button_id)


        setupPlaceButton(sendPlaceButton,sendNoteButton,sendNavButton)
        setupNoteButton(sendPlaceButton,sendNoteButton,sendNavButton)
        setupNavButton(sendPlaceButton,sendNoteButton,sendNavButton)
    }


    fun setupPlaceButton(sendPlaceButton: Button,sendNoteButton: Button,sendNavButton: Button){

        val placeLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { intent ->
                    val place: Place = PlacePickerActivity.getPlace(intent)
                    /** the ?: is in case the string place is null. This should not happen,
                     * but could be a possibility with google places api? this is just safer
                     */
                    user?.setCommuterDestinationId(place.id?:"");
                    user?.setCommuterDestinationName(place.displayName?:"")
                    /**
                     * allow the user to write a note, as a destination has been selected
                     */
                    buttonClickManager?.setDestinationEntered(true)
                    evaluateButtonColor(sendPlaceButton,sendNoteButton,sendNavButton)
                }
            }
        }

        /**
         * assigns a button to send the user to input their destination
         * No isButtonClickable check, as the destination is the first step
         * and should always be clickable
         */

        sendPlaceButton.setOnClickListener {
            val intent = Intent(this, PlacePickerActivity::class.java)
            placeLauncher.launch(intent)
        }


        /**
         * sets the color if the button's activity has been completed or not
         */
        evaluateButtonColor(sendPlaceButton,sendNoteButton,sendNavButton)

    }

    fun setupNoteButton(sendPlaceButton: Button,sendNoteButton: Button,sendNavButton: Button){

        val noteLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            /**
             * i dont think the result matters?
             * all that matters is that the user visited the note page, as the
             * note is allowed to be empty
             */
       //     if (result.resultCode == RESULT_OK) {
                    buttonClickManager?.setNoteEntered(true)
                    evaluateButtonColor(sendPlaceButton,sendNoteButton,sendNavButton)

          //  }

        }

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

        /**
         * set the color of sendNoteButton based on if the user has entered a
         * destination and a note.
         */
        evaluateButtonColor(sendPlaceButton,sendNoteButton,sendNavButton)

    }

    fun setupNavButton(placeButton: Button, noteButton: Button, sendNavButton: Button){
        /**
         * assigns a button to send the user to the navigation screen
         */
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

    fun setButtonColor(button : Button, id : Int){
        val color = ContextCompat.getColor(this, id)
        button.backgroundTintList = ColorStateList.valueOf(color)
    }

    /**
     * checks which buttons should be which color, and sets them.
     */
    fun evaluateButtonColor(placeButton: Button,noteButton: Button,navButton: Button){

        if(buttonClickManager?.getDestinationEntered() == false){
            setButtonColor(placeButton,R.color.yellow);
            setButtonColor(noteButton,R.color.grey);
            setButtonColor(navButton,R.color.grey);

        }
        else if(buttonClickManager?.getDestinationEntered() == true && buttonClickManager?.getNoteEntered() == false){
            setButtonColor(placeButton,R.color.green);
            setButtonColor(noteButton,R.color.yellow);
            setButtonColor(navButton,R.color.grey);
        }
        else if(buttonClickManager?.getDestinationEntered() == true && buttonClickManager?.getNoteEntered() == true){
            setButtonColor(placeButton,R.color.green);
            setButtonColor(noteButton,R.color.green);
            setButtonColor(navButton,R.color.yellow);
        }
    }

}





