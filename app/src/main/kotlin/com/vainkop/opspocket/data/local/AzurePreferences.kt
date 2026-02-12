package com.vainkop.opspocket.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.azureDataStore: DataStore<Preferences> by preferencesDataStore(name = "azure_preferences")

data class AzureSelection(
    val tenantId: String? = null,
    val tenantName: String? = null,
    val subscriptionId: String? = null,
    val subscriptionName: String? = null,
) {
    val isComplete: Boolean
        get() = tenantId != null && subscriptionId != null
}

@Singleton
class AzurePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val selectionFlow: Flow<AzureSelection> = context.azureDataStore.data.map { prefs ->
        AzureSelection(
            tenantId = prefs[KEY_TENANT_ID],
            tenantName = prefs[KEY_TENANT_NAME],
            subscriptionId = prefs[KEY_SUBSCRIPTION_ID],
            subscriptionName = prefs[KEY_SUBSCRIPTION_NAME],
        )
    }

    suspend fun getSelection(): AzureSelection = selectionFlow.first()

    suspend fun saveTenantSelection(tenantId: String, tenantName: String) {
        context.azureDataStore.edit { prefs ->
            prefs[KEY_TENANT_ID] = tenantId
            prefs[KEY_TENANT_NAME] = tenantName
            // Clear subscription when tenant changes
            prefs.remove(KEY_SUBSCRIPTION_ID)
            prefs.remove(KEY_SUBSCRIPTION_NAME)
        }
    }

    suspend fun saveSubscriptionSelection(subscriptionId: String, subscriptionName: String) {
        context.azureDataStore.edit { prefs ->
            prefs[KEY_SUBSCRIPTION_ID] = subscriptionId
            prefs[KEY_SUBSCRIPTION_NAME] = subscriptionName
        }
    }

    suspend fun clearAll() {
        context.azureDataStore.edit { it.clear() }
    }

    private companion object {
        val KEY_TENANT_ID = stringPreferencesKey("selected_tenant_id")
        val KEY_TENANT_NAME = stringPreferencesKey("selected_tenant_name")
        val KEY_SUBSCRIPTION_ID = stringPreferencesKey("selected_subscription_id")
        val KEY_SUBSCRIPTION_NAME = stringPreferencesKey("selected_subscription_name")
    }
}
