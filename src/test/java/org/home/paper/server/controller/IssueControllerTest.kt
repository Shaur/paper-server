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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class IssueControllerTest @Autowired constructor(
    userRepository: UserRepository,
    jwtService: JwtService,
    private val restTemplate: TestRestTemplate,
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

        val response = restTemplate.exchange<Void>(
            "/issue/${issue.id}",
            HttpMethod.PUT,
            HttpEntity(updateBody, headers)
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val progress = readingProgressRepository.getReferenceById(ReadingProgressKey(getUser().id, issue.id!!))
        val expectedProgress = ReadingProgress(
            userId = getUser().id,
            issueId = issue.id,
            currentPage = 3,
            updateTime = 40L
        )

        assertThat(progress).isEqualTo(expectedProgress)
    }

    @Test
    fun `delete issue without permission`() {
        createUser()
        val jwtToken = generateToken()
        val series = seriesRepository.save(unsavedSeries)
        val issue = issueRepository.save(unsavedIssue(series.id!!))

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)

        val response = restTemplate.exchange<Void>(
            "/issue/${issue.id}",
            HttpMethod.DELETE,
            HttpEntity(null, headers)
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `delete issue`() {
        createAdmin()
        val jwtToken = generateToken()

        val series = seriesRepository.save(unsavedSeries)
        val issue = issueRepository.save(unsavedIssue(series.id!!))

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)

        val response = restTemplate.exchange<Void>(
            "/issue/${issue.id}",
            HttpMethod.DELETE,
            HttpEntity(null, headers)
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

}