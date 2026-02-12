package com.vainkop.opspocket.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AzureSelection(
    val tenantId: String? = null,
    val tenantName: String? = null,
    val subscriptionId: String? = null,
    val subscriptionName: String? = null,
) {
    val isComplete: Boolean
        get() = tenantId != null && subscriptionId != null
}

class AzurePreferences(private val settings: Settings) {

    private val _selectionFlow = MutableStateFlow(loadSelection())
    val selectionFlow: Flow<AzureSelection> = _selectionFlow.asStateFlow()

    suspend fun getSelection(): AzureSelection = loadSelection()

    suspend fun saveTenantSelection(tenantId: String, tenantName: String) {
        settings.putString(KEY_TENANT_ID, tenantId)
        settings.putString(KEY_TENANT_NAME, tenantName)
        settings.remove(KEY_SUBSCRIPTION_ID)
        settings.remove(KEY_SUBSCRIPTION_NAME)
        _selectionFlow.value = loadSelection()
    }

    suspend fun saveSubscriptionSelection(subscriptionId: String, subscriptionName: String) {
        settings.putString(KEY_SUBSCRIPTION_ID, subscriptionId)
        settings.putString(KEY_SUBSCRIPTION_NAME, subscriptionName)
        recordSubscriptionUsage(subscriptionId)
        _selectionFlow.value = loadSelection()
    }

    fun getSubscriptionUsageCount(subscriptionId: String): Int {
        return settings.getInt("${KEY_SUB_USAGE_PREFIX}$subscriptionId", 0)
    }

    fun getLastUsedSubscriptionId(): String? {
        return settings.getStringOrNull(KEY_LAST_USED_SUB)
    }

    private fun recordSubscriptionUsage(subscriptionId: String) {
        val current = settings.getInt("${KEY_SUB_USAGE_PREFIX}$subscriptionId", 0)
        settings.putInt("${KEY_SUB_USAGE_PREFIX}$subscriptionId", current + 1)
        settings.putString(KEY_LAST_USED_SUB, subscriptionId)
    }

    suspend fun clearAll() {
        settings.remove(KEY_TENANT_ID)
        settings.remove(KEY_TENANT_NAME)
        settings.remove(KEY_SUBSCRIPTION_ID)
        settings.remove(KEY_SUBSCRIPTION_NAME)
        _selectionFlow.value = AzureSelection()
    }

    private fun loadSelection(): AzureSelection = AzureSelection(
        tenantId = settings.getStringOrNull(KEY_TENANT_ID),
        tenantName = settings.getStringOrNull(KEY_TENANT_NAME),
        subscriptionId = settings.getStringOrNull(KEY_SUBSCRIPTION_ID),
        subscriptionName = settings.getStringOrNull(KEY_SUBSCRIPTION_NAME),
    )

    private companion object {
        const val KEY_TENANT_ID = "selected_tenant_id"
        const val KEY_TENANT_NAME = "selected_tenant_name"
        const val KEY_SUBSCRIPTION_ID = "selected_subscription_id"
        const val KEY_SUBSCRIPTION_NAME = "selected_subscription_name"
        const val KEY_SUB_USAGE_PREFIX = "sub_usage_count_"
        const val KEY_LAST_USED_SUB = "last_used_subscription_id"
    }
}
