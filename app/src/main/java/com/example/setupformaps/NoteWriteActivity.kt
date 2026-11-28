package com.example.setupformaps

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat


/**
 * Simple activity, allows user to write and save a note to
 * remind themselves where they are going.
 *
 * The note input is saved to the user object attribute: commuterNote
 * */
class NoteWriteActivity : AppCompatActivity() {
    lateinit var noteInput: EditText;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_note_write)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        noteInput = findViewById(R.id.noteInput)

        val saveNote: Button = findViewById(R.id.saveNote)
        saveNote.setOnClickListener {
            Log.i("Main Activity", "destroy note")

            user!!.setCommuterNote(noteInput.getText().toString());
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        noteInput.setText(user!!.getCommuterNote());
    }
}