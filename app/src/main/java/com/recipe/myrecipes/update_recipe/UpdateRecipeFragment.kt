package com.recipe.myrecipes.update_recipe

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.recipe.myrecipes.R
import com.recipe.myrecipes.add_recipe.IngredientView
import com.recipe.myrecipes.data.Recipe
import com.recipe.myrecipes.data.RecipeViewModel

class UpdateRecipeFragment: Fragment() {

    private val args by navArgs<UpdateRecipeFragmentArgs>()

    private lateinit var recipeViewModel: RecipeViewModel

    private lateinit var exitButton: AppCompatImageView
    private lateinit var recipeNameEditText: AppCompatEditText
    private lateinit var titleIngredients: AppCompatTextView
    private lateinit var ingredientsContainer: LinearLayoutCompat
    private lateinit var instructionsEditText: AppCompatEditText
    private lateinit var linkEditText: AppCompatEditText
    private lateinit var uploadButton: AppCompatTextView
    private var ingredientsViews: MutableList<IngredientView> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_update_recipe, container, false)

        v.initViews()

        initEditTexts()

        initializeRecipeItems(args.recipe)

        recipeViewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)

        exitButton.setOnClickListener {
            val action = UpdateRecipeFragmentDirections.actionUpdateRecipeFragmentToRecipePageFragment(args.recipe)
            findNavController().navigate(action)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val action = UpdateRecipeFragmentDirections.actionUpdateRecipeFragmentToRecipePageFragment(args.recipe)
            findNavController().navigate(action)
        }

        uploadButton.setOnClickListener {
            updateRecipe()
        }

        // TODO - do it better
        instructionsEditText.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }

        titleIngredients.setOnClickListener {
            addIngredient()
        }

        return v
    }

    private fun View.initViews() {
        exitButton = findViewById(R.id.exitButton)
        recipeNameEditText = findViewById(R.id.recipeName)
        titleIngredients = findViewById(R.id.titleIngredients)
        instructionsEditText = findViewById(R.id.instructions)
        linkEditText = findViewById(R.id.link)
        uploadButton = findViewById(R.id.uploadButton)
        ingredientsContainer = findViewById(R.id.ingredientsContainer)
    }

    private fun initEditTexts() {
        linkEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        linkEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                // hide keyboard
                val inputMethodManager = linkEditText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(linkEditText.windowToken, 0)
            }
            actionId == EditorInfo.IME_ACTION_NEXT
        }

        recipeNameEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
        recipeNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                instructionsEditText.requestFocus()
            }
            actionId == EditorInfo.IME_ACTION_NEXT
        }
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

        val recipe = Recipe(args.recipe.id, ingredients, recipeName, instructions, urlLink, args.recipe.view_order)
        recipeViewModel.updateRecipe(recipe)
        Toast.makeText(requireContext(), getText(R.string.toast_updated_successfully), Toast.LENGTH_LONG).show()

        val action = UpdateRecipeFragmentDirections.actionUpdateRecipeFragmentToRecipePageFragment(recipe)
        findNavController().navigate(action)
    }

    private fun getIngredients(): List<String> {
        return ingredientsViews.map {
            it.getIngredient()
        }
    }
}