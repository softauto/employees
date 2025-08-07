<general_rules>
When creating new entities, always place them in the `com.example.employees` package alongside existing entities like `Employee.java`. Follow the JPA entity pattern with `@Entity` annotation, proper ID generation using `@Id` and `@GeneratedValue`, and include default constructors and parameterized constructors.

When adding new REST endpoints, follow the existing controller pattern in `EmployeeController.java` with proper `@RestController` and `@RequestMapping` annotations. Use appropriate HTTP method annotations (`@GetMapping`, `@PostMapping`, `@DeleteMapping`, etc.) and follow RESTful URL conventions.

When creating new business logic, implement it in service classes following the pattern in `EmployeeService.java` with `@Service` annotation. Use dependency injection with `@Autowired` to inject repository dependencies and keep business logic separate from controller logic.

When adding data access methods, extend `JpaRepository` interface following the pattern in `EmployeeRepository.java`. Use the `@Repository` annotation and leverage Spring Data JPA's built-in methods or create custom query methods following Spring Data naming conventions.

Use Maven commands `mvn clean compile` for building and `mvn spring-boot:run` for running the application. No specific linting or formatting scripts are configured in this repository.
</general_rules>

<repository_structure>
This is a Spring Boot 3.4.0 application using Java 17 with Maven build tool. The source code follows standard Maven structure with all Java classes located in `src/main/java/com/example/employees/` package.

The application follows MVC pattern with clear separation of concerns:
- Entity layer: `Employee.java` - JPA entity representing the data model
- Repository layer: `EmployeeRepository.java` - Data access interface extending JpaRepository
- Service layer: `EmployeeService.java` - Business logic implementation
- Controller layer: `EmployeeController.java` - REST API endpoints
- Main application class: `EmployeeManagementApplication.java` - Spring Boot application entry point

No separate test directory structure exists currently. The project uses a single package structure keeping all components together for simplicity.
</repository_structure>

<dependencies_and_installation>
This project uses Maven as the build tool with dependencies managed in `pom.xml`. Run `mvn clean install` to install dependencies.

Key dependencies include:
- Spring Boot Starter Web - for REST API functionality
- Spring Data JPA - for database operations
- H2 Database - in-memory database for development
- TestNG (version 7.4.0) - testing framework
- Spring Boot Test - Spring Boot testing utilities

Requires Java 17 or higher. Ensure JAVA_HOME is properly configured before running Maven commands.
</dependencies_and_installation>

<testing_instructions>
The project includes TestNG (version 7.4.0) and Spring Boot Test dependencies in `pom.xml`. No test files currently exist in the repository.

Tests should be placed in `src/test/java/com/example/employees/` directory following Maven conventions. Create the test directory structure if it doesn't exist.

Run tests using `mvn test` command. Focus testing on:
- Service layer business logic (EmployeeService methods)
- Repository data access methods (custom queries if added)
- Controller REST endpoints (integration tests)

Use `@SpringBootTest` annotation for integration tests and `@MockBean` for mocking dependencies in unit tests.
</testing_instructions>

<pull_request_formatting>
</pull_request_formatting>
