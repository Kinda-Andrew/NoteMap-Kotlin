package com.example.setupformaps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DestinationWriteActivity : AppCompatActivity() {
    lateinit var destinationInput: EditText;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_write_destination)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        destinationInput = findViewById(R.id.destinationInput)

        val saveStringDestination: Button = findViewById(R.id.saveStringDestination)
        saveStringDestination.setOnClickListener {
            user!!.setStringDestination(destinationInput.getText().toString());
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        destinationInput.setText(user!!.getStringDestination());
    }
}