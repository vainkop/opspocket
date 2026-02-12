package com.vainkop.opspocket.data.mapper

import com.vainkop.opspocket.data.mapper.ClusterMapper.toDomain
import com.vainkop.opspocket.data.remote.dto.ClusterDto
import com.vainkop.opspocket.data.remote.dto.RegionDto
import com.vainkop.opspocket.domain.model.AgentStatus
import com.vainkop.opspocket.domain.model.ClusterStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class ClusterMapperTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = ClusterDto(
            id = "cluster-123",
            name = "production-cluster",
            region = RegionDto(name = "us-east-1", displayName = "US East (N. Virginia)"),
            status = "ready",
            agentStatus = "online",
            providerType = "eks",
            createdAt = "2024-01-15T10:30:00Z",
        )

        val cluster = dto.toDomain()

        assertEquals("cluster-123", cluster.id)
        assertEquals("production-cluster", cluster.name)
        assertEquals("us-east-1", cluster.regionName)
        assertEquals("US East (N. Virginia)", cluster.regionDisplayName)
        assertEquals(ClusterStatus.READY, cluster.status)
        assertEquals(AgentStatus.ONLINE, cluster.agentStatus)
        assertEquals("eks", cluster.providerType)
        assertEquals("2024-01-15T10:30:00Z", cluster.createdAt)
    }

    @Test
    fun `toDomain maps null name to empty string`() {
        val dto = ClusterDto(id = "c-1", name = null)

        val cluster = dto.toDomain()

        assertEquals("", cluster.name)
    }

    @Test
    fun `toDomain maps null region to empty region strings`() {
        val dto = ClusterDto(id = "c-1", region = null)

        val cluster = dto.toDomain()

        assertEquals("", cluster.regionName)
        assertEquals("", cluster.regionDisplayName)
    }

    @Test
    fun `toDomain maps region with null fields to empty strings`() {
        val dto = ClusterDto(id = "c-1", region = RegionDto(name = null, displayName = null))

        val cluster = dto.toDomain()

        assertEquals("", cluster.regionName)
        assertEquals("", cluster.regionDisplayName)
    }

    @Test
    fun `toDomain maps null providerType to empty string`() {
        val dto = ClusterDto(id = "c-1", providerType = null)

        val cluster = dto.toDomain()

        assertEquals("", cluster.providerType)
    }

    @Test
    fun `toDomain maps null createdAt to empty string`() {
        val dto = ClusterDto(id = "c-1", createdAt = null)

        val cluster = dto.toDomain()

        assertEquals("", cluster.createdAt)
    }

    @Test
    fun `toDomain maps null status to UNKNOWN`() {
        val dto = ClusterDto(id = "c-1", status = null)

        val cluster = dto.toDomain()

        assertEquals(ClusterStatus.UNKNOWN, cluster.status)
    }

    @Test
    fun `toDomain maps ready status to READY`() {
        val dto = ClusterDto(id = "c-1", status = "ready")

        val cluster = dto.toDomain()

        assertEquals(ClusterStatus.READY, cluster.status)
    }

    @Test
    fun `toDomain maps status case-insensitively`() {
        val dto = ClusterDto(id = "c-1", status = "READY")

        val cluster = dto.toDomain()

        assertEquals(ClusterStatus.READY, cluster.status)
    }

    @Test
    fun `toDomain maps unrecognized status to UNKNOWN`() {
        val dto = ClusterDto(id = "c-1", status = "some-random-status")

        val cluster = dto.toDomain()

        assertEquals(ClusterStatus.UNKNOWN, cluster.status)
    }

    @Test
    fun `toDomain maps all known cluster statuses`() {
        val statusMappings = mapOf(
            "connecting" to ClusterStatus.CONNECTING,
            "ready" to ClusterStatus.READY,
            "warning" to ClusterStatus.WARNING,
            "failed" to ClusterStatus.FAILED,
            "deleting" to ClusterStatus.DELETING,
            "deleted" to ClusterStatus.DELETED,
            "hibernating" to ClusterStatus.HIBERNATING,
            "hibernated" to ClusterStatus.HIBERNATED,
            "resuming" to ClusterStatus.RESUMING,
        )

        for ((input, expected) in statusMappings) {
            val dto = ClusterDto(id = "c-1", status = input)
            assertEquals(expected, dto.toDomain().status, "Status '$input' should map to $expected")
        }
    }

    @Test
    fun `toDomain maps null agentStatus to UNKNOWN`() {
        val dto = ClusterDto(id = "c-1", agentStatus = null)

        val cluster = dto.toDomain()

        assertEquals(AgentStatus.UNKNOWN, cluster.agentStatus)
    }

    @Test
    fun `toDomain maps waiting-connection to WAITING_CONNECTION`() {
        val dto = ClusterDto(id = "c-1", agentStatus = "waiting-connection")

        val cluster = dto.toDomain()

        assertEquals(AgentStatus.WAITING_CONNECTION, cluster.agentStatus)
    }

    @Test
    fun `toDomain maps all known agent statuses`() {
        val statusMappings = mapOf(
            "waiting-connection" to AgentStatus.WAITING_CONNECTION,
            "online" to AgentStatus.ONLINE,
            "non-responding" to AgentStatus.NON_RESPONDING,
            "disconnected" to AgentStatus.DISCONNECTED,
            "disconnecting" to AgentStatus.DISCONNECTING,
        )

        for ((input, expected) in statusMappings) {
            val dto = ClusterDto(id = "c-1", agentStatus = input)
            assertEquals(
                expected,
                dto.toDomain().agentStatus,
                "Agent status '$input' should map to $expected",
            )
        }
    }

    @Test
    fun `toDomain maps unrecognized agent status to UNKNOWN`() {
        val dto = ClusterDto(id = "c-1", agentStatus = "some-unknown-status")

        val cluster = dto.toDomain()

        assertEquals(AgentStatus.UNKNOWN, cluster.agentStatus)
    }

    @Test
    fun `toDomain with all null optional fields uses defaults`() {
        val dto = ClusterDto(
            id = "minimal-id",
            name = null,
            region = null,
            status = null,
            agentStatus = null,
            providerType = null,
            createdAt = null,
        )

        val cluster = dto.toDomain()

        assertEquals("minimal-id", cluster.id)
        assertEquals("", cluster.name)
        assertEquals("", cluster.regionName)
        assertEquals("", cluster.regionDisplayName)
        assertEquals(ClusterStatus.UNKNOWN, cluster.status)
        assertEquals(AgentStatus.UNKNOWN, cluster.agentStatus)
        assertEquals("", cluster.providerType)
        assertEquals("", cluster.createdAt)
    }
}
