package com.vainkop.opspocket.domain.model

enum class ClusterStatus {
    CONNECTING,
    READY,
    WARNING,
    FAILED,
    DELETING,
    DELETED,
    HIBERNATING,
    HIBERNATED,
    RESUMING,
    UNKNOWN;

    companion object {
        fun fromString(status: String?): ClusterStatus {
            if (status == null) return UNKNOWN
            return entries.find { it.name.equals(status, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
