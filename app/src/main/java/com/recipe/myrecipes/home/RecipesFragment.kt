package com.recipe.myrecipes.home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.recipe.myrecipes.R
import com.recipe.myrecipes.data.Category
import com.recipe.myrecipes.data.Recipe
import com.recipe.myrecipes.data.RecipeViewModel

const val LAST_VIEW_ORDER = "lastViewOrder"
const val LAST_SCROLL_POSITION = "lastScrollPosition"
const val TOP_RECYCLER = "topRecycler"
const val LAST_SELECTED_TAB = "lastSelectedTab"

class RecipesFragment: Fragment() {

    private lateinit var  recipeViewModel: RecipeViewModel

    private lateinit var logoutButton: AppCompatImageView
    private lateinit var homeButton: AppCompatImageView
    private lateinit var searchBar: AppCompatEditText
    private lateinit var categoryTabLayout: TabLayout
    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var recipesAdapter: RecipesAdapter
    private lateinit var addRecipeButton: FloatingActionButton
    private var lastScrollPosition = 0
    private var top = -1
    private var selectedCategoryTag: String? = null

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

        val itemTouchHelper = createItemTouchHelper()

        recipesAdapter = RecipesAdapter(
            onDeleteRecipeCallback = { recipe ->
                showDeleteRecipeAlert(recipe)
            },
            onChangingOrder = { recipes ->
                updateRecipesOrder(recipes)
            },
            onStartDrag = { viewHolder ->
                itemTouchHelper.startDrag(viewHolder)
            }
        )
        recipesAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        recipesRecyclerView.adapter = recipesAdapter

        itemTouchHelper.attachToRecyclerView(recipesRecyclerView)

        initCategoryTabs()

        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        homeButton.setOnClickListener {
            selectedCategoryTag = null
            searchBar.setText("")
            val firstTab = categoryTabLayout.getTabAt(0)
            if (firstTab != null) {
                firstTab.select()
            }
            filterData("")
        }

        recipeViewModel.getRecipes().observe(viewLifecycleOwner) {
            filterData(searchBar.text?.toString() ?: "")
        }
        recipeViewModel.getExploreRecipes().observe(viewLifecycleOwner) {
            filterData(searchBar.text?.toString() ?: "")
        }
    }

    private fun View.initViews() {
        logoutButton = findViewById(R.id.logoutButton)
        homeButton = findViewById(R.id.homeButton)
        searchBar = findViewById(R.id.searchBar)
        categoryTabLayout = findViewById(R.id.categoryTabLayout)
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView)
        addRecipeButton = findViewById(R.id.addRecipeButton)
    }

    private fun initCategoryTabs() {
        categoryTabLayout.removeAllTabs()
        val allTab = categoryTabLayout.newTab().setText(R.string.category_all)
        allTab.tag = null
        categoryTabLayout.addTab(allTab)

        for (category in Category.entries) {
            val tab = categoryTabLayout.newTab().setText(category.stringResId)
            tab.tag = category.name
            categoryTabLayout.addTab(tab)
        }

        val exploreTab = categoryTabLayout.newTab().setText(R.string.category_explore)
        exploreTab.tag = "EXPLORE"
        categoryTabLayout.addTab(exploreTab)

        categoryTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedCategoryTag = tab?.tag as? String
                filterData(searchBar.text?.toString() ?: "")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                selectedCategoryTag = tab?.tag as? String
                filterData(searchBar.text?.toString() ?: "")
            }
        })

        val savedTabTag = activity?.getPreferences(Context.MODE_PRIVATE)?.getString(LAST_SELECTED_TAB, null)
        if (savedTabTag != null) {
            for (i in 0 until categoryTabLayout.tabCount) {
                val tab = categoryTabLayout.getTabAt(i)
                val tag = tab?.tag as? String
                val matches = if (savedTabTag == "ALL_KEY") tag == null else tag == savedTabTag
                if (matches) {
                    selectedCategoryTag = tag
                    categoryTabLayout.post {
                        tab?.select()
                        categoryTabLayout.setScrollPosition(i, 0f, true)
                    }
                    break
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.getPreferences(Context.MODE_PRIVATE)?.edit {
            putString(LAST_SELECTED_TAB, selectedCategoryTag ?: "ALL_KEY")
        }
        if (recipesRecyclerView.childCount > 0) {
            val v: View = recipesRecyclerView.getChildAt(0)
            top = v.top - recipesRecyclerView.paddingTop
            activity?.getPreferences(Context.MODE_PRIVATE)
                ?.edit { putInt(LAST_SCROLL_POSITION, lastScrollPosition) }
            activity?.getPreferences(Context.MODE_PRIVATE)?.edit { putInt(TOP_RECYCLER, top) }
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
       return ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
           override fun isLongPressDragEnabled(): Boolean = false

           override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                if (fromPos != RecyclerView.NO_POSITION && toPos != RecyclerView.NO_POSITION) {
                    (recyclerView.adapter as RecipesAdapter).onChangingOrder(fromPos, toPos)
                }
                return true
            }

           override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

           override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
               super.clearView(recyclerView, viewHolder)
               (recyclerView.adapter as RecipesAdapter).onFinishReorder()
           }
        })
    }

    private fun filterData(query: String = searchBar.text?.toString() ?: "") {
        if (selectedCategoryTag == "EXPLORE") {
            recipeViewModel.exploreRecipes.value?.let { exploreList ->
                val filtered = exploreList.filter { recipe ->
                    query.isBlank() || query.lowercase() in recipe.name.lowercase()
                }

                val items = mutableListOf<HomeListItem>()
                for (cat in Category.entries) {
                    val catRecipes = filtered.filter { it.getCategoryEnum() == cat }
                    if (catRecipes.isNotEmpty()) {
                        items.add(HomeListItem.Header(cat.stringResId))
                        catRecipes.forEach { recipe ->
                            items.add(HomeListItem.RecipeItem(recipe))
                        }
                    }
                }
                recipesAdapter.setItems(items)
            }
        } else {
            recipeViewModel.recipes.value?.let { list ->
                val filtered = list.filter { recipe ->
                    val matchesCategory = selectedCategoryTag == null || recipe.category.equals(selectedCategoryTag, ignoreCase = true)
                    val matchesQuery = query.isBlank() || query.lowercase() in recipe.name.lowercase()
                    matchesCategory && matchesQuery
                }
                recipesAdapter.setItems(filtered.sortedBy { it.viewOrder }.map { HomeListItem.RecipeItem(it) })
            }
        }
    }

    private fun deleteRecipe(recipe: Recipe) {
        val mTask = recipeViewModel.deleteRecipe(recipe)
        mTask.addOnSuccessListener {
            Toast.makeText(requireContext(), String.format(getString(R.string.toast_deleted_successfully), recipe.name), Toast.LENGTH_LONG).show()
        }.addOnFailureListener {error ->
            Toast.makeText(requireContext(), String.format(getString(R.string.toast_error), error.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun showDeleteRecipeAlert(recipe: Recipe) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle(String.format(getString(R.string.delete_alert_title), recipe.name))
            .setMessage(String.format(getString(R.string.delete_alert_body), recipe.name))
            .setPositiveButton(getString(R.string.delete_alert_positive_button)) { _, _ ->
                deleteRecipe(recipe)
            }
            .setNegativeButton(getString(R.string.delete_alert_negative_button), null)
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle(R.string.logout_confirm_title)
            .setMessage(R.string.logout_confirm_body)
            .setPositiveButton(getString(R.string.delete_alert_positive_button)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.delete_alert_negative_button), null)
            .show()
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(requireContext(), gso).signOut().addOnCompleteListener {
            findNavController().navigate(R.id.action_recipesFragment_to_loginFragment)
        }
    }

    private fun updateRecipesOrder(recipes: List<Recipe>) {
        recipes.forEachIndexed { idx, recipe ->
            recipeViewModel.updateRecipe(recipe.copy(viewOrder = idx))

            // if its the last index
            if (idx == recipes.size - 1) {
                activity?.getPreferences(Context.MODE_PRIVATE)?.edit { putInt(LAST_VIEW_ORDER, idx) }
            }
        }
    }
}