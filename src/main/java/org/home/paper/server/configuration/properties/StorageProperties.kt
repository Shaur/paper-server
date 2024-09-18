package org.home.paper.server.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "storage")
data class StorageProperties(
    val purgatoryPath: String,
    val issuesPath: String
)
