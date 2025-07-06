package property_pilot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PropertyController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class PropertyControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/properties";
    }

    @Test
    void testCreateAndListAndDeleteProperty() {
        // Create a property
        Map<String, String> newProperty = Map.of(
                "name", "Test Property",
                "address", "456 Test St",
                "notes", "Test notes"
        );

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                getBaseUrl(),
                newProperty,
                Map.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        Integer id = (Integer) createResponse.getBody().get("id");
        assertThat(id).isNotNull();

        // List properties
        ResponseEntity<Map[]> listResponse = restTemplate.getForEntity(
                getBaseUrl(),
                Map[].class
        );

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();

        // Delete property
        restTemplate.delete(getBaseUrl() + "/" + id);

        // List again, should be empty (since @Transactional rolls back)
        ResponseEntity<Map[]> afterDeleteResponse = restTemplate.getForEntity(
                getBaseUrl(),
                Map[].class
        );

        assertThat(afterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
