package com.recipe.myrecipes.data

import android.content.Context
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.recipe.myrecipes.R
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "recipe_table")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val ingredients: List<String>,
    val name: String,
    val instructions: String,
    val urlLink: String,
    val view_order: Int = 0,

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