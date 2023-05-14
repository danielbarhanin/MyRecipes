package com.recipe.myrecipes.home

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Recipe
import com.recipe.myrecipes.data.RecipeViewModel
import org.json.JSONArray

const val LAST_VIEW_ORDER = "lastViewOrder"
const val LAST_SCROLL_POSITION = "lastScrollPosition"
const val TOP_RECYCLER = "topRecycler"

class RecipesFragment: Fragment() {

    private lateinit var  recipeViewModel: RecipeViewModel

    private lateinit var searchBar: AppCompatEditText
    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var recipesAdapter: RecipesAdapter
    private lateinit var addRecipeButton: FloatingActionButton
    private var lastScrollPosition = 0
    private var top = -1

    private var textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            filterData(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_recipes, container, false)

        v.initViews()

        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        addRecipeButton.setOnClickListener {
            findNavController().navigate(R.id.action_recipesFragment_to_addRecipeFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchBar.addTextChangedListener(textWatcher)

        recipesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        lastScrollPosition = activity?.getPreferences(Context.MODE_PRIVATE)?.getInt(LAST_SCROLL_POSITION, 0) ?: 0
        top = activity?.getPreferences(Context.MODE_PRIVATE)?.getInt(TOP_RECYCLER, -1) ?: -1
        (recipesRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(lastScrollPosition, top)

        recipesRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                lastScrollPosition = (recipesRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            }
        })

        recipesAdapter = RecipesAdapter(
            onDeleteRecipeCallback = { recipe ->
                showDeleteRecipeAlert(recipe)
            },
            onChangingOrder = { recipes ->
                updateRecipesOrder(recipes)
            }
        )
        recipesAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        recipeViewModel.readAllData.observe(viewLifecycleOwner, Observer { recipes ->
            recipesAdapter.setData(recipes.sortedBy { it.view_order })
            recipesRecyclerView.adapter = recipesAdapter
        })

        createItemTouchHelper().attachToRecyclerView(recipesRecyclerView)
    }

    private fun View.initViews() {
        searchBar = findViewById(R.id.searchBar)
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView)
        addRecipeButton = findViewById(R.id.addRecipeButton)
    }

    private var state: Parcelable? = null

    override fun onPause() {
        super.onPause()
        val v: View = recipesRecyclerView.getChildAt(0)
        top = v.top - recipesRecyclerView.paddingTop
        activity?.getPreferences(Context.MODE_PRIVATE)?.edit { putInt(LAST_SCROLL_POSITION, lastScrollPosition) }
        activity?.getPreferences(Context.MODE_PRIVATE)?.edit { putInt(TOP_RECYCLER, top) }

    }

    private fun createItemTouchHelper(): ItemTouchHelper {
       return ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
           override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                (recyclerView.adapter as RecipesAdapter).onChangingOrder(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

           override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

           override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
               super.clearView(recyclerView, viewHolder)
               (recyclerView.adapter as RecipesAdapter).onFinishReorder()
           }
        })
    }

    private fun filterData(query: String) {
        recipeViewModel.readAllData.value?.let {
            recipesAdapter.setData(
                it.filter { query.lowercase() in it.name.lowercase() }
            )
        }
    }

    private fun showDeleteRecipeAlert(recipe: Recipe) {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setPositiveButton(getString(R.string.delete_alert_positive_button)) { _, _ ->
                recipeViewModel.deleteRecipe(recipe)
                Toast.makeText(requireContext(), String.format(getString(R.string.toast_deleted_successfully), recipe.name), Toast.LENGTH_LONG).show()
            }
            setNegativeButton(R.string.delete_alert_negative_button) { _, _ ->

            }
            setTitle(String.format(getString(R.string.delete_alert_title), recipe.name))
            setMessage(String.format(getString(R.string.delete_alert_body), recipe.name))
        }

        builder.create().show()
    }

    private fun updateRecipesOrder(recipes: List<Recipe>) {
        recipes.forEachIndexed { idx, recipe ->
            recipeViewModel.updateRecipe(Recipe(recipe.id, recipe.ingredients, recipe.name,
            recipe.instructions, recipe.urlLink, idx))

            // if its the last index
            if (idx == recipes.size - 1) {
                activity?.getPreferences(Context.MODE_PRIVATE)?.edit { putInt(LAST_VIEW_ORDER, idx) }
            }
        }
    }

//    private fun fromDbToJson() {
//        val json = JsonArray()
//        recipeViewModel.readAllData.value?.forEach {
//                val recipeObject = JsonObject()
//                recipeObject.addProperty("name", it.name)
//                recipeObject.addProperty("id", it.id)
//                recipeObject.addProperty("view_order", it.view_order)
//                recipeObject.addProperty("urlLink" ,it.urlLink)
//                recipeObject.addProperty("instructions", it.instructions)
//                val jsonArray = JsonArray()
//                it.ingredients.forEach { ingredient ->
//                    jsonArray.add(ingredient)
//                }
//                recipeObject.add("ingredients", jsonArray)
//                json.add(recipeObject)
//            }
//
//        val jsonFinal = JsonObject()
//        jsonFinal.add("recipes", json)
//
//        val file = File(requireContext().filesDir, "db_json.json")
//        val fileWriter = FileWriter(file)
//        val bufferedWriter = BufferedWriter(fileWriter)
//        bufferedWriter.write(jsonFinal.toString())
//        bufferedWriter.close()
//    }

//    private fun fromJsonToDb() {
//        val jsonString = requireContext().assets.open("db_json.json")
//            .bufferedReader()
//            .use { it.readText() }
//
//        val jsonObject = JSONObject(jsonString)
//        val recipes = jsonObject.getJSONArray("recipes")
//        for (i in 0 until recipes.length()) {
//            val recipe = recipes.getJSONObject(i)
//            val id = recipe.getInt("id")
//            val name = recipe.getString("name")
//            val viewOrder = recipe.getInt("view_order")
//            val link = recipe.getString("urlLink")
//            val instructions = recipe.getString("instructions")
//            val ingredients = fromJsonArrayToIngredientsList(recipe.getJSONArray("ingredients"))
//
//            recipeViewModel.addRecipe(
//                Recipe(id, ingredients, name, instructions, link, viewOrder)
//            )
//        }
//    }

    private fun fromJsonArrayToIngredientsList(jsonArray: JSONArray) : List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.get(i) as String)
        }

        return list
    }

}