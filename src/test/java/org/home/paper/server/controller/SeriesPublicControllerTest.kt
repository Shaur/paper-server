package org.home.paper.server.controller

import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.model.Series
import org.home.paper.server.model.SeriesSubscription
import org.home.paper.server.model.User
import org.home.paper.server.repository.SeriesRepository
import org.home.paper.server.repository.SeriesSubscriptionRepository
import org.home.paper.server.repository.UserRepository
import org.home.paper.server.service.JwtService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [SeriesPublicControllerTest.Initializer::class])
class SeriesPublicControllerTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var seriesRepository: SeriesRepository

    @Autowired
    private lateinit var subscriptionRepository: SeriesSubscriptionRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @AfterEach
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun afterEach() {
        userRepository.deleteAll()
        seriesRepository.deleteAll()
        subscriptionRepository.deleteAll()
    }

    @Test
    fun `series subscribe test`() {
        val user = userRepository.save(User(null, "user", "password"))
        val series = seriesRepository.save(Series(null, "test", "someone"))

        val jwtToken = jwtService.generateToken(user)

        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        val response = restTemplate.exchange<Void>(
            "/series/${series.id}/subscribe",
            HttpMethod.PUT,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val subscriptions = subscriptionRepository.getByUserId(user.id)
        assertThat(subscriptions).hasSize(1)
        assertThat(subscriptions.first()).isEqualTo(SeriesSubscription(user.id, series.id!!))
    }

    @Test
    fun `unsubscribe series test`() {
        val user = userRepository.save(User(null, "user", "password"))
        val series = seriesRepository.save(Series(null, "test", "someone"))
        val jwtToken = jwtService.generateToken(user)

        subscriptionRepository.save(SeriesSubscription(user.id, series.id!!))
        var subscriptions = subscriptionRepository.getByUserId(user.id)
        assertThat(subscriptions).hasSize(1)

        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        val response = restTemplate.exchange<Void>(
            "/series/${series.id}/unsubscribe",
            HttpMethod.PUT,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        subscriptions = subscriptionRepository.getByUserId(user.id)
        assertThat(subscriptions).hasSize(0)
    }

    @Test
    fun `find unsubscribed series`(){
        val user = userRepository.save(User(null, "user", "password"))
        val series = seriesRepository.save(Series(null, "test", "someone"))
        val jwtToken = jwtService.generateToken(user)

        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        val response = restTemplate.exchange<List<SeriesCatalogItemView>>(
            "/series",
            HttpMethod.GET,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotEmpty

        val seriesDescription = response.body?.first()
        assertThat(seriesDescription?.id).isEqualTo(series.id)
        assertThat(seriesDescription?.title).isEqualTo(series.title)
        assertThat(seriesDescription?.subscribed).isEqualTo(false)
    }

    @Test
    fun `find subscribed series`(){
        val user = userRepository.save(User(null, "user", "password"))
        val series = seriesRepository.save(Series(null, "test", "someone"))
        subscriptionRepository.save(SeriesSubscription(user.id, series.id!!))
        val jwtToken = jwtService.generateToken(user)

        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        val response = restTemplate.exchange<List<SeriesCatalogItemView>>(
            "/series",
            HttpMethod.GET,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotEmpty

        val seriesDescription = response.body?.first()
        assertThat(seriesDescription?.id).isEqualTo(series.id)
        assertThat(seriesDescription?.title).isEqualTo(series.title)
        assertThat(seriesDescription?.subscribed).isEqualTo(true)
    }

    object Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of().applyTo(configurableApplicationContext.environment)
        }
    }

}