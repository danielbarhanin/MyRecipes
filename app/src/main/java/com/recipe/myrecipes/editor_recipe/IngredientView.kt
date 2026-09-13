package com.recipe.myrecipes.editor_recipe

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.recipe.myrecipes.R

class IngredientView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val position: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    var onDeleteIngredient: ((Int) -> Unit)? = null

    private val layout = LayoutInflater.from(context).inflate(R.layout.ingredient_edit_item, this, true)
    private val deleteButton: AppCompatImageView = layout.findViewById(R.id.removeIngredientButton)
    val ingredientNameEditText: AppCompatEditText = layout.findViewById(R.id.ingredientName)

    init {
        deleteButton.setOnClickListener {
            onDeleteIngredient?.invoke(position)
        }
    }

    fun getIngredient(): String = ingredientNameEditText.text.toString()

    fun initializeIngredient(ingredientStr: String) = ingredientNameEditText.setText(ingredientStr)
}
