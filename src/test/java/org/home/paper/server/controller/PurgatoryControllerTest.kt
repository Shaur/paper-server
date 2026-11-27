package org.home.paper.server.controller

import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.home.paper.server.dto.JobView
import org.home.paper.server.model.ArchiveMeta
import org.home.paper.server.model.JobStatus
import org.home.paper.server.model.PurgatoryItem
import org.home.paper.server.repository.JobRepository
import org.home.paper.server.repository.PurgatoryRepository
import org.home.paper.server.repository.UserRepository
import org.home.paper.server.service.JwtService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import java.io.File


@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureRestTestClient
class PurgatoryControllerTest @Autowired constructor(
    userRepository: UserRepository,
    jwtService: JwtService,
    private val restClient: RestTestClient,
    private val purgatoryRepository: PurgatoryRepository,
    private val jobRepository: JobRepository
) : AbstractControllerTest(userRepository, jwtService) {

    @Value($$"${storage.purgatory-path}")
    private lateinit var imageDir: String

    @AfterEach
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun afterEach() {
        userRepository.deleteAll()
        purgatoryRepository.deleteAll()
        jobRepository.deleteAll()
    }

    @Test
    fun `delete page`() {
        createUser()

        val item = PurgatoryItem(
            meta = ArchiveMeta("Test name", "1", pagesCount = 3)
        )

        val savedItem = purgatoryRepository.insert(item)

        val issueDir = File(imageDir).resolve("${savedItem.id}")
        if (!issueDir.exists()) {
            issueDir.mkdirs()
        }

        for (i in (0..2)) {
            issueDir.resolve("$i.jpg").createNewFile()
        }

        val jwtToken = generateToken()
        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)

        restClient.delete()
            .uri("/purgatory/${savedItem.id}/0")
            .headers { h -> h.addAll(headers) }
            .exchangeSuccessfully()

        val updatedIssue = purgatoryRepository.get(savedItem.id!!)
        assertThat(updatedIssue?.meta?.pagesCount).isEqualTo(2)
    }

    @ParameterizedTest
    @MethodSource("getFileNames")
    fun `upload file`(fileName: String) {
        createAdmin()
        val token = generateToken()

        val content = "fake-cbz-content".toByteArray()
        val resource = object : ByteArrayResource(content) {
            override fun getFilename() = fileName
        }

        val body = LinkedMultiValueMap<String, Any>()
        body.add("file", resource)

        val resultEntity = restClient.post()
            .uri("/purgatory")
            .headers { h -> h.setBearerAuth(token) }
            .body(body)
            .exchange()
            .expectStatus().isCreated
            .expectBody<JobView>()
            .returnResult()

        val result = resultEntity.responseBody
        assertThat(result?.fileName).isEqualTo(fileName)
        assertThat(result?.id).isNotNull

        val job = jobRepository.findAll().single()
        assertThat(job.id).isNotNull
        assertThat(job.fileName).isEqualTo(fileName)
        assertThat(job.status).isEqualTo(JobStatus.PENDING)
        assertThat(job.uploadedBy).isEqualTo(getUser().username)

        assertThat(File(job.filePath).canonicalPath).startsWith(File(imageDir).canonicalPath)
        assertThat(File(job.filePath).readBytes()).isEqualTo(content)
    }

    companion object {
        @JvmStatic
        fun getFileNames(): List<String> {
            return listOf("comics.cbz", "../../evil.cbz")
        }
    }

    @Test
    fun `upload file without token`() {
        val content = "fake-cbz-content".toByteArray()
        val resource = object : ByteArrayResource(content) {
            override fun getFilename() = "comic.cbz"
        }

        val body = LinkedMultiValueMap<String, Any>()
        body.add("file", resource)

        restClient.post()
            .uri("/purgatory")
            .body(body)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `upload file without name`() {
        createAdmin()
        val token = generateToken()

        val content = "fake-cbz-content".toByteArray()
        val resource = object : ByteArrayResource(content) {
            override fun getFilename() = null
        }

        val body = LinkedMultiValueMap<String, Any>()
        body.add("file", resource)

        restClient.post()
            .uri("/purgatory")
            .headers { h -> h.setBearerAuth(token) }
            .body(body)
            .exchange()
            .expectStatus().isBadRequest
    }

}