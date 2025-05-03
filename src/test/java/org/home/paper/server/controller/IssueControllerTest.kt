package org.home.paper.server.controller

import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.home.paper.server.Dummies.unsavedIssue
import org.home.paper.server.Dummies.unsavedSeries
import org.home.paper.server.Dummies.unsavedUser
import org.home.paper.server.dto.ReadingProgressUpdate
import org.home.paper.server.model.*
import org.home.paper.server.repository.IssueRepository
import org.home.paper.server.repository.ReadingProgressRepository
import org.home.paper.server.repository.SeriesRepository
import org.home.paper.server.repository.UserRepository
import org.home.paper.server.service.JwtService
import org.home.paper.server.service.UserService
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
@ContextConfiguration(initializers = [IssueControllerTest.Initializer::class])
class IssueControllerTest @Autowired constructor(
    private val restTemplate: TestRestTemplate,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val readingProgressRepository: ReadingProgressRepository,
    private val seriesRepository: SeriesRepository,
    private val issueRepository: IssueRepository,
    private val jwtService: JwtService
) {


    @AfterEach
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun afterEach() {
        userRepository.deleteAll()
        issueRepository.deleteAll()
        seriesRepository.deleteAll()
        readingProgressRepository.deleteAll()
    }

    @Test
    fun `new progress update`() {
        val user = userService.create(unsavedUser)
        val series = seriesRepository.save(unsavedSeries)
        val issue = issueRepository.create(unsavedIssue(series.id!!))

        val jwtToken = jwtService.generateToken(userService.loadUserByUsername(user.username))

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

        val progress = readingProgressRepository.getReferenceById(ReadingProgressKey(user.id, issue.id!!))
        val expectedProgress = ReadingProgress(
            userId = user.id,
            issueId = issue.id,
            currentPage = 3,
            updateTime = 40L
        )

        assertThat(progress).isEqualTo(expectedProgress)
    }

    object Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of().applyTo(configurableApplicationContext.environment)
        }
    }
}