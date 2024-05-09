package com.example.onlinevotingsystem

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class VotingOptionAdapter(
    private val optionList: List<CandidateModel>,
    private val votingModel: VotingModel,
    private val electionId: String,
    private val user: User
) : RecyclerView.Adapter<VotingOptionViewHolder>() {

    private var isVoteCast = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VotingOptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.voting_option_item, parent, false)
        return VotingOptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: VotingOptionViewHolder, position: Int) {
        val candidate = optionList[position]
        holder.bind(candidate, votingModel, electionId, isVoteCast, user)
    }

    override fun getItemCount(): Int {
        return optionList.size
    }

    fun setVoteCast() {
        isVoteCast = true
        notifyDataSetChanged()
    }
}

class VotingOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
    private val partyTextView: TextView = itemView.findViewById(R.id.partyTextView)
    private val profileImageView: CircleImageView = itemView.findViewById(R.id.profile)
    private val voteButton: Button = itemView.findViewById(R.id.voteButton)
    private var isButtonClickable = true

    fun bind(
        candidate: CandidateModel,
        votingModel: VotingModel,
        electionId: String,
        isVoteCast: Boolean,
        user: User
    ) {
        nameTextView.text = candidate.name
        partyTextView.text = candidate.partyAffiliation

        voteButton.isEnabled = isButtonClickable

        Glide.with(itemView.context)
            .load(candidate.photoUrl)
            .placeholder(R.drawable.user)
            .error(R.drawable.user)
            .into(profileImageView)

        val userId = getCurrentUserId(itemView.context) ?: ""

        getUserVotingStatus(userId, electionId) { hasVoted ->
            voteButton.isEnabled = !hasVoted && !isVoteCast

            voteButton.setOnClickListener {
                if (hasVoted) {
                    showToast("You have already voted in this election.")
                } else {
                    val candidateId = candidate.id
                    val currentVoteCount = votingModel.votes[candidateId] ?: 0
                    votingModel.votes[candidateId] = currentVoteCount + 1
                    updateVoteCountInFirebase(candidateId, votingModel, electionId)
                    showToast("Voted for ${candidate.name}")
                    (itemView.context as? VoteCastListener)?.onVoteCast()
                    updateUserVotingStatus(itemView.context, electionId)
                }
            }
        }
    }

    private fun updateVoteCountInFirebase(
        candidateId: String,
        votingModel: VotingModel,
        electionId: String
    ) {
        val votesRef = FirebaseDatabase.getInstance().getReference("Elections").child(electionId)
            .child("votes")

        val currentVoteCount = votingModel.votes[candidateId] ?: 0
        votesRef.child(candidateId).setValue(currentVoteCount + 1)
            .addOnSuccessListener {
                // Vote count updated successfully
            }
            .addOnFailureListener { exception ->
                // Failed to update vote count
                showToast("Failed to vote: ${exception.message}")
            }
    }

    private fun updateUserVotingStatus(context: Context, electionId: String) {
        val userId = getCurrentUserId(context)
        userId?.let { id ->
            val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(id)
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentUser = dataSnapshot.getValue(User::class.java)
                    currentUser?.let { user ->
                        if (!user.hasVotedIn(electionId)) {
                            user.voteInVoting(electionId)

                            usersRef.setValue(user)
                                .addOnSuccessListener {
                                    // User object updated successfully
                                }
                                .addOnFailureListener { exception ->
                                    // Failed to update user object
                                    Log.e(
                                        "UpdateUser",
                                        "Error updating user object: ${exception.message}"
                                    )
                                }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                    Log.e("UpdateUser", "Error retrieving user object: ${databaseError.message}")
                }
            })
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentUserId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("userData", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userId", null)
    }

    private fun getUserVotingStatus(userId: String, electionId: String, callback: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentUser = dataSnapshot.getValue(User::class.java)
                val hasVoted = currentUser?.hasVotedIn(electionId) ?: false
                callback(hasVoted)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Log.e("UserVotingStatus", "Error retrieving user voting status: ${databaseError.message}")
                callback(false) // Default to false if there's an error
            }
        })
    }
}

interface VoteCastListener {
    fun onVoteCast()
}
