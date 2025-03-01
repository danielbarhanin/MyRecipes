package com.recipe.myrecipes.data

import android.content.Context
import android.os.Parcelable
import com.recipe.myrecipes.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Recipe(
    val id: String,
    val ingredients: List<String>,
    val name: String,
    val instructions: String,
    val urlLink: String,
    val viewOrder: Int = 0,

    ): Parcelable {

    fun getRecipeString(context: Context): String {
        return "*${name}*\n\n" +
                "*${context.getString(R.string.ingredients)}*\n${getIngredientsString()}\n\n" +
                "*${context.getString(R.string.instructions)}*\n${instructions}\n" +
                "\n $urlLink"
    }

    private fun getIngredientsString() : String {
        var ingredientStr = ""
        for (ingredient in ingredients) {
            ingredientStr += "- $ingredient\n"
        }

        return ingredientStr
    }
}