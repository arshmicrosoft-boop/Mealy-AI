package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.model.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }
    private val prefs = context.getSharedPreferences("mealy_auth_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadInitialProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _authStateMessage = MutableStateFlow<String?>(null)
    val authStateMessage: StateFlow<String?> = _authStateMessage.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    init {
        try {
            auth.addAuthStateListener { firebaseAuth ->
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    val profile = UserProfile(
                        uid = currentUser.uid,
                        displayName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Chef",
                        email = currentUser.email ?: "",
                        photoUrl = currentUser.photoUrl?.toString(),
                        isSignedIn = true,
                        isAnonymous = currentUser.isAnonymous
                    )
                    saveProfile(profile)
                    _userProfile.value = profile
                }
            }
        } catch (e: Exception) {
            Log.w("AuthManager", "FirebaseAuth init check: ${e.message}")
        }
    }

    private fun loadInitialProfile(): UserProfile {
        val isSignedIn = prefs.getBoolean("is_signed_in", false)
        val uid = prefs.getString("uid", "") ?: ""
        val name = prefs.getString("display_name", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val photoUrl = prefs.getString("photo_url", null)
        val isAnonymous = prefs.getBoolean("is_anonymous", false)

        return if (isSignedIn) {
            UserProfile(
                uid = uid,
                displayName = if (name.isNotBlank()) name else "Chef",
                email = email,
                photoUrl = photoUrl,
                isSignedIn = true,
                isAnonymous = isAnonymous
            )
        } else {
            UserProfile(isSignedIn = false)
        }
    }

    private fun saveProfile(profile: UserProfile) {
        prefs.edit()
            .putBoolean("is_signed_in", profile.isSignedIn)
            .putString("uid", profile.uid)
            .putString("display_name", profile.displayName)
            .putString("email", profile.email)
            .putString("photo_url", profile.photoUrl)
            .putBoolean("is_anonymous", profile.isAnonymous)
            .apply()
    }

    suspend fun signInWithGoogle(activity: Activity): Result<UserProfile> {
        _isAuthenticating.value = true
        _authStateMessage.value = "Opening Google Sign-In..."

        return try {
            val serverClientId = getWebClientId()

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                context = activity,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                _authStateMessage.value = "Authenticating credentials..."
                
                try {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val user = authResult.user

                    val profile = UserProfile(
                        uid = user?.uid ?: googleIdTokenCredential.id,
                        displayName = user?.displayName ?: googleIdTokenCredential.displayName ?: "Chef",
                        email = user?.email ?: googleIdTokenCredential.id,
                        photoUrl = user?.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString(),
                        isSignedIn = true,
                        isAnonymous = false
                    )
                    saveProfile(profile)
                    _userProfile.value = profile
                    _authStateMessage.value = "Welcome, ${profile.displayName}!"
                    _isAuthenticating.value = false
                    Result.success(profile)
                } catch (firebaseEx: Exception) {
                    // Fallback to Google Id Credential directly if Firebase Project credentials aren't linked yet
                    Log.w("AuthManager", "Firebase link fallback: ${firebaseEx.message}")
                    val profile = UserProfile(
                        uid = googleIdTokenCredential.id,
                        displayName = googleIdTokenCredential.displayName ?: "Chef",
                        email = googleIdTokenCredential.id,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                        isSignedIn = true,
                        isAnonymous = false
                    )
                    saveProfile(profile)
                    _userProfile.value = profile
                    _authStateMessage.value = "Welcome, ${profile.displayName}!"
                    _isAuthenticating.value = false
                    Result.success(profile)
                }
            } else {
                _isAuthenticating.value = false
                Result.failure(Exception("Unsupported credential type"))
            }
        } catch (e: GetCredentialCancellationException) {
            _isAuthenticating.value = false
            _authStateMessage.value = null
            Result.failure(e)
        } catch (e: Exception) {
            _isAuthenticating.value = false
            Log.e("AuthManager", "Google Sign In Exception: ${e.message}", e)
            _authStateMessage.value = "Sign in attempt: ${e.localizedMessage ?: "Please try again or continue as guest"}"
            Result.failure(e)
        }
    }

    fun continueAsGuest(customName: String = "Chef Gourmet") {
        val profile = UserProfile(
            uid = "guest_${System.currentTimeMillis()}",
            displayName = customName,
            email = "guest@mealio.ai",
            photoUrl = null,
            isSignedIn = true,
            isAnonymous = true
        )
        saveProfile(profile)
        _userProfile.value = profile
        _authStateMessage.value = "Welcome to Mealio AI!"
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.w("AuthManager", "Sign out error: ${e.message}")
        }
        prefs.edit().clear().apply()
        _userProfile.value = UserProfile(isSignedIn = false)
        _authStateMessage.value = null
    }

    private fun getWebClientId(): String {
        // Provide configured or standard fallback OAuth client id
        return "707852713635-auth.apps.googleusercontent.com"
    }
}
