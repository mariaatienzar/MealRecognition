package com.example.mealrecognition.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealrecognition.R
import com.example.mealrecognition.upload.receivers.RecognitionResults


class RecogAdapter: RecyclerView.Adapter<RecogAdapter.RecipeViewHolder> (){
    var arrSegmentationResponse = ArrayList<ArrayList<String>>()
    class  RecipeViewHolder(view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        return RecipeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recipe_item,parent, false))
    }

    fun setData(arrData: ArrayList<ArrayList<String>>){
        arrSegmentationResponse = arrData as ArrayList<ArrayList<String>>
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.tv_dish_name).text = arrSegmentationResponse[position].toString()
    }

    override fun getItemCount(): Int {
        return arrSegmentationResponse.size
    }

}