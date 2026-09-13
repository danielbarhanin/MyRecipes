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

sealed class HomeListItem {
    data class Header(val titleResId: Int) : HomeListItem()
    data class RecipeItem(val recipe: Recipe) : HomeListItem()
}

class RecipesAdapter(
    var onDeleteRecipeCallback: ((Recipe) -> Unit),
    var onChangingOrder: ((recipes: List<Recipe>) -> Unit),
    var onStartDrag: ((RecyclerView.ViewHolder) -> Unit)? = null
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_RECIPE = 1
    }

    private var items = emptyList<HomeListItem>()

    fun setItems(list: List<HomeListItem>) {
        items = list
        notifyDataSetChanged()
    }

    fun setRecipes(recipes: List<Recipe>) {
        setItems(recipes.map { HomeListItem.RecipeItem(it) })
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeListItem.Header -> VIEW_TYPE_HEADER
            is HomeListItem.RecipeItem -> VIEW_TYPE_RECIPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.category_header_item, parent, false))
        } else {
            RecipeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is HomeListItem.RecipeItem -> (holder as RecipeViewHolder).bind(item.recipe)
        }
    }

    override fun getItemCount() = items.size

    fun onChangingOrder(fromPosition : Int, toPosition: Int) {
        if (fromPosition < items.size && toPosition < items.size) {
            val fromItem = items[fromPosition]
            val toItem = items[toPosition]
            if (fromItem is HomeListItem.RecipeItem && toItem is HomeListItem.RecipeItem) {
                val list = items.toMutableList()
                Collections.swap(list, fromPosition, toPosition)
                items = list
                notifyItemMoved(fromPosition, toPosition)
            }
        }
    }

    fun onFinishReorder() {
        val recipes = items.filterIsInstance<HomeListItem.RecipeItem>().map { it.recipe }
        onChangingOrder.invoke(recipes)
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTitle: AppCompatTextView = itemView.findViewById(R.id.headerTitle)

        fun bind(header: HomeListItem.Header) {
            headerTitle.text = itemView.context.getString(header.titleResId)
        }
    }

    inner class RecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val recipeName: AppCompatTextView = itemView.findViewById(R.id.recipeName)
        private val categoryBadge: AppCompatTextView = itemView.findViewById(R.id.categoryBadge)
        private val recipeRow: ConstraintLayout = itemView.findViewById(R.id.recipeRow)
        private val deleteButton: AppCompatImageView = itemView.findViewById(R.id.deleteButton)

        fun bind(recipe: Recipe) {
            val cat = recipe.getCategoryEnum()
            recipeName.text = recipe.name
            categoryBadge.text = itemView.context.getString(cat.stringResId)

            if (recipe.isReadOnly) {
                categoryBadge.visibility = View.GONE
                deleteButton.visibility = View.GONE
                deleteButton.setOnClickListener(null)
                recipeRow.setOnLongClickListener(null)
            } else {
                categoryBadge.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener {
                    onDeleteRecipeCallback.invoke(recipe)
                }
                recipeRow.setOnLongClickListener {
                    onStartDrag?.invoke(this)
                    true
                }
            }

            recipeRow.setOnClickListener {
                val action = RecipesFragmentDirections.actionRecipesFragmentToFragmentRecipe(recipe)
                itemView.findNavController().navigate(action)
            }
        }
    }
}