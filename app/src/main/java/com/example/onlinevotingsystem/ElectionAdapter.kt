package com.example.onlinevotingsystem

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ElectionAdapter(private val votingList: List<VotingModel>, private val listener: OnSwitchClickListener) :
    RecyclerView.Adapter<ElectionAdapter.VotingViewHolder>() {

    inner class VotingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        val switchIsActive: Switch = itemView.findViewById(R.id.switchIsActive)
        init {
            switchIsActive.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onSwitchClick(adapterPosition, switchIsActive.isChecked)
            }
        }
    }

    interface OnSwitchClickListener {
        fun onSwitchClick(position: Int, isChecked: Boolean)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VotingViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.election_list, parent, false)
        return VotingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VotingViewHolder, position: Int) {
        val currentItem = votingList[position]
        holder.titleTextView.text = currentItem.title
        holder.descriptionTextView.text = currentItem.description
        holder.switchIsActive.isChecked = currentItem.active?: false
        if(currentItem.active?: false){
            holder.switchIsActive.setText("Active")
        }else{
            holder.switchIsActive.setText("Not Active")
        }
    }

    override fun getItemCount() = votingList.size
}