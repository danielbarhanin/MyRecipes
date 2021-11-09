package com.recipes.myrecipes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipeAdapter(private var recipeList: MutableList<String>): RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecipeAdapter.RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeAdapter.RecipeViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = recipeList.size

    fun addRecipe(recipeHeader: String){
        recipeList.add(recipeHeader)
        notifyDataSetChanged()
    }

    inner class RecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        var recipePosition = itemView.findViewById<TextView>(R.id.recipePosition)
        var recipeHeader = itemView.findViewById<TextView>(R.id.recipeHeader)

        fun bind(position: Int){
            recipePosition.text = (position + 1).toString()
            recipeHeader.text = recipeList[position]
        }

    }
}