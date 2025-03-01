package com.recipe.myrecipes.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Recipe
import java.util.*

class RecipesAdapter(var onDeleteRecipeCallback: ((Recipe) -> Unit), var onChangingOrder: ((recipes: List<Recipe>) -> Unit)): RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    private var data = emptyList<Recipe>()

    fun setData(recipes: List<Recipe>) {
        data = recipes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        return RecipeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    fun onChangingOrder(fromPosition : Int, toPosition: Int) {
        Collections.swap(data, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun onFinishReorder() {
        onChangingOrder.invoke(data)
    }

    inner class RecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val recipeName: AppCompatTextView = itemView.findViewById(R.id.recipeName)
        private val recipeRow: ConstraintLayout = itemView.findViewById(R.id.recipeRow)
        private val deleteButton: AppCompatImageView = itemView.findViewById(R.id.deleteButton)

        fun bind(recipe: Recipe) {
            recipeName.text = recipe.name

            deleteButton.setOnClickListener {
              onDeleteRecipeCallback.invoke(recipe)
            }

            recipeRow.setOnClickListener {
                val action = RecipesFragmentDirections.actionRecipesFragmentToFragmentRecipe(recipe)
                itemView.findNavController().navigate(action)
            }
        }
    }
}