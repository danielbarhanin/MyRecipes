package com.recipe.myrecipes.data

import android.content.Context
import android.os.Parcelable
import com.recipe.myrecipes.R
import kotlinx.parcelize.Parcelize

enum class Category(val stringResId: Int, val iconResId: Int, val svgAssetFileName: String) {
    MAIN_COURSE(R.string.category_main_course, R.drawable.ic_cat_main_course, "main_course.svg"),
    SOUPS(R.string.category_soups, R.drawable.ic_cat_soups, "soups.svg"),
    SALADS(R.string.category_salads, R.drawable.ic_cat_salads, "salads.svg"),
    DESSERTS(R.string.category_desserts, R.drawable.ic_cat_desserts, "desserts.svg");

    companion object {
        fun fromName(name: String?): Category {
            if (name == null) return MAIN_COURSE
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MAIN_COURSE
        }
    }
}

@Parcelize
data class Recipe(
    val id: String,
    val ingredients: List<String>,
    val name: String,
    val instructions: String,
    val urlLink: String,
    val viewOrder: Int = 0,
    val category: String = Category.MAIN_COURSE.name,
    val isReadOnly: Boolean = false,
    val imageUrl: String = "",
) : Parcelable {

    fun getCategoryEnum(): Category {
        return Category.fromName(category)
    }

    fun getRecipeString(context: Context): String {
        return "*$name*\n\n" +
                "*${context.getString(R.string.select_category)}:* ${context.getString(getCategoryEnum().stringResId)}\n\n" +
                "*${context.getString(R.string.ingredients)}*\n${getIngredientsString()}\n\n" +
                "*${context.getString(R.string.instructions)}*\n$instructions\n" +
                "\n $urlLink"
    }

    private fun getIngredientsString(): String {
        var ingredientStr = ""
        for (ingredient in ingredients) {
            ingredientStr += "- $ingredient\n"
        }

        return ingredientStr
    }
}
