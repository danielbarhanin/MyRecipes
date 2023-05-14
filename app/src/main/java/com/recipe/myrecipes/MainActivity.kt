package com.recipe.myrecipes

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.recipe.myrecipes.home.LAST_SCROLL_POSITION
import com.recipe.myrecipes.home.TOP_RECYCLER

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()

        getPreferences(Context.MODE_PRIVATE)?.edit { putInt(LAST_SCROLL_POSITION, 0) }
        getPreferences(Context.MODE_PRIVATE)?.edit { putInt(TOP_RECYCLER, -1) }
    }
}