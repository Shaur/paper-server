package org.home.paper.server.configuration

import org.home.paper.server.model.auth.AuthentificationEntity
import org.home.paper.server.service.JwtService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
internal class SecurityConfigurationTest @Autowired constructor(
    private val restClient: RestTestClient,
    private val jwtService: JwtService
) {

    @Test
    fun `unauthorized without token`() {
        restClient.get()
            .uri("/series")
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody()
            .json("{\"error\":\"unauthorized\"}")
    }

    @Test
    fun `unauthorized garbage token`() {
        restClient.get()
            .uri("/series")
            .header(HttpHeaders.AUTHORIZATION, "Bearer garbage")
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody()
            .json("{\"error\":\"unauthorized\"}")
    }

    @Test
    fun `unauthorized with unknown username`() {
        val userDetails = AuthentificationEntity(
            id = 1L,
            name = "John",
            authorities = listOf(),
            password = "password",
        )

        val token = jwtService.generateToken(userDetails)
        restClient.get()
            .uri("/series")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody()
            .json("{\"error\":\"unauthorized\"}")
    }

    @Test
    fun `unauthorized for post image`() {
        restClient.post()
            .uri("/pages/anything")
            .exchange()
            .expectStatus().isUnauthorized()
    }

    @Test
    fun `cors preflight`() {
        restClient.options()
            .uri("/series")
            .header(HttpHeaders.ORIGIN, "https://evil.example")
            .header("Access-Control-Request-Method", "GET")
            .exchangeSuccessfully()
            .expectHeader().doesNotExist("Access-Control-Allow-Credentials")
            .expectHeader().valueEquals("Access-Control-Allow-Origin", "*")
    }
}
