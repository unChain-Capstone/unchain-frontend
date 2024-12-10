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
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
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
import com.unchain.data.api.RetrofitClient
import com.unchain.data.api.UserRegistrationRequest
import com.unchain.data.preferences.model.UserPreferences
import com.unchain.data.preferences.preferences.UserPreferencesManager
import com.unchain.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        userPreferencesManager = UserPreferencesManager(this)

        // Set up click listeners
        binding.loginButton.setOnClickListener {
            signIn()
        }

        binding.registerButton.setOnClickListener {
            signIn() // For now, both buttons do the same thing
        }

        // Check if user is already signed in, but only if they've clicked a button
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Clear the current user to force new login
            auth.signOut()
        }
    }

    private fun signIn() {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false
        binding.registerButton.isEnabled = false

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
            } catch (e: NoCredentialException) {
                Log.d("LoginActivity", "No saved credentials found, proceeding with fresh login")
                // When no credentials are found, continue with fresh Google Sign-In
                try {
                    val freshResult: GetCredentialResponse = credentialManager.getCredential(
                        request = GetCredentialRequest.Builder()
                            .addCredentialOption(GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(getString(R.string.web_id))
                                .build())
                            .build(),
                        context = this@LoginActivity
                    )
                    handleSignIn(freshResult)
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Fresh login attempt failed", e)
                    withContext(Dispatchers.Main) {
                        resetButtons()
                        // Show error message to user
                        binding.errorText.text = "Unable to sign in. Please try again."
                        binding.errorText.visibility = View.VISIBLE
                    }
                }
            } catch (e: GetCredentialException) {
                Log.e("LoginActivity", "Credential Error", e)
                withContext(Dispatchers.Main) {
                    resetButtons()
                    binding.errorText.text = "Sign in failed. Please try again."
                    binding.errorText.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Unexpected Error", e)
                withContext(Dispatchers.Main) {
                    resetButtons()
                    binding.errorText.text = "An unexpected error occurred. Please try again."
                    binding.errorText.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun resetButtons() {
        binding.progressBar.visibility = View.GONE
        binding.loginButton.isEnabled = true
        binding.registerButton.isEnabled = true
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
                        resetButtons()
                    }
                } else {
                    Log.e(TAG, "Unexpected type of credential")
                    resetButtons()
                }
            }
            else -> {
                Log.e(TAG, "Unexpected type of credential")
                resetButtons()
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
                        lifecycleScope.launch(Dispatchers.Main) {
                            try {
                                withContext(Dispatchers.IO) {
                                    // Get Firebase token
                                    val token = it.getIdToken(true).await().token
                                    Log.d(TAG, "Firebase Token: $token")

                                    // Register user to backend
                                    try {
                                        val response = RetrofitClient.apiService.registerUser(
                                            token = "Bearer $token",
                                            user = UserRegistrationRequest(
                                                id = it.uid,
                                                name = it.displayName ?: "",
                                                email = it.email ?: "",
                                                photoUrl = it.photoUrl?.toString() ?: ""
                                            )
                                        )

                                        withContext(Dispatchers.Main) {
                                            if (response.isSuccessful) {
                                                Log.d(TAG, "User registered successfully")
                                            } else {
                                                Log.e(TAG, "Failed to register user: ${response.code()} ${response.message()}")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Log.e(TAG, "Error registering user", e)
                                        }
                                    }

                                    // Save user preferences locally
                                    withContext(Dispatchers.Main) {
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
                            } catch (e: Exception) {
                                Log.e(TAG, "Error getting token", e)
                            } finally {
                                withContext(Dispatchers.Main) {
                                    binding.progressBar.visibility = View.GONE
                                    updateUI(user)
                                }
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    resetButtons()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}