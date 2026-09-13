package com.recipe.myrecipes.editor_recipe

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Category
import com.recipe.myrecipes.data.RecipeViewModel

open class BaseRecipeEditorFragment: Fragment() {

    protected lateinit var recipeViewModel: RecipeViewModel

    protected lateinit var exitButton: AppCompatImageView
    protected lateinit var recipeNameEditText: AppCompatEditText
    protected lateinit var titleCategory: AppCompatTextView
    protected lateinit var categoryChipGroup: ChipGroup
    protected lateinit var ingredientsContainer: LinearLayoutCompat
    protected lateinit var instructionsEditText: AppCompatEditText
    protected lateinit var linkEditText: AppCompatEditText
    protected lateinit var uploadButton: AppCompatTextView
    protected lateinit var titleIngredients: AppCompatTextView
    protected lateinit var titleRecipeName: AppCompatTextView
    protected lateinit var titleInstructions: AppCompatTextView
    protected var ingredientsViews: MutableList<IngredientView> = mutableListOf()
    protected var selectedCategory: Category = Category.MAIN_COURSE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_add_recipe, container, false)

        v.initViews()

        initEditTexts()

        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

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

    protected fun View.initViews() {
        exitButton = findViewById(R.id.exitButton)
        recipeNameEditText = findViewById(R.id.recipeName)
        titleCategory = findViewById(R.id.titleCategory)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        instructionsEditText = findViewById(R.id.instructions)
        linkEditText = findViewById(R.id.link)
        uploadButton = findViewById(R.id.uploadButton)
        ingredientsContainer = findViewById(R.id.ingredientsContainer)
        titleIngredients = findViewById(R.id.titleIngredients)
        titleRecipeName = findViewById(R.id.titleRecipeName)
        titleInstructions = findViewById(R.id.titleInstructions)
    }

    protected fun setupCategoryChips(defaultCategory: Category = Category.MAIN_COURSE) {
        categoryChipGroup.removeAllViews()
        selectedCategory = defaultCategory

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val bgColors = intArrayOf(
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorPrimary),
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.cat_badge_bg)
        )
        val textColors = intArrayOf(
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white),
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.cat_badge_txt)
        )
        val bgStateList = android.content.res.ColorStateList(states, bgColors)
        val textStateList = android.content.res.ColorStateList(states, textColors)

        for (cat in Category.entries) {
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = getString(cat.stringResId)
                isCheckable = true
                isClickable = true
                isChecked = (cat == defaultCategory)
                tag = cat
                chipBackgroundColor = bgStateList
                setTextColor(textStateList)
                chipIcon = null
                isChipIconVisible = false
            }
            categoryChipGroup.addView(chip)
        }

        categoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds[0])
                (selectedChip?.tag as? Category)?.let {
                    selectedCategory = it
                }
            }
        }
    }

    protected fun initEditTexts() {
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

    protected fun getIngredients(): List<String> {
        return ingredientsViews.map {
            it.getIngredient()
        }.filter { it.isNotEmpty() }
    }

}