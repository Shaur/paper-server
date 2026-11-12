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
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.io.File

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureRestTestClient
class ComicsPrivateApiControllerTest @Autowired constructor(
    userRepository: UserRepository,
    jwtService: JwtService,
    private val restClient: RestTestClient,
    private val purgatoryRepository: PurgatoryRepository
) : AbstractControllerTest(userRepository, jwtService) {

    @Value($$"${storage.purgatory-path}")
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

        restClient.delete()
            .uri("/private/comics/purgatory/${savedItem.id}/0")
            .headers { h -> h.addAll(headers) }
            .exchangeSuccessfully()

        val updatedIssue = purgatoryRepository.get(savedItem.id!!)
        assertThat(updatedIssue?.meta?.pagesCount).isEqualTo(2)
    }

}