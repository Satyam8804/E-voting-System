package com.example.onlinevotingsystem

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.onlinevotingsystem.databinding.VotingStatsItemsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class VotingStatsAdapter(
    private var candidateList: List<CandidateModel> ,
    private val votingModel: VotingModel,
    private val electionId: String,
    ) : RecyclerView.Adapter<VotingStatsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VotingStatsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.voting_stats_items, parent, false)
        return VotingStatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: VotingStatsViewHolder, position: Int) {
        val candidate = candidateList[position]
        holder.bind(candidate,votingModel,electionId)
    }

    override fun getItemCount(): Int {
        return candidateList.size
    }
}

class VotingStatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
    private val partyName: TextView = itemView.findViewById(R.id.partyTextView)
    private val photoImageView: CircleImageView = itemView.findViewById(R.id.profile)
    private val voteCountTextView:TextView = itemView.findViewById(R.id.voteCount)
    fun bind(candidate: CandidateModel , votingModel: VotingModel , electionId: String) {
        nameTextView.text = candidate.name
        partyName.text = candidate.partyAffiliation

        Glide.with(itemView.context)
            .load(candidate.photoUrl)
            .apply(
                RequestOptions()
                    .error(R.drawable.user)
                    .placeholder(R.drawable.user)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            ).into(photoImageView)


        val databaseReference = FirebaseDatabase.getInstance().getReference("Elections").child(electionId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val election = dataSnapshot.getValue(VotingModel::class.java)
                val voteCount = election?.votes?.get(candidate.id) ?: 0
                voteCountTextView.text = voteCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("VotingStatsAdapter", "Error fetching vote count: ${databaseError.message}")
                // Handle error, e.g., display a toast or log the error
            }
        })
    }
}
