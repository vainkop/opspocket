package com.vainkop.opspocket.domain.model

data class Cluster(
    val id: String,
    val name: String,
    val regionName: String,
    val regionDisplayName: String,
    val status: ClusterStatus,
    val agentStatus: AgentStatus,
    val providerType: String,
    val createdAt: String,
)
