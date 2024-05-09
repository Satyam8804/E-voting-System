package com.example.onlinevotingsystem

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID

class AddParty : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private var logoUri: Uri? = null
    private val db = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_party)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Add Party"
        val editTextPartyName = findViewById<EditText>(R.id.editTextPartyName)
        val imageViewPartyLogo = findViewById<CircleImageView>(R.id.profile)
        val buttonAddParty = findViewById<Button>(R.id.buttonAddParty)

        imageViewPartyLogo.setOnClickListener {
            openFileChooser()
        }

        buttonAddParty.setOnClickListener {
            val partyName = editTextPartyName.text.toString().trim()
            if (partyName.isEmpty() || logoUri == null) {
                // Handle validation error
                return@setOnClickListener
            }

            // Upload logo to Firebase Storage
            uploadLogoToFirebaseStorage(partyName, logoUri!!)
        }
    }

    private fun openFileChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            logoUri = data.data
            val imageViewPartyLogo = findViewById<CircleImageView>(R.id.profile)
            imageViewPartyLogo.setImageURI(logoUri)
            imageViewPartyLogo.visibility = ImageView.VISIBLE
        }
    }

    private fun uploadLogoToFirebaseStorage(partyName: String, logoUri: Uri) {
        val storageRef = Firebase.storage.reference.child("party_logos/${partyName}_${System.currentTimeMillis()}")
        val uploadTask = storageRef.putFile(logoUri)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Logo upload successful, get download URL
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val logoUrl = uri.toString()
                     addPartyToDatabase(partyName, logoUrl)
                }
            } else {
                // Handle logo upload failure
            }
        }
    }

    private fun addPartyToDatabase(partyName: String, logoUrl: String) {
        val partyRef = db.getReference("Party").child(UUID.randomUUID().toString())

        val party = hashMapOf(
            "name" to partyName,
            "logoUrl" to logoUrl
        )
        partyRef.setValue(party)
            .addOnSuccessListener {
                // Party added successfully
                println("Party added successfully")
                Toast.makeText(this, "${partyName} Added Successfully !!",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle failure
                println("Error adding party: $e")
            }
    }

}