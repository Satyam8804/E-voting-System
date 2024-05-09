package com.example.onlinevotingsystem

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActiveStatsAdapter(private val activeElections: List<VotingModel>) :
    RecyclerView.Adapter<ActiveStatsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.electionTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.description)
        val candidateInfoTextView: TextView = itemView.findViewById(R.id.presidentialInfo)
        val viewButton: Button = itemView.findViewById(R.id.ViewButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.election_list_admin, parent, false) // Replace "election_list_voters" with your actual layout file
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val election = activeElections[position]
        holder.titleTextView.text = election.title

        holder.descriptionTextView.text = if (election.description.length > 100) {
            "${election.description.substring(0, 100)}..." // Display limited description
        } else {
            election.description // Display full description if it's shorter than 100 characters
        }

        holder.candidateInfoTextView.text = "${election.options.size} candidates" // Assuming options represent candidates
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                holder.viewButton.isEnabled = true
                holder.viewButton.setOnClickListener {
                    // Handle click event for the view button
                    val context = holder.itemView.context
                    val intent = Intent(context, ShowResults::class.java)
                    intent.putExtra("election_id", election.id)
                    intent.putStringArrayListExtra("options", ArrayList(election.options))
                    context.startActivity(intent)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return activeElections.size
    }
}