package com.example.setupformaps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.setupformaps.user.Commuter
import com.example.setupformaps.user.User

var user : User? = null;
class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        user = Commuter(this);

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

            //assigns button to send to note activity
        val sendNoteButton: Button = findViewById(R.id.note_button_id)
        sendNoteButton.setOnClickListener {
            val intent = Intent(this, NoteWriteActivity::class.java)
            startActivity(intent)
        }

    }
}


