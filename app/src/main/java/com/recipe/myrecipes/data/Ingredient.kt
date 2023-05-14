package com.recipe.myrecipes.data

import androidx.room.Entity

@Entity(tableName = "ingredient_table")
data class Ingredient(
    val name: String,
    val amount: String,
    val unit: String
)