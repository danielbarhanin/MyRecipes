package com.recipe.myrecipes.data

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class RecipeViewModel(application: Application): AndroidViewModel(application) {

   val readAllData: LiveData<List<Recipe>>
   private val repository: RecipeRepository

   init {
       val recipeDao = RecipeDatabase.getDatabase(application).recipeDao()
       repository = RecipeRepository(recipeDao)
       readAllData = repository.readAllData
   }

   fun addRecipe(recipe: Recipe) {
       viewModelScope.launch(Dispatchers.IO) {
           repository.addRecipe(recipe)
       }
   }

   fun updateRecipe(recipe: Recipe) {
       viewModelScope.launch(Dispatchers.IO) {
           repository.updateRecipe(recipe)
       }
   }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecipe(recipe)
        }
    }

   fun getRecipeById (id: Int): Recipe {
        return repository.getRecipeById(id)
   }
}