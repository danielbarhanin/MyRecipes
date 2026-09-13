package com.recipe.myrecipes

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.recipe.myrecipes.data.API_KEY
import com.recipe.myrecipes.data.APPLICATION_ID
import com.recipe.myrecipes.data.DATABASE_URL_TEST
import com.recipe.myrecipes.home.LAST_SCROLL_POSITION
import com.recipe.myrecipes.home.TOP_RECYCLER

const val USER_ID: String = "UserId"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Reset tab to "All" when app is restarted fresh
        getPreferences(MODE_PRIVATE)?.edit { putString("lastSelectedTab", "ALL_KEY") }

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(
                this,
                FirebaseOptions.Builder()
                    .setApplicationId(APPLICATION_ID)
                    .setApiKey(API_KEY)
                    .build(),
            )
        }

        try {
            FirebaseDatabase.getInstance(DATABASE_URL_TEST).setPersistenceEnabled(true)
        } catch (_: Exception) {
            // Persistence may already be enabled
        }

        // Decide which fragment to start based on if there's a user logged
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.my_nav)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            graph.setStartDestination(R.id.loginFragment)
        } else {
            graph.setStartDestination(R.id.recipesFragment)
        }

        val userId = currentUser?.uid ?: ""
        val args = Bundle().apply {
            putString(USER_ID, userId)
        }
        navHostFragment.navController.setGraph(graph, args)
    }

    override fun onStop() {
        super.onStop()

        getPreferences(MODE_PRIVATE)?.edit { putInt(LAST_SCROLL_POSITION, 0) }
        getPreferences(MODE_PRIVATE)?.edit { putInt(TOP_RECYCLER, -1) }
        getPreferences(MODE_PRIVATE)?.edit { putString("lastSelectedTab", "ALL_KEY") }
    }
}
