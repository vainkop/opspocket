package com.vainkop.opspocket.domain.model

enum class VmPowerState {
    RUNNING, STOPPED, DEALLOCATED, STARTING, STOPPING, DEALLOCATING, UNKNOWN;

    companion object {
        fun fromAzureCode(code: String?): VmPowerState {
            if (code == null) return UNKNOWN
            return when {
                code.equals("PowerState/running", ignoreCase = true) -> RUNNING
                code.equals("PowerState/stopped", ignoreCase = true) -> STOPPED
                code.equals("PowerState/deallocated", ignoreCase = true) -> DEALLOCATED
                code.equals("PowerState/starting", ignoreCase = true) -> STARTING
                code.equals("PowerState/stopping", ignoreCase = true) -> STOPPING
                code.equals("PowerState/deallocating", ignoreCase = true) -> DEALLOCATING
                else -> UNKNOWN
            }
        }
    }
}
