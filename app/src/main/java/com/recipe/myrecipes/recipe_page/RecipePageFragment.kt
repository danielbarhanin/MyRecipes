package com.recipe.myrecipes.recipe_page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Recipe
import com.recipe.myrecipes.data.RecipeViewModel

sealed class RecipeItem {
    data class Image(val imageUrl: String) : RecipeItem()
    data class Instructions(val body: String) : RecipeItem()
    data class Link(val recipe: Recipe) : RecipeItem()
    data class Ingredients(val ingredients: List<String>) : RecipeItem()
}

class RecipePageFragment : Fragment() {

    private val args by navArgs<RecipePageFragmentArgs>()
    private lateinit var recipeViewModel: RecipeViewModel

    private lateinit var recipeName: AppCompatTextView
    private lateinit var categoryBadge: AppCompatTextView
    private lateinit var recipeItems: RecyclerView
    private lateinit var exitButton: AppCompatImageView
    private lateinit var editButton: AppCompatImageView
    private lateinit var deleteButton: AppCompatImageView
    private lateinit var shareButton: AppCompatImageView

    private val items = mutableListOf<RecipeItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val v = inflater.inflate(R.layout.fragment_recipe_page, container, false)

        v.initViews()

        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        createItems()

        exitButton.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentRecipe_to_recipesFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_fragmentRecipe_to_recipesFragment)
        }

        if (args.recipeItems.isReadOnly) {
            editButton.visibility = View.GONE
            editButton.setOnClickListener(null)
            deleteButton.visibility = View.GONE
            deleteButton.setOnClickListener(null)
        } else {
            editButton.visibility = View.VISIBLE
            editButton.setOnClickListener {
                val action = RecipePageFragmentDirections.actionRecipePageFragmentToUpdateRecipeFragment(args.recipeItems)
                findNavController().navigate(action)
            }
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                showDeleteRecipeAlert(args.recipeItems)
            }
        }

        shareButton.setOnClickListener { shareRecipe() }

        return v
    }

    private fun createItems() {
        val recipe = args.recipeItems
        val cat = recipe.getCategoryEnum()
        recipeName.text = recipe.name
        categoryBadge.text = getString(cat.stringResId)

        items.clear()
        if (recipe.imageUrl.isNotEmpty()) {
            items.add(RecipeItem.Image(recipe.imageUrl))
        }

        items.add(RecipeItem.Ingredients(recipe.ingredients))
        items.add(RecipeItem.Instructions(recipe.instructions))
        if (recipe.urlLink.isNotEmpty()) items.add(RecipeItem.Link(recipe))

        recipeItems.adapter = RecipeAdapter(items)
        recipeItems.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun View.initViews() {
        recipeName = findViewById(R.id.recipeName)
        categoryBadge = findViewById(R.id.categoryBadge)
        recipeItems = findViewById(R.id.recyclerView)
        exitButton = findViewById(R.id.exitButton)
        editButton = findViewById(R.id.editButton)
        deleteButton = findViewById(R.id.deleteButton)
        shareButton = findViewById(R.id.shareButton)
    }

    private fun shareRecipe() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, args.recipeItems.getRecipeString(requireContext()))

        requireActivity().startActivity(Intent.createChooser(intent, "Share the recipe.."))
    }

    private fun showDeleteRecipeAlert(recipe: Recipe) {
        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle(String.format(getString(R.string.delete_alert_title), recipe.name))
            .setMessage(String.format(getString(R.string.delete_alert_body), recipe.name))
            .setPositiveButton(getString(R.string.delete_alert_positive_button)) { _, _ ->
                deleteRecipe(recipe)
            }
            .setNegativeButton(getString(R.string.delete_alert_negative_button), null)
            .show()
    }

    private fun deleteRecipe(recipe: Recipe) {
        recipeViewModel.deleteRecipe(recipe).addOnCompleteListener {
            Toast.makeText(
                requireContext(),
                String.format(getString(R.string.toast_deleted_successfully), recipe.name),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(R.id.action_fragmentRecipe_to_recipesFragment)
        }
    }
}
