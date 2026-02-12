package com.vainkop.opspocket.domain.model

enum class AgentStatus {
    WAITING_CONNECTION,
    ONLINE,
    NON_RESPONDING,
    DISCONNECTED,
    DISCONNECTING,
    UNKNOWN;

    companion object {
        fun fromString(status: String?): AgentStatus = when (status) {
            "waiting-connection" -> WAITING_CONNECTION
            "online" -> ONLINE
            "non-responding" -> NON_RESPONDING
            "disconnected" -> DISCONNECTED
            "disconnecting" -> DISCONNECTING
            else -> UNKNOWN
        }
    }
}
