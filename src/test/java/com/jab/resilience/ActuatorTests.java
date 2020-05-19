package com.jab.resilience;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ActuatorTests {

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    int port;

    @Test
    public void given_app_when_callActuator_then_expectedResults() {

        webTestClient.get().uri(getAddress())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    private String getAddress() {
        return "http://localhost:" + port + "/";
    }

}
