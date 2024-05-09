package com.example.onlinevotingsystem

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class RegistrationAdapter(private val registrations: List<Registration>) :
    RecyclerView.Adapter<RegistrationAdapter.RegistrationViewHolder>() {

    inner class RegistrationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewUserId: TextView = itemView.findViewById(R.id.textViewUserId)
        val textViewAge: TextView = itemView.findViewById(R.id.textViewAge)
        val textViewVoterId: TextView = itemView.findViewById(R.id.textViewVoterId)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        val buttonVerify: Button = itemView.findViewById(R.id.buttonVerify)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistrationViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.voter_reg_items, parent, false)
        return RegistrationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RegistrationViewHolder, position: Int) {
        val currentItem = registrations[position]

        holder.textViewUserId.text = "User ID: ${currentItem.userId}"
        holder.textViewAge.text = "Age: ${currentItem.age}"
        holder.textViewVoterId.text = "Voter ID: ${currentItem.voterId}"
        holder.textViewStatus.text = "Status: ${if (currentItem.isVerified) "Verified" else "Pending"}"
        if (currentItem.isVerified) {
            holder.buttonVerify.isEnabled = false
            holder.buttonVerify.text = "Verified"
        } else {
            holder.buttonVerify.isEnabled = true
            holder.buttonVerify.text = "Verify"
        }
        holder.buttonVerify.setOnClickListener {
            val currentItem = registrations[position]
            currentItem.isVerified = true

            // Notify the adapter of the change
            notifyItemChanged(position)

            // You can also update the Firebase database with the new status
            val database = FirebaseDatabase.getInstance()
            val reference = database.getReference("VoterRegistrations")
            reference.child(currentItem.userId).setValue(currentItem)
                .addOnSuccessListener {
                    showToast("Verification successful!",holder.itemView.context )
                }
                .addOnFailureListener { exception ->
                    showToast("Verification failed: ${exception.message}" ,holder.itemView.context)
                }
        }
    }
    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun getItemCount() = registrations.size
}
