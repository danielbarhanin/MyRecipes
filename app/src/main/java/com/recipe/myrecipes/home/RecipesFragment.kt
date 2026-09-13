package com.recipe.myrecipes.home

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.view.isNotEmpty
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
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

@Suppress("DEPRECATION")
class RecipesFragment : Fragment() {

    private lateinit var recipeViewModel: RecipeViewModel

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var menuButton: AppCompatImageView
    private lateinit var searchBar: AppCompatEditText
    private lateinit var categoryTabLayout: TabLayout
    private lateinit var exploreCategoryScrollView: HorizontalScrollView
    private lateinit var exploreCategoryChipGroup: ChipGroup
    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateContainer: View
    private lateinit var recipesAdapter: RecipesAdapter
    private lateinit var addRecipeButton: FloatingActionButton
    private var lastScrollPosition = 0
    private var top = -1
    private var selectedCategoryTag: String? = null
    private val selectedExploreCategories = mutableSetOf<String>()

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
        savedInstanceState: Bundle?,
    ): View? {

        val v = inflater.inflate(R.layout.fragment_recipes, container, false)

        v.initViews()

        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        addRecipeButton.setOnClickListener {
            findNavController().navigate(R.id.action_recipesFragment_to_addRecipeFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                requireActivity().finish()
            }
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchBar.addTextChangedListener(textWatcher)

        recipesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        lastScrollPosition = activity?.getPreferences(Context.MODE_PRIVATE)?.getInt(LAST_SCROLL_POSITION, 0) ?: 0
        top = activity?.getPreferences(Context.MODE_PRIVATE)?.getInt(TOP_RECYCLER, -1) ?: -1
        (recipesRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(lastScrollPosition, top)

        recipesRecyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    lastScrollPosition = (recipesRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                }
            },
        )

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
            },
        )
        recipesAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        recipesRecyclerView.adapter = recipesAdapter

        itemTouchHelper.attachToRecyclerView(recipesRecyclerView)

        initCategoryTabs()
        initExploreCategoryChips()

        menuButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    selectedCategoryTag = null
                    searchBar.setText("")
                    categoryTabLayout.getTabAt(0)?.select()
                    filterData("")
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_terms -> {
                    showTermsDialog()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    showLogoutConfirmationDialog()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

        recipeViewModel.isLoadingRecipes.observe(viewLifecycleOwner) {
            filterData(searchBar.text?.toString() ?: "")
        }
        recipeViewModel.isLoadingExplore.observe(viewLifecycleOwner) {
            filterData(searchBar.text?.toString() ?: "")
        }

        recipeViewModel.getRecipes().observe(viewLifecycleOwner) {
            filterData(searchBar.text?.toString() ?: "")
        }
        recipeViewModel.getExploreRecipes().observe(viewLifecycleOwner) {
            filterData(searchBar.text?.toString() ?: "")
        }
    }

    private fun View.initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        menuButton = findViewById(R.id.menuButton)
        searchBar = findViewById(R.id.searchBar)
        categoryTabLayout = findViewById(R.id.categoryTabLayout)
        exploreCategoryScrollView = findViewById(R.id.exploreCategoryScrollView)
        exploreCategoryChipGroup = findViewById(R.id.exploreCategoryChipGroup)
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
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

        categoryTabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    selectedCategoryTag = tab?.tag as? String
                    filterData(searchBar.text?.toString() ?: "")
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    selectedCategoryTag = tab?.tag as? String
                    filterData(searchBar.text?.toString() ?: "")
                }
            },
        )

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

    private fun initExploreCategoryChips() {
        exploreCategoryChipGroup.removeAllViews()
        selectedExploreCategories.clear()

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
        )
        val bgColors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary),
            ContextCompat.getColor(requireContext(), R.color.cat_badge_bg),
        )
        val textColors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.white),
            ContextCompat.getColor(requireContext(), R.color.cat_badge_txt),
        )
        val bgStateList = ColorStateList(states, bgColors)
        val textStateList = ColorStateList(states, textColors)

        val allChip = Chip(requireContext()).apply {
            id = View.generateViewId()
            text = getString(R.string.category_all)
            isCheckable = true
            isClickable = true
            isChecked = true
            tag = "ALL_CHIP"
            chipBackgroundColor = bgStateList
            setTextColor(textStateList)
            chipIcon = null
            isChipIconVisible = false
        }
        exploreCategoryChipGroup.addView(allChip)

        val categoryChipMap = mutableMapOf<String, Chip>()

        for (cat in Category.entries) {
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = getString(cat.stringResId)
                isCheckable = true
                isClickable = true
                isChecked = false
                tag = cat.name
                chipBackgroundColor = bgStateList
                setTextColor(textStateList)
                chipIcon = null
                isChipIconVisible = false
            }
            categoryChipMap[cat.name] = chip
            exploreCategoryChipGroup.addView(chip)
        }

        var isUpdatingChips = false

        allChip.setOnClickListener {
            if (isUpdatingChips) return@setOnClickListener
            isUpdatingChips = true
            selectedExploreCategories.clear()
            allChip.isChecked = true
            categoryChipMap.values.forEach { it.isChecked = false }
            isUpdatingChips = false
            filterData(searchBar.text?.toString() ?: "")
        }

        categoryChipMap.forEach { (catName, chip) ->
            chip.setOnClickListener {
                if (isUpdatingChips) return@setOnClickListener
                isUpdatingChips = true
                if (chip.isChecked) {
                    selectedExploreCategories.add(catName)
                    allChip.isChecked = false
                } else {
                    selectedExploreCategories.remove(catName)
                    if (selectedExploreCategories.isEmpty()) {
                        allChip.isChecked = true
                    }
                }
                isUpdatingChips = false
                filterData(searchBar.text?.toString() ?: "")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.getPreferences(Context.MODE_PRIVATE)?.edit {
            putString(LAST_SELECTED_TAB, selectedCategoryTag ?: "ALL_KEY")
        }
        if (recipesRecyclerView.isNotEmpty()) {
            val v: View = recipesRecyclerView.getChildAt(0)
            top = v.top - recipesRecyclerView.paddingTop
            activity?.getPreferences(Context.MODE_PRIVATE)
                ?.edit { putInt(LAST_SCROLL_POSITION, lastScrollPosition) }
            activity?.getPreferences(Context.MODE_PRIVATE)?.edit { putInt(TOP_RECYCLER, top) }
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
                override fun isLongPressDragEnabled(): Boolean = false

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    val fromPos = viewHolder.bindingAdapterPosition
                    val toPos = target.bindingAdapterPosition
                    if ((fromPos != RecyclerView.NO_POSITION) && (toPos != RecyclerView.NO_POSITION)) {
                        (recyclerView.adapter as RecipesAdapter).onChangingOrder(fromPos, toPos)
                    }
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    (recyclerView.adapter as RecipesAdapter).onFinishReorder()
                }
            },
        )
    }

    private fun updateEmptyStateSubtitle() {
        val emptyStateSubtitle = emptyStateContainer.findViewById<AppCompatTextView>(R.id.emptyStateSubtitle) ?: return
        val emptySubtitle = when {
            (selectedCategoryTag != null) && (selectedCategoryTag != "EXPLORE") -> {
                val catName = getString(Category.fromName(selectedCategoryTag).stringResId)
                getString(R.string.no_recipes_under_category, catName)
            }
            (selectedCategoryTag == "EXPLORE") && (selectedExploreCategories.size == 1) -> {
                val catName = getString(Category.fromName(selectedExploreCategories.first()).stringResId)
                getString(R.string.no_recipes_under_category, catName)
            }
            else -> {
                getString(R.string.no_recipes_subtitle)
            }
        }
        emptyStateSubtitle.text = emptySubtitle
    }

    private fun filterData(query: String = searchBar.text?.toString() ?: "") {
        val isLoading = if (selectedCategoryTag == "EXPLORE") {
            recipeViewModel.isLoadingExplore.value ?: false
        } else {
            recipeViewModel.isLoadingRecipes.value ?: false
        }

        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
        }

        if (selectedCategoryTag == "EXPLORE") {
            exploreCategoryScrollView.visibility = View.VISIBLE
            recipeViewModel.exploreRecipes.value?.let { exploreList ->
                val filtered = exploreList.filter { recipe ->
                    val matchesCategory = selectedExploreCategories.isEmpty() ||
                            selectedExploreCategories.any { cat -> recipe.category.equals(cat, ignoreCase = true) }
                    val matchesQuery = query.isBlank() || (query.lowercase() in recipe.name.lowercase())
                    matchesCategory && matchesQuery
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

                if (!isLoading) {
                    if (items.isEmpty()) {
                        updateEmptyStateSubtitle()
                        emptyStateContainer.visibility = View.VISIBLE
                    } else {
                        emptyStateContainer.visibility = View.GONE
                    }
                }
            }
        } else {
            exploreCategoryScrollView.visibility = View.GONE
            recipeViewModel.recipes.value?.let { list ->
                val filtered = list.filter { recipe ->
                    val matchesCategory = (selectedCategoryTag == null) || recipe.category.equals(selectedCategoryTag, ignoreCase = true)
                    val matchesQuery = query.isBlank() || (query.lowercase() in recipe.name.lowercase())
                    matchesCategory && matchesQuery
                }
                recipesAdapter.setItems(
                    filtered.asSequence().sortedBy { it.viewOrder }.map { HomeListItem.RecipeItem(it) }.toList(),
                )

                if (!isLoading) {
                    if (filtered.isEmpty()) {
                        updateEmptyStateSubtitle()
                        emptyStateContainer.visibility = View.VISIBLE
                    } else {
                        emptyStateContainer.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun deleteRecipe(recipe: Recipe) {
        val mTask = recipeViewModel.deleteRecipe(recipe)
        mTask.addOnSuccessListener {
            Toast.makeText(requireContext(), String.format(getString(R.string.toast_deleted_successfully), recipe.name), Toast.LENGTH_LONG).show()
        }.addOnFailureListener { error ->
            Toast.makeText(requireContext(), String.format(getString(R.string.toast_error), error.message), Toast.LENGTH_LONG).show()
        }
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

    private fun showTermsDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle(R.string.terms_dialog_title)
            .setMessage(R.string.terms_dialog_content)
            .setPositiveButton(R.string.terms_agree_button, null)
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
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
            if (idx == (recipes.size - 1)) {
                activity?.getPreferences(Context.MODE_PRIVATE)?.edit { putInt(LAST_VIEW_ORDER, idx) }
            }
        }
    }
}
