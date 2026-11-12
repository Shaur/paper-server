package org.home.paper.server.controller

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.home.paper.server.Dummies.unsavedSeries
import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.dto.SeriesMergeRequest
import org.home.paper.server.dto.SeriesSplitRequest
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
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
class SeriesPublicControllerTest @Autowired constructor(
    userRepository: UserRepository,
    jwtService: JwtService,
    private val restClient: RestTestClient,
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

        restClient.put()
            .uri("/series/${series.id}/subscribe")
            .headers { headers -> headers.addAll(authHeaders()) }
            .exchangeSuccessfully()

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

        restClient.put()
            .uri("/series/${series.id}/unsubscribe")
            .headers { headers -> headers.addAll(authHeaders()) }
            .exchangeSuccessfully()

        subscriptions = subscriptionRepository.getByUserId(getUser().id)
        assertThat(subscriptions).hasSize(0)
    }

    @Test
    fun `find unsubscribed series`() {
        createUser()

        val series = seriesRepository.save(Series(null, "test", "someone"))

        val response = restClient.get()
            .uri("/series")
            .headers { headers -> headers.addAll(authHeaders()) }
            .exchangeSuccessfully()

        response.expectBody<List<SeriesCatalogItemView>>()
            .value { values ->
                assertThat(values).isNotEmpty

                val seriesDescription = values?.first()
                assertThat(seriesDescription?.id).isEqualTo(series.id)
                assertThat(seriesDescription?.title).isEqualTo(series.title)
                assertThat(seriesDescription?.subscribed).isEqualTo(false)
            }
    }

    @Test
    fun `find subscribed series`() {
        createUser()

        val series = seriesRepository.save(Series(null, "test", "someone"))
        subscriptionRepository.save(SeriesSubscription(getUser().id, series.id!!))

        val response = restClient.get()
            .uri("/series")
            .headers { headers -> headers.addAll(authHeaders()) }
            .exchangeSuccessfully()

        response.expectBody<List<SeriesCatalogItemView>>()
            .value { values ->
                assertThat(values).isNotEmpty

                val seriesDescription = values?.first()
                assertThat(seriesDescription?.id).isEqualTo(series.id)
                assertThat(seriesDescription?.title).isEqualTo(series.title)
                assertThat(seriesDescription?.subscribed).isEqualTo(true)
            }
    }

    @Test
    fun `find by title part`() {
        createUser()

        val series = seriesRepository.save(Series(null, "Amazing Spider-Man", "someone"))
        subscriptionRepository.save(SeriesSubscription(getUser().id, series.id!!))

        val response = restClient.get()
            .uri("/series?titlePart=Spider")
            .headers { headers -> headers.addAll(authHeaders()) }
            .exchangeSuccessfully()

        response.expectBody<List<SeriesCatalogItemView>>()
            .value { values ->
                assertThat(values).isNotEmpty

                val seriesDescription = values?.first()
                assertThat(seriesDescription?.id).isEqualTo(series.id)
                assertThat(seriesDescription?.title).isEqualTo(series.title)
                assertThat(seriesDescription?.subscribed).isEqualTo(true)
            }

        val emptyResponse = restClient.get()
            .uri("/series?titlePart=Batman")
            .headers { headers -> headers.addAll(authHeaders()) }
            .exchangeSuccessfully()

        emptyResponse.expectBody<List<SeriesCatalogItemView>>()
            .value { values -> assertThat(values).isEmpty() }
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

        val request = SeriesMergeRequest(listOf(series1.id!!, series2.id!!))

        val headers = authHeaders()

        restClient.put()
            .uri("/series/merge")
            .body(request)
            .headers { h -> h.addAll(headers) }
            .exchangeSuccessfully()

        val actual = restClient.get()
            .uri("/series")
            .headers { h -> h.addAll(headers) }
            .exchangeSuccessfully()

        val expectedView = SeriesCatalogItemView(
            id = series1.id!!,
            title = "${series1.title} (${Calendar.getInstance().get(Calendar.YEAR)})",
            issuesCount = 2,
            publisher = series1.publisher,
            cover = "/pages/${issue2.id}/0",
            ended = false
        )

        actual.expectBody<List<SeriesCatalogItemView>>()
            .value { values ->
                assertThat(values?.size).isEqualTo(1)
                assertThat(values).isEqualTo(listOf(expectedView))
            }
    }

    @Test
    fun `split series`() {
        createAdmin()

        val series = seriesRepository.save(Series(null, "test", "someone"))
        val seriesId = series.id!!
        val issue1 = issueRepository.save(
            Issue(
                number = "3",
                seriesId = seriesId,
                pagesCount = 10,
                publicationDate = Date()
            )
        )

        val issue2 = issueRepository.save(
            issue1.copy(
                id = null,
                number = "4",
                seriesId = seriesId
            )
        )

        val request = SeriesSplitRequest(
            oldSeriesId = seriesId,
            issuesIds = listOf(issue2.id!!)
        )

        val headers = authHeaders()

        restClient.put()
            .uri("/series/split")
            .body(request)
            .headers { h -> h.addAll(headers) }
            .exchangeSuccessfully()

        val actual = restClient.get()
            .uri("/series")
            .headers { h -> h.addAll(headers) }
            .exchangeSuccessfully()

        actual.expectBody<List<SeriesCatalogItemView>>()
            .value { values ->
                assertThat(values?.size).isEqualTo(2)

                assertThat(values).matches {
                    for (view in it) {
                        assertThat(view.title)
                            .isEqualTo("${series.title} (${Calendar.getInstance().get(Calendar.YEAR)})")
                        assertThat(view.publisher).isEqualTo(series.publisher)
                        assertThat(view.issuesCount).isEqualTo(1)
                        assertThat(view.ended).isEqualTo(false)
                        assertThat(view.cover).containsAnyOf("/pages/${issue1.id}/0", "/pages/${issue2.id}/0")
                    }

                    return@matches true
                }
            }
    }

}