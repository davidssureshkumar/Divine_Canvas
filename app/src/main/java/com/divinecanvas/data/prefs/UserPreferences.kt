package com.divinecanvas.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/** Optional account info. Persisted only if the user chooses to sign in. */
data class AccountInfo(
    val displayName: String?,
    val email: String?,
) {
    val isSignedIn: Boolean
        get() = !email.isNullOrBlank() || !displayName.isNullOrBlank()
}

@Singleton
class UserPreferences
@Inject
constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val NAME = stringPreferencesKey("account_name")
        val EMAIL = stringPreferencesKey("account_email")
        val EDITOR_SNAPSHOT = stringPreferencesKey("editor_snapshot")
    }

    val accountInfo: Flow<AccountInfo> =
        context.dataStore.data.map { prefs -> AccountInfo(prefs[Keys.NAME], prefs[Keys.EMAIL]) }

    /** Serialized [com.divinecanvas.ui.editor.EditorSnapshot] JSON, or null. */
    val editorSnapshotJson: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[Keys.EDITOR_SNAPSHOT] }

    suspend fun saveEditorSnapshot(json: String) {
        context.dataStore.edit { prefs -> prefs[Keys.EDITOR_SNAPSHOT] = json }
    }

    suspend fun saveAccount(name: String?, email: String?) {
        context.dataStore.edit { prefs ->
            name?.let { prefs[Keys.NAME] = it }
            email?.let { prefs[Keys.EMAIL] = it }
        }
    }

    suspend fun clearAccount() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.NAME)
            prefs.remove(Keys.EMAIL)
        }
    }
}
