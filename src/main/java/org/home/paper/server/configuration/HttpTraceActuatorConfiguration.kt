package org.home.paper.server.configuration

import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class HttpTraceActuatorConfiguration {

    @Bean
    fun createTraceRepository() = InMemoryHttpExchangeRepository()

}