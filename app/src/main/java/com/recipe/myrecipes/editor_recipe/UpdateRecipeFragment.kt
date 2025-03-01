package com.recipe.myrecipes.editor_recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.contains
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Recipe

class UpdateRecipeFragment: BaseRecipeEditorFragment() {

    private val args by navArgs<UpdateRecipeFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)

        initializeRecipeItems(args.recipe)

        exitButton.setOnClickListener {
            val action =
                UpdateRecipeFragmentDirections.actionUpdateRecipeFragmentToRecipePageFragment(
                    args.recipe
                )
            findNavController().navigate(action)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val action =
                UpdateRecipeFragmentDirections.actionUpdateRecipeFragmentToRecipePageFragment(
                    args.recipe
                )
            findNavController().navigate(action)
        }

        uploadButton.setOnClickListener {
            updateRecipe()
        }

        titleIngredients.setOnClickListener {
            addIngredient()
        }

        setText()

        return v
    }

    private fun setText() {
        titleRecipeName.text = getString(R.string.update_recipe_name)
        titleInstructions.text = getString(R.string.update_instructions)
        titleIngredients.text = getString(R.string.update_ingredients)
        uploadButton.text = getString(R.string.update)
    }

    private fun initializeRecipeItems(recipe: Recipe) {
        recipeNameEditText.setText(recipe.name)
        instructionsEditText.setText(recipe.instructions)
        linkEditText.setText(recipe.urlLink)

        recipe.ingredients.forEach {
            addIngredient(it)
        }
    }

    private fun addIngredient(ingredientStr: String = "") {
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
        ingredient.initializeIngredient(ingredientStr)
        ingredientsContainer.addView(ingredient)
        ingredientsViews.add(ingredient)
    }

    private fun updateRecipe() {
        val recipeName = recipeNameEditText.text.toString()
        val instructions = instructionsEditText.text.toString()
        val urlLink = linkEditText.text.toString()
        val ingredients = getIngredients()

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

        val recipe = Recipe(args.recipe.id, ingredients, recipeName, instructions, urlLink, args.recipe.viewOrder)

        val mTask = recipeViewModel.updateRecipe(recipe)

        mTask.addOnSuccessListener {
            Toast.makeText(requireContext(), getText(R.string.toast_updated_successfully), Toast.LENGTH_LONG).show()

            val action =
                UpdateRecipeFragmentDirections.actionUpdateRecipeFragmentToRecipePageFragment(
                    recipe
                )
            findNavController().navigate(action)

        }.addOnFailureListener { error ->
            Toast.makeText(requireContext(), String.format(getString(R.string.toast_error), error.message), Toast.LENGTH_LONG).show()
        }
    }
}