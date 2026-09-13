package com.recipe.myrecipes.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.recipe.myrecipes.R
import com.recipe.myrecipes.USER_ID

@Suppress("DEPRECATION")
class LoginFragment : Fragment() {

    private lateinit var loginButton: View
    private lateinit var termsTextView: View

    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            firebaseAuth(account.idToken)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val v = inflater.inflate(R.layout.fragment_login, container, false)

        loginButton = v.findViewById(R.id.LoginButton)
        termsTextView = v.findViewById(R.id.termsTextView)

        ViewCompat.setOnApplyWindowInsetsListener(v.findViewById(R.id.loginRoot)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        loginButton.setOnClickListener {
            googleSignIn()
        }

        termsTextView.setOnClickListener {
            showTermsDialog()
        }

        return v
    }

    private fun googleSignIn() {
        googleSignInLauncher.launch(mGoogleSignInClient.signInIntent)
    }

    private fun showTermsDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle(R.string.terms_dialog_title)
            .setMessage(R.string.terms_dialog_content)
            .setPositiveButton(R.string.terms_agree_button, null)
            .show()
    }

    private fun firebaseAuth(idToken: String?) {
        val credentials = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credentials).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId: String = user?.uid ?: ""

                findNavController().navigate(
                    R.id.action_loginFragment_to_recipesFragment,
                    bundleOf(USER_ID to userId),
                )
            } else {
                Toast.makeText(requireContext(), getString(R.string.login_error), Toast.LENGTH_LONG).show()
            }
        }
    }
}
