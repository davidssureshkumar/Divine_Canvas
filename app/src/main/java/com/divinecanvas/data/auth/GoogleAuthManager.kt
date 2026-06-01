package com.divinecanvas.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.divinecanvas.BuildConfig
import com.divinecanvas.core.AppResult
import com.divinecanvas.data.prefs.AccountInfo
import com.divinecanvas.data.prefs.UserPreferences
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional Google Sign-In via Credential Manager. Entirely opt-in: when no web
 * client id is configured the feature reports itself unavailable and every other
 * feature in the app continues to work without an account.
 */
@Singleton
class GoogleAuthManager @Inject constructor(
    private val userPreferences: UserPreferences,
) {
    val isConfigured: Boolean = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    val accountInfo: Flow<AccountInfo> = userPreferences.accountInfo

    suspend fun signIn(activityContext: Context): AppResult<AccountInfo> {
        if (!isConfigured) {
            return AppResult.Failure("Google Sign-In isn't configured for this build")
        }
        return try {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val response = CredentialManager.create(activityContext)
                .getCredential(activityContext, request)
            val credential = response.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleId = GoogleIdTokenCredential.createFrom(credential.data)
                val info = AccountInfo(googleId.displayName, googleId.id)
                userPreferences.saveAccount(info.displayName, info.email)
                AppResult.Success(info)
            } else {
                AppResult.Failure("Unexpected credential type")
            }
        } catch (e: Exception) {
            AppResult.Failure(e.message ?: "Sign-in cancelled", e)
        }
    }

    suspend fun signOut() {
        userPreferences.clearAccount()
    }
}
