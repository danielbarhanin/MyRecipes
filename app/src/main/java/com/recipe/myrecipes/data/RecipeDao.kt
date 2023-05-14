package com.recipe.myrecipes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRecipe(recipe: Recipe)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("SELECT * FROM recipe_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipe_table WHERE id= :id")
    fun getRecipeById(id: Int): Recipe
}