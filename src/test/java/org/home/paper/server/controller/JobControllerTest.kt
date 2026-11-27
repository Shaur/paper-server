package org.home.paper.server.controller

import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.home.paper.server.dto.JobView
import org.home.paper.server.model.Job
import org.home.paper.server.model.JobStatus
import org.home.paper.server.repository.JobRepository
import org.home.paper.server.repository.UserRepository
import org.home.paper.server.service.JwtService
import org.home.paper.server.service.toView
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureRestTestClient
class JobControllerTest @Autowired constructor(
    userRepository: UserRepository,
    jwtService: JwtService,
    private val restClient: RestTestClient,
    private val jobRepository: JobRepository
) : AbstractControllerTest(userRepository, jwtService) {

    companion object {
        private fun job() = Job(
            id = null,
            fileName = "comics.cbz",
            filePath = "testdata/comics.cbz",
            uploadedBy = "admin",
            status = JobStatus.FAILED,
            updateTime = LocalDateTime.now().withNano(0),
            errorMessage = "Some error",
            attempts = 3
        )
    }

    private lateinit var token: String

    @AfterEach
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun afterEach() {
        userRepository.deleteAll()
        jobRepository.deleteAll()
    }

    @BeforeEach
    fun beforeEach() {
        createAdmin()
        token = generateToken()
    }

    @Test
    fun `resolve failed job`() {
        val savedJob = jobRepository.save(job())

        restClient.post()
            .uri("/jobs/${savedJob.id}/resolve")
            .headers { h -> h.setBearerAuth(token) }
            .exchangeSuccessfully()

        val updatedJob = jobRepository.findByIdOrNull(savedJob.id!!)
        assertNotNull(updatedJob)
        assertThat(updatedJob?.status).isEqualTo(JobStatus.DONE)
    }

    @Test
    fun `resolve job in wrong status`() {
        val savedJob = jobRepository.save(job().copy(status = JobStatus.PENDING))

        restClient.post()
            .uri("/jobs/${savedJob.id}/resolve")
            .headers { h -> h.setBearerAuth(token) }
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `resolve non existing job`() {
        restClient.post()
            .uri("/jobs/500/resolve")
            .headers { h -> h.setBearerAuth(token) }
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `get job by id`() {
        val savedJob = jobRepository.save(job())
        val expectedView = savedJob.toView()

        restClient.get()
            .uri("/jobs/${savedJob.id}")
            .headers { h -> h.setBearerAuth(token) }
            .exchangeSuccessfully()
            .expectBody<JobView>()
            .isEqualTo(expectedView)
    }

    @Test
    fun `get not existing job by id`() {
        restClient.get()
            .uri("/jobs/500")
            .headers { h -> h.setBearerAuth(token) }
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `get all jobs`() {
        val expectedJobs = listOf(job(), job().copy(status = JobStatus.DONE))
            .map(jobRepository::save)
            .map(Job::toView)
            .sortedByDescending { it.id }

        restClient.get()
            .uri("/jobs")
            .headers { h -> h.setBearerAuth(token) }
            .exchangeSuccessfully()
            .expectBody(object : ParameterizedTypeReference<List<JobView>>() {})
            .isEqualTo(expectedJobs)
    }

    @Test
    fun `get empty process job`() {
        restClient.get()
            .uri("/jobs")
            .headers { h -> h.setBearerAuth(token) }
            .exchangeSuccessfully()
            .expectBody(object : ParameterizedTypeReference<List<JobView>>() {})
            .isEqualTo(emptyList())
    }
}