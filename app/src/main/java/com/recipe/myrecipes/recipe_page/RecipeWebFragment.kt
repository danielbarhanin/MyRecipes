package com.recipe.myrecipes.recipe_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.recipe.myrecipes.R

class RecipeWebFragment : Fragment() {

    private val args by navArgs<RecipeWebFragmentArgs>()
    private lateinit var exitButton: AppCompatImageView
    private lateinit var progressloader: ProgressBar
    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_recipe_link_webview, container, false)

        v.initViews()

        exitButton.setOnClickListener {
            val action = RecipeWebFragmentDirections.actionRecipeWebFragmentToRecipePageFragment(args.recipe)
            findNavController().navigate(action)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val action = RecipeWebFragmentDirections.actionRecipeWebFragmentToRecipePageFragment(args.recipe)
            findNavController().navigate(action)
        }

        webView.run {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    progressloader.isVisible = false
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    progressloader.isVisible = false
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressloader.progress = newProgress
                }
            }

            loadUrl(args.recipe.urlLink)
        }

        return v
    }

    private fun View.initViews() {
        exitButton = findViewById(R.id.exitButton)
        progressloader = findViewById(R.id.progressBar)
        webView = findViewById(R.id.webView)
    }
}