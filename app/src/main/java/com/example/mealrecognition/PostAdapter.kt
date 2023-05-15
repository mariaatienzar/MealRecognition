package com.example.mealrecognition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealrecognition.upload.receivers.SegmentationResponse

class PostAdapter (private val postModel: MutableList<SegmentationResponse>): RecyclerView.Adapter<PostViewHolder>() {
    override fun onCreateViewHolder(parent:ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_intake_info, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        return holder.bindView(postModel[position])
    }


    override fun getItemCount(): Int {
        return postModel.size
    }
}


class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    //private val foodname: TextView = itemView.findViewById(R.id.food_name)
    private val foodfam: TextView = itemView.findViewById(R.id.foodfam)
    private val foodType: TextView = itemView.findViewById(R.id.foodtype)
    private val occasion: TextView = itemView.findViewById(R.id.occasion)
    private val segmen: TextView = itemView.findViewById(R.id.segment)

    fun bindView(postModel: SegmentationResponse){
        foodfam.text = postModel.foodFamily.toString()
        foodType.text = postModel.foodType.toString()
        occasion.text = postModel.occasion
        segmen.text = postModel.segmentation_results.toString()
    }
}