package com.example.setupformaps.application

import android.animation.ObjectAnimator
import android.widget.Button
import com.example.setupformaps.R
import com.example.setupformaps.databinding.ActivityMainBinding

class ButtonClickManager {

    private var noteEntered = false
    private var destinationEntered = false

    fun setNoteEntered(boolean: Boolean){noteEntered = boolean}
    fun getNoteEntered(): Boolean {return noteEntered}

    fun setDestinationEntered(boolean: Boolean){destinationEntered = boolean}
    fun getDestinationEntered(): Boolean {return destinationEntered}


    /**
     * checks which tasks have been done already
     * ie the user should not be able to click on navigation before setting a
     * destination. When a button is clicked, it will check if it is allowed to be clicked
     * If it is, normal behavior. If not, flash or animate the button of the task that needs to
     * be done first
     */
    fun isClickAllowed(button : Button): Boolean {

        /**
         * navigation requires both a note and a destination
         * to be set.
         */
        if(button.id == R.id.navigation_button_id){
            if(noteEntered && destinationEntered){
                return true
            }
        }
        /**
         * place should be entered first, no prerequisite.
         */
        else if(button.id == R.id.place_button_id){
            return true;
        }
        /**
         * note should only be entered if destination is set
         */
        else if(button.id == R.id.note_button_id){
            if(destinationEntered){
                return true
            }
        }
        /**
         * if none of the button id's match, this function should not be called
         * return false to be safe/consistency
         */

        return false;


    }

    fun animateButton(buttonToAnimate : Button){

        ObjectAnimator.ofFloat(
            buttonToAnimate,
            "translationX",
            0f, -25f, 25f, -20f, 20f, -10f, 10f, -5f, 5f, 0f
        ).apply {
            duration = 500
            start()
        }
    }

    }



