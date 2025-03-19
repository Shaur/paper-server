package org.home.paper.server.controller

import org.assertj.core.api.Assertions.assertThat
import org.home.paper.server.Application
import org.home.paper.server.model.ArchiveMeta
import org.home.paper.server.model.PurgatoryItem
import org.home.paper.server.repository.PurgatoryRepository
import org.home.paper.server.repository.UserRepository
import org.home.paper.server.service.JwtService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.io.File

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
class ComicsPrivateApiControllerTest @Autowired constructor(
    userRepository: UserRepository,
    jwtService: JwtService,
    private val restTemplate: TestRestTemplate,
    private val purgatoryRepository: PurgatoryRepository
) : AbstractControllerTest(userRepository, jwtService) {

    @Value("\${storage.purgatory-path}")
    private lateinit var imageDir: String

    @AfterEach
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    fun afterEach() {
        userRepository.deleteAll()
        purgatoryRepository.deleteAll()
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

        val response = restTemplate.exchange<Void>(
            "/private/comics/purgatory/${savedItem.id}/0",
            HttpMethod.DELETE,
            HttpEntity<Void>(headers)
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val updatedIssue = purgatoryRepository.get(savedItem.id!!)
        assertThat(updatedIssue?.meta?.pagesCount).isEqualTo(2)
    }

}