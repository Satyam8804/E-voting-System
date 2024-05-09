package com.example.onlinevotingsystem



import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class PublishResult : AppCompatActivity() {

    private lateinit var spinnerElection: Spinner
    private lateinit var recyclerViewVotingStats: RecyclerView
    private lateinit var votingStatsAdapter: VotingStatsAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var electionId: String
    private val candidatesList = mutableListOf<CandidateModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_result)

        spinnerElection = findViewById(R.id.spinner)
        recyclerViewVotingStats = findViewById(R.id.recycler_voting_stats)
        recyclerViewVotingStats.layoutManager = LinearLayoutManager(this)

        databaseReference = FirebaseDatabase.getInstance().getReference("Elections")
        fetchElectionTitles()

        spinnerElection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTitle = parent?.getItemAtPosition(position).toString()
                fetchVotingStats(selectedTitle)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle nothing selected
            }
        }
    }

    private fun fetchElectionTitles() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val electionTitles = mutableListOf<String>()
                for (electionSnapshot in dataSnapshot.children) {
                    val title = electionSnapshot.child("title").getValue(String::class.java)
                    title?.let {
                        electionTitles.add(it)
                    }
                }
                val adapter = ArrayAdapter(this@PublishResult, android.R.layout.simple_spinner_item, electionTitles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerElection.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun fetchVotingStats(selectedTitle: String) {
        databaseReference.orderByChild("title").equalTo(selectedTitle).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear existing data
                candidatesList.clear()

                for (electionSnapshot in dataSnapshot.children) {
                    val isPublished = electionSnapshot.child("isPublished").getValue(Boolean::class.java)
                    if (isPublished == true) {
                        electionId = electionSnapshot.key ?: ""
                        val votingModel = electionSnapshot.getValue(VotingModel::class.java)
                        val candidatesSnapshot = electionSnapshot.child("votes")
                        for (candidateSnapshot in candidatesSnapshot.children) {
                            val candidateId = candidateSnapshot.key ?: ""
                            // Fetch candidate details from another node
                            val candidateDetailsSnapshot = FirebaseDatabase.getInstance().getReference("Candidates").child(candidateId)
                            candidateDetailsSnapshot.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(candidateDataSnapshot: DataSnapshot) {
                                    val candidate = candidateDataSnapshot.getValue(CandidateModel::class.java)
                                    candidate?.let {
                                        // Update the vote count
                                        candidatesList.add(it)
                                        // Initialize the adapter if not initialized
                                        if (!::votingStatsAdapter.isInitialized) {
                                            votingStatsAdapter = VotingStatsAdapter(candidatesList, votingModel!!, electionId)
                                            recyclerViewVotingStats.adapter = votingStatsAdapter
                                        } else {
                                            // Notify the adapter after adding each candidate
                                            votingStatsAdapter.notifyDataSetChanged()
                                        }
                                    }
                                }

                                override fun onCancelled(candidateDatabaseError: DatabaseError) {
                                    // Handle error
                                }
                            })
                        }
                        return // Exit the loop after fetching voting stats for the selected election
                    }else{
                        Toast.makeText(applicationContext,"Result Is Not Published Yet !!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

}