package com.unchain.auth

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.unchain.R
import com.unchain.activities.MainActivity
import com.unchain.data.preferences.model.UserPreferences
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        userPreferencesManager = UserPreferencesManager(this)

        // Check if user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already signed in, go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // User needs to sign in
            signIn()
        }

    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }


    private fun signIn() {
        val credentialManager = CredentialManager.create(this)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.web_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e("LoginActivity", "Credential Error", e)
                // Add fallback sign-in method if needed
                binding.progressBar.visibility = View.GONE
            } catch (e: Exception) {
                Log.e("LoginActivity", "Unexpected Error", e)
                binding.progressBar.visibility = View.GONE
            }
        }
    }



    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    Log.e(TAG, "Unexpected type of credential")
                }
            }
            else -> {
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    user?.let {

                        Log.d(TAG, "User Details:")
                        Log.d(TAG, "UID: ${it.uid}")
                        Log.d(TAG, "Display Name: ${it.displayName}")
                        Log.d(TAG, "Email: ${it.email}")
                        Log.d(TAG, "Photo URL: ${it.photoUrl}")
                        lifecycleScope.launch {
                            userPreferencesManager.saveUser(
                                UserPreferences(
                                    userId = it.uid,
                                    displayName = it.displayName ?: "",
                                    email = it.email ?: "",
                                    photoUrl = it.photoUrl?.toString() ?: ""
                                )
                            )
                        }
                    }
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }



    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

}