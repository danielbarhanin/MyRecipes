package com.recipe.myrecipes.add_recipe

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.edit
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Recipe
import com.recipe.myrecipes.data.RecipeViewModel
import com.recipe.myrecipes.home.LAST_VIEW_ORDER
import kotlinx.android.synthetic.main.ingredient_item.view.*
import kotlinx.android.synthetic.main.instructions_item.*

class AddRecipeFragment: Fragment() {

    private lateinit var recipeViewModel: RecipeViewModel

    private lateinit var exitButton: AppCompatImageView
    private lateinit var recipeNameEditText: AppCompatEditText
    private lateinit var ingredientsContainer: LinearLayoutCompat
    private lateinit var instructionsEditText: AppCompatEditText
    private lateinit var linkEditText: AppCompatEditText
    private lateinit var uploadButton: AppCompatTextView
    private lateinit var titleIngredients: AppCompatTextView
    private var ingredientsViews: MutableList<IngredientView> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_add_recipe, container, false)

        v.initViews()

        initEditTexts()

        recipeViewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)

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

        // TODO - do it better
        instructionsEditText.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }


        return v
    }

    private fun View.initViews() {
        exitButton = findViewById(R.id.exitButton)
        recipeNameEditText = findViewById(R.id.recipeName)
        instructionsEditText = findViewById(R.id.instructions)
        linkEditText = findViewById(R.id.link)
        uploadButton = findViewById(R.id.uploadButton)
        ingredientsContainer = findViewById(R.id.ingredientsContainer)
        titleIngredients = findViewById(R.id.titleIngredients)

        // add first Ingredient
        addIngredient()
    }

    private fun initEditTexts() {
        linkEditText.imeOptions = IME_ACTION_DONE
        linkEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_NEXT) {
                // hide keyboard
                val inputMethodManager = linkEditText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(linkEditText.windowToken, 0)
            }
            actionId == IME_ACTION_NEXT
        }

        recipeNameEditText.imeOptions = IME_ACTION_NEXT
        recipeNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_NEXT) {
                instructionsEditText.requestFocus()
            }
            actionId == IME_ACTION_NEXT
        }

        recipeNameEditText.requestFocus()
    }

    private fun addIngredient() {
        val ingredient = IngredientView(requireContext(), ingredientsViews.size).apply {
            onDeleteIngredient = { position ->
                ingredientsViews.remove(this)
                if (ingredientsContainer.contains(this)) ingredientsContainer.removeView(this)
            }
        }
        ingredient.ingredientNameEditText.imeOptions = IME_ACTION_NEXT
        ingredient.ingredientNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_NEXT) {
                val position = ingredientsViews.indexOf(ingredient)
                if ((position + 1) < ingredientsViews.size) {
                    ingredientsViews[position + 1].requestFocus()
                } else {
                    instructionsEditText.requestFocus()
                }
            }
            actionId == IME_ACTION_NEXT
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
        var order = activity?.getPreferences(Context.MODE_PRIVATE)?.getInt(LAST_VIEW_ORDER, 0) ?: 0
        if (order > 0) order += 1

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

        recipeViewModel.addRecipe(Recipe(0, ingredients, recipeName, instructions, urlLink, order))
        Toast.makeText(requireContext(), getString(R.string.toast_added_successfully), Toast.LENGTH_LONG).show()
        activity?.getPreferences(Context.MODE_PRIVATE)?.edit { putInt(LAST_VIEW_ORDER, order) }
        findNavController().navigate(R.id.action_addRecipeFragment_to_recipesFragment)
    }

    private fun getIngredients(): List<String> {
        return ingredientsViews.map {
            it.getIngredient()
        }
    }
}