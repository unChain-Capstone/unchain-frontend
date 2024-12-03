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
        binding.progressBar.visibility = View.VISIBLE
        
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
                    binding.progressBar.visibility = View.GONE
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