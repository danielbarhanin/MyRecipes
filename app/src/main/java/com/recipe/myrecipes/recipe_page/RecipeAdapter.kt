package com.recipe.myrecipes.recipe_page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Recipe
import com.recipe.myrecipes.recipe_page.RecipeAdapter.ViewType.Ingredients
import com.recipe.myrecipes.recipe_page.RecipeAdapter.ViewType.Instructions
import com.recipe.myrecipes.recipe_page.RecipeAdapter.ViewType.Link

class RecipeAdapter(private val recipeItems: MutableList<RecipeItem>): RecyclerView.Adapter<RecipeItemViewHolder>() {

    object ViewType {
        const val Instructions = 0
        const val Link = 1
        const val Ingredients = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeItemViewHolder {
        return when (viewType) {
            Instructions -> {
                RecipeItemViewHolder.InstructionsViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.instructions_item, parent, false)
                )
            }
            Link -> {
                RecipeItemViewHolder.LinkViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.link_item, parent, false)
                )
            }
            else -> {
                RecipeItemViewHolder.IngredientsViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.ingredients_container, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecipeItemViewHolder, position: Int) {
        when(holder) {
            is RecipeItemViewHolder.InstructionsViewHolder -> {
                val item = recipeItems[position]
                item as RecipeItem.Instructions
                holder.bind(item.body)
            }
            is RecipeItemViewHolder.LinkViewHolder -> {
                val item = recipeItems[position]
                item as RecipeItem.Link
                holder.bind(item.recipe)
            }
            is RecipeItemViewHolder.IngredientsViewHolder -> {
                val item = recipeItems[position]
                item as RecipeItem.Ingredients
                holder.bind(item.ingredients)
            }
        }
    }

    override fun getItemCount() = recipeItems.size

    override fun getItemViewType(position: Int): Int {
        return when (recipeItems[position]) {
            is RecipeItem.Instructions -> Instructions
            is RecipeItem.Link -> Link
            is RecipeItem.Ingredients -> Ingredients
        }
    }
}

sealed class RecipeItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    class InstructionsViewHolder(view: View): RecipeItemViewHolder(view) {
        private val instructionsTextView: AppCompatTextView = view.findViewById(R.id.instructions)

        fun bind(instructions: String) {
            instructionsTextView.text = instructions
        }
    }

    class IngredientsViewHolder(view: View): RecipeItemViewHolder(view) {
        private val container: LinearLayoutCompat = view.findViewById(R.id.container)

        fun bind(ingredients: List<String>) {
            ingredients.forEach {
                val layout = LayoutInflater.from(itemView.context).inflate(R.layout.ingredient_item, container, false)
                val ingredientName: AppCompatTextView = layout.findViewById(R.id.ingredientName)
                ingredientName.text = it
                container.addView(layout)
            }
        }
    }

    class LinkViewHolder(view: View): RecipeItemViewHolder(view) {
        private val linkTextView: AppCompatTextView = view.findViewById(R.id.link)

        fun bind(recipe: Recipe) {
           linkTextView.text = recipe.urlLink

            linkTextView.setOnClickListener {
                val action = RecipePageFragmentDirections.actionRecipePageFragmentToRecipeWebFragment(recipe)
                itemView.findNavController().navigate(action)
            }
        }
    }
}