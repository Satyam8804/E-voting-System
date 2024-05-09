package com.example.onlinevotingsystem

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID

class AddCandidate : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageUri: Uri
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: FirebaseDatabase
    val partyList = mutableListOf<String>()
    val electionList = mutableListOf<String>()
    private lateinit var profile : CircleImageView
    private lateinit var partySpinner: Spinner
    private lateinit var electionSpinner: Spinner
    private var profileUri: Uri? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_candidate)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Add Candidate"
        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance()

        val editTextCandidateName = findViewById<EditText>(R.id.editTextCandidateName)
        partySpinner = findViewById<Spinner>(R.id.spinnerPartyType)
        electionSpinner = findViewById<Spinner>(R.id.spinnerElectionType)
        val buttonAddCandidate = findViewById<Button>(R.id.buttonAddCandidateAndElection)

        profile = findViewById(R.id.profile)
        fetchPartyNames()
        fetchElectionTypes()

        profile.setOnClickListener{
            openFileChooser()
        }

        buttonAddCandidate.setOnClickListener{
            val candidateName = editTextCandidateName.text.toString()
            val party = partySpinner.selectedItem.toString()
            val elections = electionSpinner.selectedItem.toString()
            uploadImageAndAddData(candidateName,party,elections)
        }

    }

    private fun openFileChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun fetchPartyNames() {

        databaseReference.getReference("Party").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (partySnapshot in dataSnapshot.children) {
                    val partyName = partySnapshot.child("name").getValue(String::class.java)
                    partyName?.let { partyList.add(it) }
                }

                // Populate party names into Spinner
                val adapter = ArrayAdapter(this@AddCandidate, android.R.layout.simple_spinner_item, partyList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                partySpinner.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun fetchElectionTypes() {
        databaseReference.getReference("Elections").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (electionSnapshot in dataSnapshot.children) {
                    val electionType = electionSnapshot.child("title").getValue(String::class.java)
                    electionType?.let { electionList.add(it) }
                }
                // Populate election types into Spinner
                val adapter = ArrayAdapter(this@AddCandidate, android.R.layout.simple_spinner_item, electionList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                electionSpinner.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
    private fun uploadImageAndAddData(candidateName: String, partyType: String, electionType: String) {
        val storageRef = Firebase.storage.reference.child("Candidates/${candidateName}_${System.currentTimeMillis()}")


        val uploadTask = storageRef.putFile(profileUri!!)

        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Logo upload successful, get download URL
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageURL = uri.toString()
                    // Add candidate and election data to Firebase Database
                    val candidateId = databaseReference.getReference("Candidates").push().key
                    val candidate = CandidateModel(candidateId ?: "", candidateName, partyType, imageURL, electionType)

                    candidateId?.let {
                        databaseReference.getReference("Candidates").child(it).setValue(candidate)
                    }

                    Toast.makeText(this, "Candidate added successfully", Toast.LENGTH_SHORT).show()
                    updateOptionsInVotingModel(candidateId.toString(), electionType)

                }.addOnFailureListener{e ->
                    Toast.makeText(this, "Failed to add Candidate: $e", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            profileUri = data.data
            profile.setImageURI(profileUri)
            profile.visibility = ImageView.VISIBLE
        }
    }

    private fun updateOptionsInVotingModel(candidateId: String, electionType: String) {
        databaseReference.getReference("Elections").orderByChild("title").equalTo(electionType)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (votingSnapshot in dataSnapshot.children) {
                        val votingId = votingSnapshot.key
                        val votingModel = votingSnapshot.getValue(VotingModel::class.java)

                        votingModel?.let {
                            val options = it.options.toMutableList()
                            options.add(candidateId)
                            databaseReference.getReference("Elections").child(votingId!!)
                                .child("options").setValue(options)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }

}