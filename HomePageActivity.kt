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
import com.example.setupformaps.application.Permissions
import com.example.setupformaps.user.Commuter
import com.example.setupformaps.user.User
import com.google.android.libraries.navigation.NavigationFragment

var user : User? = null;
lateinit var CONTEXT : Context;

//Main Activity that opens on launch of the app
//Eventually will contain options for commuter or caretaker
//Once a route is finished, the commuter is returned here
class HomePageActivity : AppCompatActivity() {

    private lateinit var navFragment: NavigationFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (user == null) user = Commuter(this);
        CONTEXT = applicationContext;

        Permissions.request(Manifest.permission.POST_NOTIFICATIONS, 2, this)

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
        val sendWebButton: Button = findViewById(R.id.web_button_id)
        sendWebButton.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            startActivity(intent)
        }
    }
}


