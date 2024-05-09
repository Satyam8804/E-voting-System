package com.example.onlinevotingsystem

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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

class ShowElectionList : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var votingAdapter: ElectionAdapter
    private var electionList = mutableListOf<VotingModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_election_list)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Election Lists"
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        votingAdapter = ElectionAdapter(electionList, object : ElectionAdapter.OnSwitchClickListener {
            override fun onSwitchClick(position: Int, isChecked: Boolean) {
                // Handle switch event
                onSwitchClicked(position, isChecked)
            }
        })

        recyclerView.adapter = votingAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("Elections")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                electionList.clear()
                for (votingSnapshot in dataSnapshot.children) {
                    val votingModel = votingSnapshot.getValue(VotingModel::class.java)
                    votingModel?.let { electionList.add(it) }
                }
                votingAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun onSwitchClicked(position: Int, isChecked: Boolean) {
        // Check if position is valid
        if (position >= 0 && position < electionList.size) {
            // Update the isActive status of the corresponding VotingModel
            val votingId = electionList[position].id
            databaseReference.child(votingId).child("active").setValue(isChecked)
                .addOnSuccessListener {
                    Toast.makeText(this, "isActive status updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to update isActive status: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Invalid position", Toast.LENGTH_SHORT).show()
        }
    }
}
