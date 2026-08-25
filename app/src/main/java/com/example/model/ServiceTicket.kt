package com.example.model

enum class UrgencyLevel(val label: String) {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH")
}

enum class TicketStatus(val label: String) {
    TRANSMITTED("TRANSMITTED"),
    DISPATCHED("DISPATCHED"),
    IN_PROGRESS("IN PROGRESS"),
    RESOLVED("RESOLVED")
}

data class ServiceTicket(
    val id: String,
    val timestamp: String,
    val faultCode: String,
    val description: String,
    val urgency: UrgencyLevel,
    val servicePartner: String = "Apex Auto Services",
    val status: TicketStatus = TicketStatus.TRANSMITTED
)

data class EcuCodeInfo(
    val code: String,
    val title: String,
    val description: String,
    val severity: UrgencyLevel
)
