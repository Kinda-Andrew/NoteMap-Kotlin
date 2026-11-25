package com.example.setupformaps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.setupformaps.application.DatabaseManager
import com.example.setupformaps.application.Permissions
import com.example.setupformaps.user.Commuter
import com.example.setupformaps.user.User
import com.google.android.libraries.navigation.NavigationFragment
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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

        CONTEXT = applicationContext;
        if (user == null) user = Commuter();

        Permissions.request(Manifest.permission.POST_NOTIFICATIONS, 0, this)

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //assigns button to send to map activity
        val sendMapButton: Button = findViewById(R.id.map_button_id)
        sendMapButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        val sendNavButton: Button = findViewById(R.id.navigation_button_id)
        sendNavButton.setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java)
            startActivity(intent)


        }
    }
}


