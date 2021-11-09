package com.recipes.myrecipes

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var addRecipeButton: FloatingActionButton
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recipeRecyclerView = findViewById(R.id.recipesList)
        addRecipeButton = findViewById(R.id.addRecipeButton)
        recipeAdapter = RecipeAdapter(mutableListOf())
        recipeRecyclerView.adapter = recipeAdapter
        recipeRecyclerView.layoutManager = LinearLayoutManager(this)

        addRecipeButton.setOnClickListener {
            handleAddRecipe()
        }
    }

    private fun handleAddRecipe() {
        val addDialog = AlertDialog.Builder(this)
        addDialog.setTitle("הכנס את שם המתכון")

        val headerRecipe = EditText(this)
        headerRecipe.inputType = InputType.TYPE_CLASS_TEXT
        addDialog.setView(headerRecipe)

        addDialog.setPositiveButton("אישור"){
            _, _ ->
            recipeAdapter.addRecipe(headerRecipe.text.toString())
        }

        addDialog.setNegativeButton("ביטול"){_,_ ->}
        addDialog.show()
    }
}