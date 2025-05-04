package org.home.paper.server.controller

import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.dto.SeriesMergeRequest
import org.home.paper.server.model.Issue
import org.home.paper.server.model.Series
import org.home.paper.server.model.SeriesSubscription
import org.home.paper.server.repository.IssueRepository
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
import java.util.*

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = [SeriesPublicControllerTest.Initializer::class])
class SeriesPublicControllerTest @Autowired constructor(
    userRepository: UserRepository,
    jwtService: JwtService,
    private val restTemplate: TestRestTemplate,
    private val seriesRepository: SeriesRepository,
    private val issueRepository: IssueRepository,
    private val subscriptionRepository: SeriesSubscriptionRepository
) : AbstractControllerTest(userRepository, jwtService) {

    @AfterEach
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun afterEach() {
        userRepository.deleteAll()
        seriesRepository.deleteAll()
        issueRepository.deleteAll()
        subscriptionRepository.deleteAll()
    }

    @Test
    fun `series subscribe test`() {
        createUser()
        val jwtToken = generateToken()

        val series = seriesRepository.save(Series(null, "test", "someone"))

        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        val response = restTemplate.exchange<Void>(
            "/series/${series.id}/subscribe",
            HttpMethod.PUT,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val subscriptions = subscriptionRepository.getByUserId(getUser().id)
        assertThat(subscriptions).hasSize(1)
        assertThat(subscriptions.first()).isEqualTo(SeriesSubscription(getUser().id, series.id!!))
    }

    @Test
    fun `unsubscribe series test`() {
        createUser()
        val series = seriesRepository.save(Series(null, "test", "someone"))
        val jwtToken = generateToken()

        subscriptionRepository.save(SeriesSubscription(getUser().id, series.id!!))
        var subscriptions = subscriptionRepository.getByUserId(getUser().id)
        assertThat(subscriptions).hasSize(1)

        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")

        val response = restTemplate.exchange<Void>(
            "/series/${series.id}/unsubscribe",
            HttpMethod.PUT,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        subscriptions = subscriptionRepository.getByUserId(getUser().id)
        assertThat(subscriptions).hasSize(0)
    }

    @Test
    fun `find unsubscribed series`() {
        createUser()
        val series = seriesRepository.save(Series(null, "test", "someone"))
        val jwtToken = generateToken()

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
    fun `find subscribed series`() {
        createUser()
        val series = seriesRepository.save(Series(null, "test", "someone"))
        subscriptionRepository.save(SeriesSubscription(getUser().id, series.id!!))
        val jwtToken = generateToken()

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

    @Test
    fun `merge series`() {
        createUser()

        val series1 = seriesRepository.save(Series(null, "test", "someone"))
        val series2 = seriesRepository.save(Series(null, "test", "someone"))

        val issue1 = issueRepository.save(
            Issue(
                number = "3",
                seriesId = series1.id!!,
                pagesCount = 10,
                publicationDate = Date()
            )
        )

        val issue2 = issueRepository.save(
            issue1.copy(
                id = null,
                number = "4",
                seriesId = series2.id!!
            )
        )

        val headers = HttpHeaders()
        headers.setBearerAuth(generateToken())

        val request = SeriesMergeRequest(listOf(series1.id, series2.id))

        restTemplate.exchange<Void>(
            "/series/merge",
            HttpMethod.PUT,
            HttpEntity(request, headers),
        )

        val actual = restTemplate.exchange<List<SeriesCatalogItemView>>(
            "/series",
            HttpMethod.GET,
            HttpEntity<Void>(headers),
        )

        assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(actual.body?.size).isEqualTo(1)

        val expectedView = SeriesCatalogItemView(
            id = series1.id,
            title = "${series1.title} (${Calendar.getInstance().get(Calendar.YEAR)})",
            issuesCount = 2,
            publisher =  series1.publisher,
            cover = "/pages/${issue2.id}/0"
        )

        assertThat(actual.body).isEqualTo(listOf(expectedView))
    }

    object Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of().applyTo(configurableApplicationContext.environment)
        }
    }

}