package com.example.onlinevotingsystem

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.onlinevotingsystem.R
import com.example.onlinevotingsystem.VotingModel
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class AddElections : AppCompatActivity() {
    private lateinit var databaseReference: FirebaseDatabase
    private lateinit var editTextTitle : EditText
    private lateinit var editTextDescription :EditText
    private lateinit var buttonAddElection :Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_elections)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title ="Add Election"

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        buttonAddElection = findViewById(R.id.buttonAddElection)

        databaseReference = FirebaseDatabase.getInstance()

        buttonAddElection.setOnClickListener {
            // Get text from EditText fields
            val title = editTextTitle.text.toString().trim()
            val description = editTextDescription.text.toString().trim()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                addDataToDatabase(title, description)
            } else {
                showToast("Please fill in all fields")
            }
        }
    }

    private fun addDataToDatabase(title: String, desc: String) {
        val electionId = UUID.randomUUID().toString()
        val electionRef = databaseReference.getReference("Elections").child(electionId)

        val votingModel = VotingModel(id = electionId, title = title, description = desc, active = false)

        electionRef.setValue(votingModel)
            .addOnSuccessListener {
                showToast("Election added successfully")
                editTextTitle.setText("")
                editTextDescription.setText("")

            }
            .addOnFailureListener { exception ->
                showToast("Failed to add election: ${exception.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
