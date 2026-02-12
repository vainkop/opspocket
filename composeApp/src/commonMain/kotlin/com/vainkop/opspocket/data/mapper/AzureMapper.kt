package com.vainkop.opspocket.data.mapper

import com.vainkop.opspocket.data.remote.dto.AzureSubscriptionDto
import com.vainkop.opspocket.data.remote.dto.AzureTenantDto
import com.vainkop.opspocket.data.remote.dto.AzureVmDto
import com.vainkop.opspocket.domain.model.AzureSubscription
import com.vainkop.opspocket.domain.model.AzureTenant
import com.vainkop.opspocket.domain.model.AzureVm
import com.vainkop.opspocket.domain.model.VmPowerState

object AzureMapper {
    fun AzureTenantDto.toDomain(): AzureTenant = AzureTenant(
        tenantId = tenantId,
        displayName = displayName.orEmpty(),
    )

    fun AzureSubscriptionDto.toDomain(): AzureSubscription = AzureSubscription(
        subscriptionId = subscriptionId,
        displayName = displayName.orEmpty(),
        state = state.orEmpty(),
    )

    fun AzureVmDto.toDomain(): AzureVm {
        val powerStateCode = properties?.instanceView?.statuses
            ?.firstOrNull { it.code?.startsWith("PowerState/") == true }
            ?.code
        return AzureVm(
            id = id,
            name = name.orEmpty(),
            resourceGroup = extractResourceGroup(id),
            location = location.orEmpty(),
            vmSize = properties?.hardwareProfile?.vmSize.orEmpty(),
            provisioningState = properties?.provisioningState.orEmpty(),
            powerState = VmPowerState.fromAzureCode(powerStateCode),
        )
    }

    private val resourceGroupRegex = Regex(
        """/resourceGroups/([^/]+)/""",
        RegexOption.IGNORE_CASE,
    )

    fun extractResourceGroup(resourceId: String): String {
        return resourceGroupRegex.find(resourceId)?.groupValues?.getOrNull(1).orEmpty()
    }
}
