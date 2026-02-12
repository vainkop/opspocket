package com.vainkop.opspocket.data.mapper

import com.vainkop.opspocket.data.remote.dto.ClusterDto
import com.vainkop.opspocket.domain.model.AgentStatus
import com.vainkop.opspocket.domain.model.Cluster
import com.vainkop.opspocket.domain.model.ClusterStatus

object ClusterMapper {

    fun ClusterDto.toDomain(): Cluster = Cluster(
        id = id,
        name = name.orEmpty(),
        regionName = region?.name.orEmpty(),
        regionDisplayName = region?.displayName.orEmpty(),
        status = ClusterStatus.fromString(status),
        agentStatus = AgentStatus.fromString(agentStatus),
        providerType = providerType.orEmpty(),
        createdAt = createdAt.orEmpty(),
    )
}
