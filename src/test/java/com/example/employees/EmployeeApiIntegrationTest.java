package com.example.employees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EmployeeApiIntegrationTest extends AbstractTestNGSpringContextTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmployeeRepository employeeRepository;

    private String baseUrl;
    private Employee testEmployee1;
    private Employee testEmployee2;

    @BeforeMethod
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/employees";
        
        // Create test employees
        testEmployee1 = new Employee("John Doe", "Engineering", 75000.0);
        testEmployee2 = new Employee("Jane Smith", "Marketing", 65000.0);
        
        // Clean database before each test
        employeeRepository.deleteAll();
    }

    @AfterMethod
    public void tearDown() {
        // Clean database after each test
        employeeRepository.deleteAll();
    }

    // ========== GET /employees Tests ==========

    @Test
    public void testGetAllEmployees_EmptyDatabase() {
        ResponseEntity<Employee[]> response = restTemplate.getForEntity(baseUrl, Employee[].class);
        
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(response.getBody().length, 0);
    }

    @Test
    public void testGetAllEmployees_WithData() {
        // Save test employees to database
        Employee saved1 = employeeRepository.save(testEmployee1);
        Employee saved2 = employeeRepository.save(testEmployee2);

        ResponseEntity<Employee[]> response = restTemplate.getForEntity(baseUrl, Employee[].class);
        
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(response.getBody().length, 2);
        
        Employee[] employees = response.getBody();
        
        // Verify first employee
        assertEquals(employees[0].getId(), saved1.getId());
        assertEquals(employees[0].getName(), "John Doe");
        assertEquals(employees[0].getDepartment(), "Engineering");
        assertEquals(employees[0].getSalary(), 75000.0);
        
        // Verify second employee
        assertEquals(employees[1].getId(), saved2.getId());
        assertEquals(employees[1].getName(), "Jane Smith");
        assertEquals(employees[1].getDepartment(), "Marketing");
        assertEquals(employees[1].getSalary(), 65000.0);
    }

    // ========== GET /employees/{id} Tests ==========

    @Test
    public void testGetEmployeeById_Success() {
        Employee savedEmployee = employeeRepository.save(testEmployee1);
        
        ResponseEntity<Employee> response = restTemplate.getForEntity(
            baseUrl + "/" + savedEmployee.getId(), Employee.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        
        Employee employee = response.getBody();
        assertEquals(employee.getId(), savedEmployee.getId());
        assertEquals(employee.getName(), "John Doe");
        assertEquals(employee.getDepartment(), "Engineering");
        assertEquals(employee.getSalary(), 75000.0);
    }

    @Test
    public void testGetEmployeeById_NotFound() {
        Long nonExistentId = 999L;
        
        ResponseEntity<Employee> response = restTemplate.getForEntity(
            baseUrl + "/" + nonExistentId, Employee.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        // The controller returns Optional<Employee>, which when empty becomes null in JSON
        Employee responseBody = response.getBody();
        assertTrue(responseBody == null || responseBody.getId() == null);
    }

    @Test
    public void testGetEmployeeById_InvalidId() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/invalid", String.class);
        
        // Spring will return 400 Bad Request for invalid path variable type
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    // ========== POST /employees Tests ==========

    @Test
    public void testCreateEmployee_Success() {
        ResponseEntity<Employee> response = restTemplate.postForEntity(
            baseUrl, testEmployee1, Employee.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        
        Employee createdEmployee = response.getBody();
        assertNotNull(createdEmployee.getId());
        assertEquals(createdEmployee.getName(), "John Doe");
        assertEquals(createdEmployee.getDepartment(), "Engineering");
        assertEquals(createdEmployee.getSalary(), 75000.0);
        
        // Verify employee was saved to database
        Optional<Employee> dbEmployee = employeeRepository.findById(createdEmployee.getId());
        assertTrue(dbEmployee.isPresent());
        assertEquals(dbEmployee.get().getName(), "John Doe");
        assertEquals(dbEmployee.get().getDepartment(), "Engineering");
        assertEquals(dbEmployee.get().getSalary(), 75000.0);
    }

    @Test
    public void testCreateEmployee_WithAllFields() {
        Employee employeeWithAllFields = new Employee("Alice Johnson", "HR", 55000.0);
        
        ResponseEntity<Employee> response = restTemplate.postForEntity(
            baseUrl, employeeWithAllFields, Employee.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        
        Employee createdEmployee = response.getBody();
        assertNotNull(createdEmployee.getId());
        assertEquals(createdEmployee.getName(), "Alice Johnson");
        assertEquals(createdEmployee.getDepartment(), "HR");
        assertEquals(createdEmployee.getSalary(), 55000.0);
        
        // Verify database state
        assertEquals(employeeRepository.count(), 1L);
    }

    @Test
    public void testCreateEmployee_NullEmployee() {
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl, null, String.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCreateEmployee_EmptyRequestBody() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("", headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl, request, String.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCreateEmployee_InvalidJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{invalid json}", headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl, request, String.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCreateEmployee_PartialData() {
        // Test with missing fields - should still work as Employee constructor handles nulls
        String partialJson = "{\"name\":\"Partial Employee\"}";
        HttpEntity<String> request = new HttpEntity<>(partialJson);
        
        ResponseEntity<Employee> response = restTemplate.exchange(
            baseUrl, HttpMethod.POST, request, Employee.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getName(), "Partial Employee");
        assertNull(response.getBody().getDepartment());
        assertEquals(response.getBody().getSalary(), 0.0);
    }

    // ========== DELETE /employees/{id} Tests ==========

    @Test
    public void testDeleteEmployee_Success() {
        Employee savedEmployee = employeeRepository.save(testEmployee1);
        Long employeeId = savedEmployee.getId();
        
        // Verify employee exists before deletion
        assertTrue(employeeRepository.existsById(employeeId));
        
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/" + employeeId, HttpMethod.DELETE, null, Void.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        
        // Verify employee was deleted from database
        assertFalse(employeeRepository.existsById(employeeId));
        assertEquals(employeeRepository.count(), 0L);
    }

    @Test
    public void testDeleteEmployee_NotFound() {
        Long nonExistentId = 999L;
        
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/" + nonExistentId, HttpMethod.DELETE, null, Void.class);
        
        // Spring Data JPA deleteById doesn't throw exception for non-existent ID
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testDeleteEmployee_InvalidId() {
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/invalid", HttpMethod.DELETE, null, String.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testDeleteEmployee_VerifyOtherEmployeesUnaffected() {
        Employee employee1 = employeeRepository.save(testEmployee1);
        Employee employee2 = employeeRepository.save(testEmployee2);
        
        // Delete first employee
        restTemplate.exchange(
            baseUrl + "/" + employee1.getId(), HttpMethod.DELETE, null, Void.class);
        
        // Verify only first employee was deleted
        assertFalse(employeeRepository.existsById(employee1.getId()));
        assertTrue(employeeRepository.existsById(employee2.getId()));
        assertEquals(employeeRepository.count(), 1L);
    }

    // ========== Integration Tests ==========

    @Test
    public void testFullCrudWorkflow() {
        // 1. Start with empty database
        assertEquals(employeeRepository.count(), 0L);
        
        // 2. Create employee
        ResponseEntity<Employee> createResponse = restTemplate.postForEntity(
            baseUrl, testEmployee1, Employee.class);
        assertEquals(createResponse.getStatusCode(), HttpStatus.OK);
        Employee createdEmployee = createResponse.getBody();
        assertNotNull(createdEmployee.getId());
        
        // 3. Verify employee exists in database
        assertEquals(employeeRepository.count(), 1L);
        
        // 4. Get employee by ID
        ResponseEntity<Employee> getResponse = restTemplate.getForEntity(
            baseUrl + "/" + createdEmployee.getId(), Employee.class);
        assertEquals(getResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(getResponse.getBody().getName(), "John Doe");
        
        // 5. Get all employees
        ResponseEntity<Employee[]> getAllResponse = restTemplate.getForEntity(baseUrl, Employee[].class);
        assertEquals(getAllResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(getAllResponse.getBody().length, 1);
        
        // 6. Delete employee
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            baseUrl + "/" + createdEmployee.getId(), HttpMethod.DELETE, null, Void.class);
        assertEquals(deleteResponse.getStatusCode(), HttpStatus.OK);
        
        // 7. Verify employee was deleted
        assertEquals(employeeRepository.count(), 0L);
    }

    @Test
    public void testMultipleEmployeesWorkflow() {
        // Create multiple employees
        Employee emp1 = restTemplate.postForEntity(baseUrl, testEmployee1, Employee.class).getBody();
        Employee emp2 = restTemplate.postForEntity(baseUrl, testEmployee2, Employee.class).getBody();
        Employee emp3 = restTemplate.postForEntity(baseUrl, 
            new Employee("Bob Wilson", "Finance", 70000.0), Employee.class).getBody();
        
        // Verify all employees exist
        assertEquals(employeeRepository.count(), 3L);
        
        // Get all employees and verify
        ResponseEntity<Employee[]> response = restTemplate.getForEntity(baseUrl, Employee[].class);
        assertEquals(response.getBody().length, 3);
        
        // Delete middle employee
        restTemplate.exchange(baseUrl + "/" + emp2.getId(), HttpMethod.DELETE, null, Void.class);
        
        // Verify count and remaining employees
        assertEquals(employeeRepository.count(), 2L);
        assertTrue(employeeRepository.existsById(emp1.getId()));
        assertFalse(employeeRepository.existsById(emp2.getId()));
        assertTrue(employeeRepository.existsById(emp3.getId()));
    }

    // ========== JSON Serialization/Deserialization Tests ==========

    @Test
    public void testJsonSerialization() {
        Employee employee = new Employee("Test User", "Test Dept", 50000.0);
        
        ResponseEntity<Employee> response = restTemplate.postForEntity(baseUrl, employee, Employee.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        Employee responseEmployee = response.getBody();
        
        // Verify all fields are properly serialized/deserialized
        assertNotNull(responseEmployee.getId());
        assertEquals(responseEmployee.getName(), "Test User");
        assertEquals(responseEmployee.getDepartment(), "Test Dept");
        assertEquals(responseEmployee.getSalary(), 50000.0);
    }

    @Test
    public void testSpecialCharactersInJson() {
        Employee employee = new Employee("José María", "R&D", 80000.0);
        
        ResponseEntity<Employee> response = restTemplate.postForEntity(baseUrl, employee, Employee.class);
        
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        Employee responseEmployee = response.getBody();
        
        assertEquals(responseEmployee.getName(), "José María");
        assertEquals(responseEmployee.getDepartment(), "R&D");
    }
}




