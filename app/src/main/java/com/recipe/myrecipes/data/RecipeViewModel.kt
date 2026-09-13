package com.recipe.myrecipes.data

import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class RecipeViewModel : ViewModel() {
    val recipes: MutableLiveData<List<Recipe>> = MutableLiveData<List<Recipe>>()
    val exploreRecipes: MutableLiveData<List<Recipe>> = MutableLiveData<List<Recipe>>()
    val isLoadingRecipes: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
    val isLoadingExplore: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)

    val database: FirebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL_TEST)

    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private fun getRecipeMap(recipe: Recipe): Map<String, Any> {
        return mapOf(
            NAME to recipe.name,
            INGREDIENTS to recipe.ingredients,
            INSTRUCTIONS to recipe.instructions,
            URL_LINK to recipe.urlLink,
            VIEW_ORDER to recipe.viewOrder,
            CATEGORY to recipe.category,
            IMAGE_URL to recipe.imageUrl,
        )
    }

    private fun toRecipes(dataSnapshot: DataSnapshot): List<Recipe> {
        val recipesList: MutableList<Recipe> = mutableListOf()

        for (recipeData in dataSnapshot.children) {
            val id = recipeData.key ?: UUID.randomUUID().toString()

            val ingredientsList: List<String> = when (val rawIngredients = recipeData.child(INGREDIENTS).value) {
                is List<*> -> rawIngredients.mapNotNull { it?.toString() }
                is String -> listOf(rawIngredients)
                else -> emptyList()
            }

            recipesList.add(
                Recipe(
                    id = id,
                    ingredients = ingredientsList,
                    name = recipeData.child(NAME).getValue<String>() ?: "",
                    instructions = recipeData.child(INSTRUCTIONS).getValue<String>() ?: "",
                    urlLink = recipeData.child(URL_LINK).getValue<String>() ?: "",
                    viewOrder = recipeData.child(VIEW_ORDER).getValue<Int>() ?: 0,
                    category = recipeData.child(CATEGORY).getValue<String>() ?: Category.MAIN_COURSE.name,
                    imageUrl = recipeData.child(IMAGE_URL).getValue<String>() ?: "",
                ),
            )
        }

        return recipesList
    }

    fun getRecipes(): LiveData<List<Recipe>> {
        if (userId.isEmpty()) {
            recipes.postValue(emptyList())
            isLoadingRecipes.postValue(false)
            return recipes
        }

        isLoadingRecipes.postValue(true)
        database.getReference("$ROOT/$userId")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            recipes.postValue(toRecipes(dataSnapshot))
                        } else {
                            recipes.postValue(emptyList())
                        }
                        isLoadingRecipes.postValue(false)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        isLoadingRecipes.postValue(false)
                    }
                },
            )
        return recipes
    }

    fun getExploreRecipes(): LiveData<List<Recipe>> {
        isLoadingExplore.postValue(true)
        database.getReference(ROOT)
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val myRecipeIds = recipes.value?.asSequence()?.map { it.id }?.toSet() ?: emptySet()
                            val myRecipeNames = recipes.value?.asSequence()?.map { it.name.trim().lowercase() }?.toSet() ?: emptySet()

                            val exploreList: MutableList<Recipe> = mutableListOf()
                            for (userSnapshot in dataSnapshot.children) {
                                val otherUserId = userSnapshot.key
                                if ((otherUserId != null) && (otherUserId != userId)) {
                                    for (recipeData in userSnapshot.children) {
                                        val id = recipeData.key ?: UUID.randomUUID().toString()
                                        val name = recipeData.child(NAME).getValue<String>() ?: ""

                                        if ((id !in myRecipeIds) && (name.trim().lowercase() !in myRecipeNames)) {
                                            val ingredientsList: List<String> = when (val rawIngredients = recipeData.child(INGREDIENTS).value) {
                                                is List<*> -> rawIngredients.mapNotNull { it?.toString() }
                                                is String -> listOf(rawIngredients)
                                                else -> emptyList()
                                            }

                                            exploreList.add(
                                                Recipe(
                                                    id = id,
                                                    ingredients = ingredientsList,
                                                    name = name,
                                                    instructions = recipeData.child(INSTRUCTIONS).getValue<String>() ?: "",
                                                    urlLink = recipeData.child(URL_LINK).getValue<String>() ?: "",
                                                    viewOrder = recipeData.child(VIEW_ORDER).getValue<Int>() ?: 0,
                                                    category = recipeData.child(CATEGORY).getValue<String>() ?: Category.MAIN_COURSE.name,
                                                    isReadOnly = true,
                                                    imageUrl = recipeData.child(IMAGE_URL).getValue<String>() ?: "",
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                            exploreRecipes.postValue(
                                exploreList
                                    .asSequence()
                                    .distinctBy { it.id }
                                    .distinctBy { it.name.trim().lowercase() }
                                    .sortedWith(compareBy({ it.getCategoryEnum().ordinal }, { it.name.lowercase() }))
                                    .toList(),
                            )
                        } else {
                            exploreRecipes.postValue(emptyList())
                        }
                        isLoadingExplore.postValue(false)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        isLoadingExplore.postValue(false)
                    }
                },
            )
        return exploreRecipes
    }

    fun uploadRecipeImage(imageUri: Uri, recipeId: String): Task<Uri> {
        val fileName = if ((recipeId.isNotEmpty()) && (recipeId != "0")) recipeId else UUID.randomUUID().toString()
        val storage = try {
            FirebaseStorage.getInstance("gs://my-recipes-97d34.appspot.com")
        } catch (_: Exception) {
            FirebaseStorage.getInstance()
        }
        val storageRef = storage.reference.child("recipe_images/$userId/$fileName.jpg")
        return storageRef.putFile(imageUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }
    }

    fun addRecipe(recipe: Recipe): Task<Void> {
        val rootRef = database.getReference(ROOT)
        return rootRef.child(userId).child(UUID.randomUUID().toString()).setValue(getRecipeMap(recipe))
    }

    fun updateRecipe(recipe: Recipe): Task<Void> {
        val rootRef = database.getReference(ROOT)
        return rootRef.child(userId).child(recipe.id).updateChildren(getRecipeMap(recipe))
    }

    fun deleteRecipe(recipe: Recipe): Task<Void> {
        val rootRef = database.getReference("$ROOT/$userId")
        return rootRef.child(recipe.id).removeValue()
    }
}
