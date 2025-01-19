package org.home.paper.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatusCode;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void actuatorTest() {
        var healthStatusResponse = restTemplate.getForEntity("/actuator/health", HeathStatus.class);
        assertThat(healthStatusResponse.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));

        var healthStatus = healthStatusResponse.getBody();
        assertThat(healthStatus).isEqualTo(new HeathStatus("UP"));
    }

    private record HeathStatus(String status) {}
}
