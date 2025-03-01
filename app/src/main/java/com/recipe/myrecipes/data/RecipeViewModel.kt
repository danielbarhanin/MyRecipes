package com.recipe.myrecipes.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.util.UUID



class RecipeViewModel: ViewModel() {
    val recipes: MutableLiveData<List<Recipe>> = MutableLiveData<List<Recipe>>()

    val database = FirebaseDatabase.getInstance(DATABASE_URL_TEST)

    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private fun getRecipeMap(recipe: Recipe): Map<String, Any> {
        return mapOf(
            NAME to recipe.name,
            INGREDIENTS to recipe.ingredients,
            INSTRUCTIONS to recipe.instructions,
            URL_LINK to recipe.urlLink,
            VIEW_ORDER to recipe.viewOrder
        )
    }

    private fun toRecipes(dataSnapshot: DataSnapshot): List<Recipe> {
        val recipes: MutableList<Recipe> = mutableListOf()

        for (recipeData in dataSnapshot.children) {
            val id = recipeData.key ?: UUID.randomUUID().toString()

            recipes.add(
                Recipe(
                    id,
                    recipeData.child(INGREDIENTS).getValue<List<String>>() ?: listOf(),
                    recipeData.child(NAME).getValue<String>() ?: "",
                    recipeData.child(INSTRUCTIONS).getValue<String>() ?: "",
                    recipeData.child(URL_LINK).getValue<String>() ?: "",
            recipeData.child(VIEW_ORDER).getValue<Int>() ?: 0,
                )
            )
        }

        return recipes
    }

   fun getRecipes(): LiveData<List<Recipe>> {
       database.getReference("$ROOT/$userId")
           .addValueEventListener(object : ValueEventListener {
               override fun onDataChange(dataSnapshot: DataSnapshot) {
                   if (dataSnapshot.exists()) {
                       recipes.postValue(toRecipes(dataSnapshot))
                   }
               }

               override fun onCancelled(error: DatabaseError) {}
           }
       )
       return recipes
   }

   fun addRecipe(recipe: Recipe): Task<Void> {
       val rootRef = database.getReference(ROOT)

       return rootRef.child(userId).child(UUID.randomUUID().toString()).setValue(getRecipeMap(recipe))
   }

   fun updateRecipe(recipe: Recipe): Task<Void> {
       val rootRef = database.getReference(ROOT)

       return rootRef.child(userId).child(recipe.id).updateChildren(getRecipeMap(recipe))
   }
    fun deleteRecipe(recipe: Recipe): Task<Void>  {
        val rootRef = database.getReference("$ROOT/$userId")

        val mTask: Task<Void>
        // Don't delete the whole reference of userId
        if (recipes.value?.size == 1) {
            mTask = rootRef.child(recipe.id).setValue(null)
            rootRef.setValue("")
        } else {
            mTask =  rootRef.child(recipe.id).removeValue()
        }

        return mTask
    }
}