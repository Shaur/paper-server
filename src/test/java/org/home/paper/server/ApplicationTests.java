package org.home.paper.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class ApplicationTests {

    @Autowired
    private RestTestClient restClient;

    @Test
    void contextLoads() {
    }

    @Test
    void actuatorTest() {
        restClient.get()
                .uri("/actuator/health")
                .exchangeSuccessfully()
                .expectBody(HeathStatus.class)
                .isEqualTo(new HeathStatus("UP"));
    }

    private record HeathStatus(String status) {
    }
}
