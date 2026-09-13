package com.recipe.myrecipes.editor_recipe

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.edit
import androidx.core.view.contains
import androidx.navigation.fragment.findNavController
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Category
import com.recipe.myrecipes.data.Recipe
import com.recipe.myrecipes.home.LAST_SELECTED_TAB
import com.recipe.myrecipes.home.LAST_VIEW_ORDER

class AddRecipeFragment : BaseRecipeEditorFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val v = super.onCreateView(inflater, container, savedInstanceState)

        exitButton.setOnClickListener {
            findNavController().navigate(R.id.action_addRecipeFragment_to_recipesFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_addRecipeFragment_to_recipesFragment)
        }

        uploadButton.setOnClickListener {
            insertDataToDatabase()
        }

        titleIngredients.setOnClickListener {
            addIngredient(requestFocus = true)
        }

        setupCategoryChips(Category.MAIN_COURSE)
        addIngredient(requestFocus = false)
        setText()

        recipeNameEditText.post {
            recipeNameEditText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            @Suppress("DEPRECATION")
            imm?.showSoftInput(recipeNameEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        return v
    }

    private fun setText() {
        titleRecipeName.text = getString(R.string.enter_recipe_name)
        titleInstructions.text = getString(R.string.add_instructions)
        titleIngredients.text = getString(R.string.add_ingredients)
        uploadButton.text = getString(R.string.upload)
    }

    private fun addIngredient(requestFocus: Boolean = false) {
        val ingredient = IngredientView(requireContext(), position = ingredientsViews.size).apply {
            onDeleteIngredient = {
                ingredientsViews.remove(this)
                if (ingredientsContainer.contains(this)) ingredientsContainer.removeView(this)
            }
        }
        ingredient.ingredientNameEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
        ingredient.ingredientNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val position = ingredientsViews.indexOf(ingredient)
                if ((position + 1) < ingredientsViews.size) {
                    ingredientsViews[position + 1].requestFocus()
                } else {
                    instructionsEditText.requestFocus()
                }
            }
            actionId == EditorInfo.IME_ACTION_NEXT
        }
        ingredientsContainer.addView(ingredient)
        ingredientsViews.add(ingredient)
        if (requestFocus) {
            ingredient.requestFocus()
        }
    }

    private fun insertDataToDatabase() {
        val recipeName = recipeNameEditText.text.toString()
        val instructions = instructionsEditText.text.toString()
        val urlLink = linkEditText.text.toString()
        val ingredients = getIngredients()
        val order = activity?.getPreferences(Context.MODE_PRIVATE)?.getInt(LAST_VIEW_ORDER, 0) ?: 0

        if (recipeName.isEmpty() || instructions.isEmpty() || ingredients.isEmpty()) {
            val missingField = if (recipeName.isEmpty()) {
                getString(R.string.missing_recipe_name)
            } else if (instructions.isEmpty()) {
                getString(R.string.missing_instructions)
            } else {
                getString(R.string.missing_ingreideints)
            }
            Toast.makeText(requireContext(), String.format(getString(R.string.toast_please_fill), missingField), Toast.LENGTH_LONG).show()
            return
        }

        showLoadingState()

        val saveRecipeWithImageUrl = { imageUrl: String ->
            val recipe = Recipe("0", ingredients, recipeName, instructions, urlLink, order, selectedCategory.name, imageUrl = imageUrl)
            recipeViewModel.addRecipe(recipe).addOnSuccessListener {
                Toast.makeText(requireContext(), getString(R.string.toast_added_successfully), Toast.LENGTH_LONG).show()

                activity?.getPreferences(Context.MODE_PRIVATE)?.edit {
                    putInt(LAST_VIEW_ORDER, order + 1)
                    putString(LAST_SELECTED_TAB, "ALL_KEY")
                }
                findNavController().navigate(R.id.action_addRecipeFragment_to_recipesFragment)
            }.addOnFailureListener { error ->
                hideLoadingState(R.string.upload)
                Toast.makeText(requireContext(), String.format(getString(R.string.toast_error), error.message), Toast.LENGTH_LONG).show()
            }
        }

        val uri = selectedImageUri
        if (uri != null) {
            Toast.makeText(requireContext(), getString(R.string.uploading_image), Toast.LENGTH_SHORT).show()
            val tempId = java.util.UUID.randomUUID().toString()
            val localPermanentUri = saveImageToInternalStorage(uri, tempId)

            recipeViewModel.uploadRecipeImage(localPermanentUri, tempId).addOnSuccessListener { downloadUri ->
                saveRecipeWithImageUrl(downloadUri.toString())
            }.addOnFailureListener {
                saveRecipeWithImageUrl(localPermanentUri.toString())
            }
        } else {
            saveRecipeWithImageUrl("")
        }
    }
}
