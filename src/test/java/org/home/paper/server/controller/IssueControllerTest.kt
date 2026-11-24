package org.home.paper.server.controller

import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.home.paper.server.Dummies.unsavedIssue
import org.home.paper.server.Dummies.unsavedSeries
import org.home.paper.server.dto.IssueUpdateRequest
import org.home.paper.server.dto.ReadingProgressUpdate
import org.home.paper.server.model.ReadingProgress
import org.home.paper.server.model.ReadingProgressKey
import org.home.paper.server.repository.IssueRepository
import org.home.paper.server.repository.ReadingProgressRepository
import org.home.paper.server.repository.SeriesRepository
import org.home.paper.server.repository.UserRepository
import org.home.paper.server.service.JwtService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
class IssueControllerTest @Autowired constructor(
    userRepository: UserRepository,
    jwtService: JwtService,
    private val restClient: RestTestClient,
    private val readingProgressRepository: ReadingProgressRepository,
    private val seriesRepository: SeriesRepository,
    private val issueRepository: IssueRepository
) : AbstractControllerTest(userRepository, jwtService) {

    @AfterEach
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun afterEach() {
        userRepository.deleteAll()
    }

    @Test
    fun `new progress update`() {
        createUser()
        val jwtToken = generateToken()

        val series = seriesRepository.save(unsavedSeries)
        val issue = issueRepository.save(unsavedIssue(series.id!!))

        val updateBody = ReadingProgressUpdate(
            currentPage = 3,
            updateTime = 40L
        )

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)

        restClient.put()
            .uri("/issue/${issue.id}")
            .body(updateBody)
            .headers { h -> h.addAll(headers) }
            .exchangeSuccessfully()

        val progress = readingProgressRepository.findById(ReadingProgressKey(getUser().id, issue.id!!))
        val expectedProgress = ReadingProgress(
            userId = getUser().id,
            issueId = issue.id!!,
            currentPage = 3,
            updateTime = 40L
        )

        assertThat(progress.get()).isEqualTo(expectedProgress)
    }

    @Test
    fun `delete issue without permission`() {
        createUser()
        val jwtToken = generateToken()
        val series = seriesRepository.save(unsavedSeries)
        val issue = issueRepository.save(unsavedIssue(series.id!!))

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)

        val response = restClient.delete()
            .uri("/issue/${issue.id}")
            .headers{h -> h.addAll(headers) }
            .exchange()

        response.expectStatus().isForbidden
    }

    @Test
    fun `delete issue`() {
        createAdmin()
        val jwtToken = generateToken()

        val series = seriesRepository.save(unsavedSeries)
        val issue = issueRepository.save(unsavedIssue(series.id!!))

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)

        restClient.delete()
            .uri("/issue/${issue.id}")
            .headers{h -> h.addAll(headers) }
            .exchangeSuccessfully()
    }

    @ParameterizedTest
    @MethodSource("getIssueUpdate")
    fun `update issue`(update: IssueUpdateRequest) {
        createAdmin()

        val jwtToken = generateToken()

        val series = seriesRepository.save(unsavedSeries)
        val issue = issueRepository.save(unsavedIssue(series.id!!))

        restClient.patch()
            .uri("/issue/${issue.id}")
            .headers { h -> h.setBearerAuth(jwtToken) }
            .body(update)
            .exchangeSuccessfully()

        val savedIssue = issueRepository.getById(issue.id!!)
        assertThat(savedIssue).isNotNull

        val expectedIssue = issue.copy(
            number = update.number ?: issue.number,
            publicationDate = Timestamp((update.publicationDate ?: issue.publicationDate).time),
        )

        assertThat(savedIssue).isEqualTo(expectedIssue)
    }

    companion object {

        @JvmStatic
        private fun getIssueUpdate(): List<IssueUpdateRequest> {
            val twoDaysAgo = Date.from(Instant.now().minus(2, ChronoUnit.DAYS))
            return listOf(
                IssueUpdateRequest(number = "5"),
                IssueUpdateRequest(publicationDate = twoDaysAgo),
                IssueUpdateRequest(number = "5", publicationDate = twoDaysAgo),
            )
        }
    }

}