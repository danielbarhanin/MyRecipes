package com.recipe.myrecipes.data

import android.content.Context
import android.os.Parcelable
import com.recipe.myrecipes.R
import kotlinx.parcelize.Parcelize

enum class Category(val stringResId: Int) {
    MAIN_COURSE(R.string.category_main_course),
    SOUPS(R.string.category_soups),
    SALADS(R.string.category_salads),
    DESSERTS(R.string.category_desserts);

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
    val isReadOnly: Boolean = false
): Parcelable {

    fun getCategoryEnum(): Category {
        return Category.fromName(category)
    }

    fun getRecipeString(context: Context): String {
        return "*${name}*\n\n" +
                "*${context.getString(R.string.select_category)}:* ${context.getString(getCategoryEnum().stringResId)}\n\n" +
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