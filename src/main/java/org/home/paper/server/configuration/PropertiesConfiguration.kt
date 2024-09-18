package org.home.paper.server.configuration

import org.home.paper.server.configuration.properties.StorageProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    StorageProperties::class
)
class PropertiesConfiguration