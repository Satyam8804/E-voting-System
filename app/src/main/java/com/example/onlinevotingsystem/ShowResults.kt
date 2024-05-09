package com.example.onlinevotingsystem

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowResults : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var votingModel: VotingModel
    private lateinit var adapter: VotingStatsAdapter
    private lateinit var user: User
    private lateinit var recyclerView: RecyclerView
    private lateinit var publishResult:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_results)
        setupActionBar()
        setupRecyclerView()
        fetchVotingOptions()
        publishResult = findViewById(R.id.publishResult)
        publishResult.setOnClickListener {
            // Set isPublished to true in the Firebase Realtime Database
            val electionId = intent.getStringExtra("election_id").toString()
            val votingModelRef = FirebaseDatabase.getInstance().getReference("Elections").child(electionId)
            votingModelRef.child("isPublished").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Results published successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to publish results!", Toast.LENGTH_SHORT).show()
                    Log.e("ShowResults", "Error publishing results: ${it.message}")
                }
        }

    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@ShowResults, R.color.primary)))
            setDisplayHomeAsUpEnabled(true)
            title = "Polling Live Stats"
        }
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)

    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchVotingOptions() {
        val options = intent.getStringArrayListExtra("options")
        val electionId = intent.getStringExtra("election_id").toString()

        if (!options.isNullOrEmpty()) {
            val candidateModels = mutableListOf<CandidateModel>()
            databaseReference = FirebaseDatabase.getInstance().getReference("Candidates")
            for (candidateId in options) {
                fetchCandidateDetails(candidateId) { candidate ->
                    candidate?.let {
                        candidateModels.add(it)
                        if (candidateModels.size == options.size) {
                            votingModel = VotingModel(options = options, votes = mutableMapOf())
                            adapter = VotingStatsAdapter(candidateModels, votingModel ,electionId )
                            recyclerView.adapter = adapter
                        }
                    }
                }
            }
        } else {
            handleError("No voting options available.")
        }
    }

    private fun fetchCandidateDetails(candidateId: String, callback: (CandidateModel?) -> Unit) {
        databaseReference.child(candidateId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val candidate = dataSnapshot.getValue(CandidateModel::class.java)
                callback(candidate)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ShowResults", "Error fetching candidate details: ${databaseError.message}")
                callback(null)
            }
        })
    }

    private fun handleError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("ShowResults", "Error: $message")
        finish()
    }
}
