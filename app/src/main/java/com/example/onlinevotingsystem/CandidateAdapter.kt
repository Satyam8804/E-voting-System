package com.example.onlinevotingsystem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView

class CandidateAdapter(private var candidateList: List<CandidateModel>) : RecyclerView.Adapter<CandidateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.candidate_items, parent, false)
        return CandidateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        val candidate = candidateList[position]
        holder.bind(candidate)
    }

    override fun getItemCount(): Int {
        return candidateList.size
    }
}

class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
    private val partyName: TextView = itemView.findViewById(R.id.partyTextView)
    private val photoImageView: CircleImageView = itemView.findViewById(R.id.profile)
    private val bg:ImageView = itemView.findViewById(R.id.background_image)



    fun bind(candidate: CandidateModel) {
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
    }
}
