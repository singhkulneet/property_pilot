package property_pilot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ExpenseController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class ExpenseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void testCreateAndListAndDeleteExpense() {
        // First, create a property
        Map<String, String> newProperty = Map.of(
                "name", "Test Property",
                "address", "789 Test Ave",
                "notes", "Test property for expenses"
        );

        ResponseEntity<Map> propertyResponse = restTemplate.postForEntity(
                getBaseUrl("/api/properties"),
                newProperty,
                Map.class
        );

        assertThat(propertyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Integer propertyId = (Integer) propertyResponse.getBody().get("id");
        assertThat(propertyId).isNotNull();

        // Create an expense linked to the property
        Map<String, Object> newExpense = Map.of(
                "property", Map.of("id", propertyId),
                "date", LocalDate.now().toString(),
                "category", "mortgage",
                "amount", new BigDecimal("1500.00"),
                "description", "Test mortgage payment"
        );

        ResponseEntity<Map> expenseResponse = restTemplate.postForEntity(
                getBaseUrl("/api/expenses"),
                newExpense,
                Map.class
        );

        assertThat(expenseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Integer expenseId = (Integer) expenseResponse.getBody().get("id");
        assertThat(expenseId).isNotNull();

        // List all expenses
        ResponseEntity<Map[]> listResponse = restTemplate.getForEntity(
                getBaseUrl("/api/expenses"),
                Map[].class
        );

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();

        // Delete the expense
        restTemplate.delete(getBaseUrl("/api/expenses/" + expenseId));

        // Confirm deletion
        ResponseEntity<Map[]> afterDeleteResponse = restTemplate.getForEntity(
                getBaseUrl("/api/expenses"),
                Map[].class
        );

        assertThat(afterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testUploadReceipt() {
        // Create a property
        Map<String, String> newProperty = Map.of(
                "name", "Receipt Property",
                "address", "999 Receipt St",
                "notes", "Test property for upload"
        );

        ResponseEntity<Map> propertyResponse = restTemplate.postForEntity(
                getBaseUrl("/api/properties"),
                newProperty,
                Map.class
        );

        assertThat(propertyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Integer propertyId = (Integer) propertyResponse.getBody().get("id");
        assertThat(propertyId).isNotNull();

        // Create an expense linked to the property
        Map<String, Object> newExpense = Map.of(
                "property", Map.of("id", propertyId),
                "date", LocalDate.now().toString(),
                "category", "maintenance",
                "amount", new BigDecimal("250.00"),
                "description", "Test upload expense"
        );

        ResponseEntity<Map> expenseResponse = restTemplate.postForEntity(
                getBaseUrl("/api/expenses"),
                newExpense,
                Map.class
        );

        assertThat(expenseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Integer expenseId = (Integer) expenseResponse.getBody().get("id");
        assertThat(expenseId).isNotNull();

        // Upload a file
        String uploadUrl = getBaseUrl("/api/expenses/" + expenseId + "/upload");

        // Create a small test file
        byte[] fileContent = "This is a test receipt.".getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Build multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "test_receipt.txt";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> uploadResponse = restTemplate.postForEntity(
                uploadUrl,
                requestEntity,
                String.class
        );

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(uploadResponse.getBody()).contains("File uploaded successfully");

        // Re-fetch the expense and check receiptPath is set
        ResponseEntity<List> getExpenseResponse = restTemplate.getForEntity(
                getBaseUrl("/api/expenses"),
                List.class
        );

        assertThat(getExpenseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List expenses = getExpenseResponse.getBody();
        assertThat(expenses).isNotEmpty();

        boolean receiptPathFound = false;
        for (Object obj : expenses) {
            Map e = (Map) obj;
            Integer id = (Integer) e.get("id");
            if (id != null && id.equals(expenseId)) {
                Object receiptPath = e.get("receiptPath");
                assertThat(receiptPath).isNotNull();
                receiptPathFound = true;
            }
        }
        assertThat(receiptPathFound).isTrue();
    }

    @Test
    void testDownloadReceipt() {
        // Step 1: Create a property
        Map<String, String> newProperty = Map.of(
                "name", "Download Property",
                "address", "123 Download St",
                "notes", "Test property for download"
        );

        ResponseEntity<Map> propertyResponse = restTemplate.postForEntity(
                getBaseUrl("/api/properties"),
                newProperty,
                Map.class
        );

        assertThat(propertyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Integer propertyId = (Integer) propertyResponse.getBody().get("id");
        assertThat(propertyId).isNotNull();

        // Step 2: Create an expense linked to the property
        Map<String, Object> newExpense = Map.of(
                "property", Map.of("id", propertyId),
                "date", LocalDate.now().toString(),
                "category", "utilities",
                "amount", new BigDecimal("50.00"),
                "description", "Test download expense"
        );

        ResponseEntity<Map> expenseResponse = restTemplate.postForEntity(
                getBaseUrl("/api/expenses"),
                newExpense,
                Map.class
        );

        assertThat(expenseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Integer expenseId = (Integer) expenseResponse.getBody().get("id");
        assertThat(expenseId).isNotNull();

        // Step 3: Upload a test file
        byte[] fileContent = "Test download receipt.".getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "download_receipt.txt";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> uploadResponse = restTemplate.postForEntity(
                getBaseUrl("/api/expenses/" + expenseId + "/upload"),
                requestEntity,
                String.class
        );

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Step 4: Download the receipt
        ResponseEntity<byte[]> downloadResponse = restTemplate.getForEntity(
                getBaseUrl("/api/expenses/" + expenseId + "/receipt"),
                byte[].class
        );

        assertThat(downloadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(downloadResponse.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(downloadResponse.getBody()).isNotNull();
        assertThat(new String(downloadResponse.getBody())).contains("Test download receipt.");
    }

    @Test
    void testDownloadReceiptNotFound() {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
            getBaseUrl("/api/expenses/9999/receipt"),
            byte[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
} // END: public class ExpenseControllerTest 
