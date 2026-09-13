package com.recipe.myrecipes.editor_recipe

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Category
import com.recipe.myrecipes.data.RecipeViewModel
import java.io.File
import java.io.FileOutputStream

open class BaseRecipeEditorFragment : Fragment() {

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
    protected lateinit var imagePickerContainer: MaterialCardView
    protected lateinit var recipeImagePreview: AppCompatImageView
    protected lateinit var addPhotoPlaceholder: View
    protected lateinit var removePhotoButton: AppCompatImageView
    protected lateinit var uploadProgressBar: ProgressBar

    protected var ingredientsViews: MutableList<IngredientView> = mutableListOf()
    protected var selectedCategory: Category = Category.MAIN_COURSE
    protected var selectedImageUri: Uri? = null
    protected var currentImageUrl: String = ""

    protected val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            displaySelectedImage(uri)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val v = inflater.inflate(R.layout.fragment_add_recipe, container, false)

        v.initViews()

        initEditTexts()

        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        instructionsEditText.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
                view.performClick()
            }
            return@setOnTouchListener false
        }

        imagePickerContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        removePhotoButton.setOnClickListener {
            clearSelectedImage()
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
        imagePickerContainer = findViewById(R.id.imagePickerContainer)
        recipeImagePreview = findViewById(R.id.recipeImagePreview)
        addPhotoPlaceholder = findViewById(R.id.addPhotoPlaceholder)
        removePhotoButton = findViewById(R.id.removePhotoButton)
        uploadProgressBar = findViewById(R.id.uploadProgressBar)
    }

    protected fun showLoadingState() {
        uploadButton.isEnabled = false
        uploadButton.text = ""
        uploadProgressBar.visibility = View.VISIBLE
    }

    protected fun hideLoadingState(buttonTextResId: Int) {
        uploadButton.isEnabled = true
        uploadButton.text = getString(buttonTextResId)
        uploadProgressBar.visibility = View.GONE
    }

    protected fun displaySelectedImage(imageSource: Any) {
        recipeImagePreview.visibility = View.VISIBLE
        removePhotoButton.visibility = View.VISIBLE
        addPhotoPlaceholder.visibility = View.GONE
        recipeImagePreview.load(imageSource) {
            crossfade(enable = true)
        }
    }

    protected fun clearSelectedImage() {
        selectedImageUri = null
        currentImageUrl = ""
        recipeImagePreview.visibility = View.GONE
        removePhotoButton.visibility = View.GONE
        addPhotoPlaceholder.visibility = View.VISIBLE
    }

    protected fun saveImageToInternalStorage(uri: Uri, fileName: String): Uri {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return uri
            val imagesDir = File(requireContext().filesDir, "recipe_images")
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val imageFile = File(imagesDir, "$fileName.jpg")
            val outputStream = FileOutputStream(imageFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(imageFile)
        } catch (_: Exception) {
            uri
        }
    }

    protected fun setupCategoryChips(defaultCategory: Category = Category.MAIN_COURSE) {
        categoryChipGroup.removeAllViews()
        selectedCategory = defaultCategory

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
        )
        val bgColors = intArrayOf(
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorPrimary),
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.cat_badge_bg),
        )
        val textColors = intArrayOf(
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white),
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.cat_badge_txt),
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
        return ingredientsViews.asSequence().map {
            it.getIngredient()
        }.filter { it.isNotEmpty() }.toList()
    }
}
