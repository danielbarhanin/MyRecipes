package com.recipe.myrecipes.add_recipe

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Ingredient

enum class Type {
    NAME, AMOUNT, UNIT
}

class IngredientsAdapter(): RecyclerView.Adapter<IngredientsAdapter.IngredientsViewHolder>() {

    private var data = mutableListOf<Ingredient>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientsViewHolder {
        return IngredientsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.ingredient_edit_item, parent, false))
    }

    override fun onBindViewHolder(holder: IngredientsViewHolder, position: Int) {
        holder.bind(data[position], position)
    }

    override fun getItemCount() = data.size

    fun getIngredients(): MutableList<Ingredient> {
        return data
    }

    fun addNewIngredient() {
        data.add(Ingredient("", "", ""))
        notifyDataSetChanged()
    }

    private fun deleteIngredient(position: Int) {
        data.removeAt(position)
        notifyDataSetChanged()
    }

    inner class IngredientsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var ingredientName: AppCompatEditText = itemView.findViewById(R.id.ingredientName)
//        private var ingredientsAmount: AppCompatEditText = itemView.findViewById(R.id.ingredientsAmount)
//        private var ingredientUnit: AppCompatEditText = itemView.findViewById(R.id.ingredientUnit)
        private var removeIngredientButton: AppCompatImageView = itemView.findViewById(R.id.removeIngredientButton)

        fun bind(ingredient: Ingredient, position: Int) {
            ingredientName.setText(ingredient.name)
//            ingredientsAmount.setText(ingredient.amount)
//            ingredientUnit.setText(ingredient.unit)

            ingredientName.addTextChangedListener(CustomTextWatcher(Type.NAME, position))
//            ingredientsAmount.addTextChangedListener(CustomTextWatcher(Type.AMOUNT, position))
//            ingredientUnit.addTextChangedListener(CustomTextWatcher(Type.UNIT, position))

            removeIngredientButton.setOnClickListener {
                deleteIngredient(position)
            }

        }
    }

    inner class CustomTextWatcher(var type: Type, private var position: Int): TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val name = data[position].name
            val amount = data[position].amount
            val unit = data[position].unit
            when(type) {
                Type.NAME -> {
                    data[position] = Ingredient(s.toString(), amount, unit)
                }
                Type.AMOUNT -> {
                    data[position] = Ingredient(name, s.toString(), unit)
                }
                Type.UNIT -> {
                    data[position] = Ingredient(name, amount, s.toString())
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }
}