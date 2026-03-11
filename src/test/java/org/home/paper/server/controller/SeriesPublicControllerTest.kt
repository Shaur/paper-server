package org.home.paper.server.controller

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.home.paper.server.Dummies.unsavedSeries
import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.dto.SeriesMergeRequest
import org.home.paper.server.model.Issue
import org.home.paper.server.model.Series
import org.home.paper.server.model.SeriesSubscription
import org.home.paper.server.put
import org.home.paper.server.repository.IssueRepository
import org.home.paper.server.repository.SeriesRepository
import org.home.paper.server.repository.SeriesSubscriptionRepository
import org.home.paper.server.repository.UserRepository
import org.home.paper.server.service.JwtService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class SeriesPublicControllerTest @Autowired constructor(
    userRepository: UserRepository,
    jwtService: JwtService,
    private val restTemplate: TestRestTemplate,
    private val seriesRepository: SeriesRepository,
    private val issueRepository: IssueRepository,
    private val subscriptionRepository: SeriesSubscriptionRepository,
    private val entityManager: EntityManager,
    private val transactionTemplate: TransactionTemplate
) : AbstractControllerTest(userRepository, jwtService) {

    @AfterEach
    fun afterEach() {
        val tables = listOf("user_data", "series", "issue", "series_subscription")
            .joinToString(", ")

        transactionTemplate.executeWithoutResult {
            entityManager.createNativeQuery("truncate table $tables").executeUpdate()
            entityManager.clear()
        }
    }

    @Test
    fun `series subscribe`() {
        createUser()

        val series = seriesRepository.save(unsavedSeries)

        val response = restTemplate.put<Void>(
            "/series/${series.id}/subscribe",
            entity = (null to authHeaders())
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val subscriptions = subscriptionRepository.getByUserId(getUser().id)
        assertThat(subscriptions)
            .hasSize(1)
            .first()
            .isEqualTo(SeriesSubscription(getUser().id, series.id!!))
    }

    @Test
    fun `unsubscribe series`() {
        createUser()

        val series = seriesRepository.save(unsavedSeries)

        subscriptionRepository.save(SeriesSubscription(getUser().id, series.id!!))

        var subscriptions = subscriptionRepository.getByUserId(getUser().id)
        assertThat(subscriptions).hasSize(1)

        val response = restTemplate.put<Void>(
            "/series/${series.id}/unsubscribe",
            entity = null to authHeaders()
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        subscriptions = subscriptionRepository.getByUserId(getUser().id)
        assertThat(subscriptions).hasSize(0)
    }

    @Test
    fun `find unsubscribed series`() {
        createUser()

        val series = seriesRepository.save(Series(null, "test", "someone"))

        val response = restTemplate.exchange<List<SeriesCatalogItemView>>(
            "/series",
            HttpMethod.GET,
            HttpEntity(null, authHeaders()),
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

        val response = restTemplate.exchange<List<SeriesCatalogItemView>>(
            "/series",
            HttpMethod.GET,
            HttpEntity(null, authHeaders()),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotEmpty

        val seriesDescription = response.body?.first()
        assertThat(seriesDescription?.id).isEqualTo(series.id)
        assertThat(seriesDescription?.title).isEqualTo(series.title)
        assertThat(seriesDescription?.subscribed).isEqualTo(true)
    }

    @Test
    fun `find by title part`() {
        createUser()

        val series = seriesRepository.save(Series(null, "Amazing Spider-Man", "someone"))
        subscriptionRepository.save(SeriesSubscription(getUser().id, series.id!!))

        val response = restTemplate.exchange<List<SeriesCatalogItemView>>(
            "/series?titlePart=Spider",
            HttpMethod.GET,
            HttpEntity(null, authHeaders()),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotEmpty

        val seriesDescription = response.body?.first()
        assertThat(seriesDescription?.id).isEqualTo(series.id)
        assertThat(seriesDescription?.title).isEqualTo(series.title)
        assertThat(seriesDescription?.subscribed).isEqualTo(true)

        val emptyResponse = restTemplate.exchange<List<SeriesCatalogItemView>>(
            "/series?titlePart=Batman",
            HttpMethod.GET,
            HttpEntity(null, authHeaders()),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(emptyResponse.body).isEmpty()
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

        val request = SeriesMergeRequest(listOf(series1.id, series2.id))

        val headers = authHeaders()

        restTemplate.put<Void>(
            "/series/merge",
            entity = (request to headers),
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
            publisher = series1.publisher,
            cover = "/pages/${issue2.id}/0",
            ended = false
        )

        assertThat(actual.body).isEqualTo(listOf(expectedView))
    }

}