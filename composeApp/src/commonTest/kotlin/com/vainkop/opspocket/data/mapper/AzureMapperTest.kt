package com.vainkop.opspocket.data.mapper

import com.vainkop.opspocket.data.mapper.AzureMapper.toDomain
import com.vainkop.opspocket.data.remote.dto.AzureHardwareProfileDto
import com.vainkop.opspocket.data.remote.dto.AzureInstanceViewDto
import com.vainkop.opspocket.data.remote.dto.AzureInstanceViewStatusDto
import com.vainkop.opspocket.data.remote.dto.AzureSubscriptionDto
import com.vainkop.opspocket.data.remote.dto.AzureTenantDto
import com.vainkop.opspocket.data.remote.dto.AzureVmDto
import com.vainkop.opspocket.data.remote.dto.AzureVmPropertiesDto
import com.vainkop.opspocket.domain.model.VmPowerState
import kotlin.test.Test
import kotlin.test.assertEquals

class AzureMapperTest {

    @Test
    fun `tenant toDomain maps all fields correctly`() {
        val dto = AzureTenantDto(tenantId = "t-123", displayName = "My Org")
        val tenant = dto.toDomain()
        assertEquals("t-123", tenant.tenantId)
        assertEquals("My Org", tenant.displayName)
    }

    @Test
    fun `tenant toDomain maps null displayName to empty string`() {
        val dto = AzureTenantDto(tenantId = "t-1", displayName = null)
        assertEquals("", dto.toDomain().displayName)
    }

    @Test
    fun `subscription toDomain maps all fields correctly`() {
        val dto = AzureSubscriptionDto(
            subscriptionId = "sub-123",
            displayName = "Production",
            state = "Enabled",
        )
        val sub = dto.toDomain()
        assertEquals("sub-123", sub.subscriptionId)
        assertEquals("Production", sub.displayName)
        assertEquals("Enabled", sub.state)
    }

    @Test
    fun `subscription toDomain maps null fields to empty strings`() {
        val dto = AzureSubscriptionDto(
            subscriptionId = "sub-1",
            displayName = null,
            state = null,
        )
        val sub = dto.toDomain()
        assertEquals("", sub.displayName)
        assertEquals("", sub.state)
    }

    @Test
    fun `vm toDomain maps all fields correctly`() {
        val dto = AzureVmDto(
            id = "/subscriptions/sub-1/resourceGroups/my-rg/providers/Microsoft.Compute/virtualMachines/my-vm",
            name = "my-vm",
            location = "eastus",
            properties = AzureVmPropertiesDto(
                hardwareProfile = AzureHardwareProfileDto(vmSize = "Standard_B2s"),
                provisioningState = "Succeeded",
                instanceView = AzureInstanceViewDto(
                    statuses = listOf(
                        AzureInstanceViewStatusDto(
                            code = "ProvisioningState/succeeded",
                            displayStatus = "Provisioning succeeded",
                        ),
                        AzureInstanceViewStatusDto(
                            code = "PowerState/running",
                            displayStatus = "VM running",
                        ),
                    )
                ),
            ),
        )

        val vm = dto.toDomain()
        assertEquals("my-vm", vm.name)
        assertEquals("my-rg", vm.resourceGroup)
        assertEquals("eastus", vm.location)
        assertEquals("Standard_B2s", vm.vmSize)
        assertEquals("Succeeded", vm.provisioningState)
        assertEquals(VmPowerState.RUNNING, vm.powerState)
    }

    @Test
    fun `vm toDomain with null properties uses defaults`() {
        val dto = AzureVmDto(
            id = "/subscriptions/sub-1/resourceGroups/rg/providers/Microsoft.Compute/virtualMachines/vm",
            name = null,
            location = null,
            properties = null,
        )

        val vm = dto.toDomain()
        assertEquals("", vm.name)
        assertEquals("", vm.location)
        assertEquals("", vm.vmSize)
        assertEquals("", vm.provisioningState)
        assertEquals(VmPowerState.UNKNOWN, vm.powerState)
    }

    @Test
    fun `vm toDomain extracts resource group from id`() {
        val dto = AzureVmDto(
            id = "/subscriptions/abc-123/resourceGroups/Production-RG/providers/Microsoft.Compute/virtualMachines/vm1",
        )
        assertEquals("Production-RG", dto.toDomain().resourceGroup)
    }

    @Test
    fun `extractResourceGroup handles case-insensitive path`() {
        assertEquals(
            "my-rg",
            AzureMapper.extractResourceGroup(
                "/subscriptions/sub/RESOURCEGROUPS/my-rg/providers/Microsoft.Compute/virtualMachines/vm"
            ),
        )
    }

    @Test
    fun `extractResourceGroup returns empty for malformed id`() {
        assertEquals("", AzureMapper.extractResourceGroup("not-a-valid-id"))
    }

    @Test
    fun `vm toDomain maps all power states correctly`() {
        val stateMappings = mapOf(
            "PowerState/running" to VmPowerState.RUNNING,
            "PowerState/stopped" to VmPowerState.STOPPED,
            "PowerState/deallocated" to VmPowerState.DEALLOCATED,
            "PowerState/starting" to VmPowerState.STARTING,
            "PowerState/stopping" to VmPowerState.STOPPING,
            "PowerState/deallocating" to VmPowerState.DEALLOCATING,
        )

        for ((code, expected) in stateMappings) {
            val dto = AzureVmDto(
                id = "/subscriptions/s/resourceGroups/r/providers/Microsoft.Compute/virtualMachines/v",
                properties = AzureVmPropertiesDto(
                    instanceView = AzureInstanceViewDto(
                        statuses = listOf(AzureInstanceViewStatusDto(code = code))
                    )
                ),
            )
            assertEquals(expected, dto.toDomain().powerState, "Code '$code' should map to $expected")
        }
    }

    @Test
    fun `vm toDomain maps unknown power state code to UNKNOWN`() {
        val dto = AzureVmDto(
            id = "/subscriptions/s/resourceGroups/r/providers/Microsoft.Compute/virtualMachines/v",
            properties = AzureVmPropertiesDto(
                instanceView = AzureInstanceViewDto(
                    statuses = listOf(AzureInstanceViewStatusDto(code = "PowerState/unknown-state"))
                )
            ),
        )
        assertEquals(VmPowerState.UNKNOWN, dto.toDomain().powerState)
    }

    @Test
    fun `vm toDomain maps no power state status to UNKNOWN`() {
        val dto = AzureVmDto(
            id = "/subscriptions/s/resourceGroups/r/providers/Microsoft.Compute/virtualMachines/v",
            properties = AzureVmPropertiesDto(
                instanceView = AzureInstanceViewDto(
                    statuses = listOf(
                        AzureInstanceViewStatusDto(code = "ProvisioningState/succeeded")
                    )
                )
            ),
        )
        assertEquals(VmPowerState.UNKNOWN, dto.toDomain().powerState)
    }
}
