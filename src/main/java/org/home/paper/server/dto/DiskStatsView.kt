package org.home.paper.server.dto

data class DiskStatsView(
    val total: Double,
    val usable: Double,
    val free: Double
)
