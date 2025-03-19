package org.home.paper.server.controller

import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [PagesControllerTest.Initializer::class])
class PagesControllerTest @Autowired constructor(
    private val restTemplate: TestRestTemplate
) {

    @Test
    fun `get file which is not exists`() {
        val response = restTemplate.getForEntity("/pages/1/56", String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    object Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of().applyTo(configurableApplicationContext.environment)
        }
    }
}