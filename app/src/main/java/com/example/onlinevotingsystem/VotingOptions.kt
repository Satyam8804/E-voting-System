package com.example.onlinevotingsystem

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class VotingOptions : AppCompatActivity(), VoteCastListener {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var votingModel: VotingModel
    private lateinit var adapter: VotingOptionAdapter
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_voting_options)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Polling Lists"

        databaseReference = FirebaseDatabase.getInstance().getReference("Candidates")

        val options = intent.getStringArrayListExtra("options")
//        Toast.makeText(this,"${options}",Toast.LENGTH_SHORT).show()
        val electionId = intent.getStringExtra("election_id").toString()
        if (!options.isNullOrEmpty()) {
            val candidateModels = mutableListOf<CandidateModel>()
            for (candidateId in options) {
                fetchCandidateDetails(candidateId) { candidate ->
                    candidate?.let {
                        candidateModels.add(it)
                        if (candidateModels.size == options.size) {
                            votingModel = VotingModel(options = options, votes = mutableMapOf())
                            val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            user = User()
                            adapter = VotingOptionAdapter(candidateModels, votingModel, electionId , user)
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
        databaseReference.child(candidateId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val candidate = dataSnapshot.getValue(CandidateModel::class.java)
                callback(candidate)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("VotingOptions", "Error fetching candidate details: ${databaseError.message}")
                callback(null)
            }
        })
    }

    private fun handleError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("VotingOptions", "Error: $message")
        finish()
    }

    override fun onVoteCast() {
        adapter.setVoteCast()
    }
}
