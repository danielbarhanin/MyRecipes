package com.recipe.myrecipes.editor_recipe

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.edit
import androidx.core.view.contains
import androidx.navigation.fragment.findNavController
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Recipe
import com.recipe.myrecipes.home.LAST_VIEW_ORDER

class AddRecipeFragment: BaseRecipeEditorFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
            addIngredient()
        }

        recipeNameEditText.requestFocus()
        addIngredient()
        setText()

        return v
    }

    private fun setText() {
        titleRecipeName.text = getString(R.string.enter_recipe_name)
        titleInstructions.text = getString(R.string.add_instructions)
        titleIngredients.text = getString(R.string.add_ingredients)
        uploadButton.text = getString(R.string.upload)
    }

    private fun addIngredient() {
        val ingredient = IngredientView(requireContext(), ingredientsViews.size).apply {
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
        ingredient.requestFocus()
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
            }
            else {
                getString(R.string.missing_ingreideints)
            }
            Toast.makeText(requireContext(), String.format(getString(R.string.toast_please_fill), missingField), Toast.LENGTH_LONG).show()
            return
        }

        val mTask = recipeViewModel.addRecipe(Recipe("0", ingredients, recipeName, instructions, urlLink, order))

        mTask.addOnSuccessListener {
            Toast.makeText(requireContext(), getString(R.string.toast_added_successfully), Toast.LENGTH_LONG).show()

            activity?.getPreferences(Context.MODE_PRIVATE)?.edit { putInt(LAST_VIEW_ORDER, order + 1) }
            findNavController().navigate(R.id.action_addRecipeFragment_to_recipesFragment)

        }.addOnFailureListener { error ->
            Toast.makeText(requireContext(), String.format(getString(R.string.toast_error), error.message), Toast.LENGTH_LONG).show()
        }
    }
}