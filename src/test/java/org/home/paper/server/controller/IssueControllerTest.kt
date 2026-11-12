package org.home.paper.server.controller

import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.home.paper.server.Dummies.unsavedIssue
import org.home.paper.server.Dummies.unsavedSeries
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

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

}