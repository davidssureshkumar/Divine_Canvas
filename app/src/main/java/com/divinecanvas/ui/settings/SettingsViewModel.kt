package com.divinecanvas.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divinecanvas.BuildConfig
import com.divinecanvas.core.AppResult
import com.divinecanvas.data.auth.GoogleAuthManager
import com.divinecanvas.data.prefs.AccountInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    private val authManager: GoogleAuthManager,
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)

    val accountFlow =
        authManager.accountInfo.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountInfo(null, null),
        )

    val privacyUrl: String = BuildConfig.PRIVACY_POLICY_URL
    val termsUrl: String = BuildConfig.TERMS_URL
    val versionName: String = BuildConfig.VERSION_NAME
    val authConfigured: Boolean = authManager.isConfigured

    val message: StateFlow<String?> = _message.asStateFlow()

    fun signIn(activityContext: Context) {
        viewModelScope.launch {
            when (val result = authManager.signIn(activityContext)) {
                is AppResult.Success ->
                    _message.value = "Signed in as ${result.data.displayName ?: result.data.email}"
                is AppResult.Failure -> _message.value = result.message
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { authManager.signOut() }
    }

    fun onMessageShown() {
        _message.value = null
    }
}
