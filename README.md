# Customer Management API

A Spring Boot RESTful API for managing customer data with dynamic membership tier calculation based on annual spend and purchase history.

## Features

- **CRUD Operations**: Create, read, update, and delete customer records
- **Dynamic Membership Tiers**: Real-time calculation of customer membership tiers based on business rules
- **UUID-based IDs**: Auto-generated UUID identifiers for customers
- **Email Validation**: Basic email format validation
- **Search Functionality**: Search customers by name or email
- **OpenAPI Documentation**: Comprehensive API documentation with Swagger UI
- **In-Memory Database**: H2 database for local development and testing
- **Exception Handling**: Global exception handling with meaningful error responses
- **Unit & Integration Tests**: Comprehensive test coverage

## Business Rules - Membership Tiers

The membership tier is calculated on-the-fly based on the following rules:

- **Silver**: Annual spend < $1,000 (default tier)
- **Gold**: Annual spend ≥ $1,000 AND purchased within the last 12 months AND not qualified for Platinum
- **Platinum**: Annual spend ≥ $10,000 AND purchased within the last 6 months

**Note**: The membership tier is calculated dynamically and is not stored in the database.

## Customer Data Model

Each customer has the following fields:

- `id` (UUID, auto-generated): Unique identifier for the customer
- `name` (String, required): Customer's full name
- `email` (String, required): Customer's email address with format validation
- `annualSpend` (Decimal, optional): Total annual spending amount
- `lastPurchaseDate` (ISO 8601 Date, optional): Date of the customer's last purchase
- `membershipTier` (Calculated): Dynamically calculated membership tier (SILVER, GOLD, PLATINUM)

## API Endpoints

### Customer Management

#### Create Customer
```
POST /customers
```
Creates a new customer record. The ID field is auto-generated and should not be included in the request.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "annualSpend": 2500.00,
  "lastPurchaseDate": "2024-09-01"
}
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "annualSpend": 2500.00,
  "lastPurchaseDate": "2024-09-01",
  "membershipTier": "GOLD"
}
```

#### Get Customer by ID
```
GET /customers/{id}
```
Retrieves a specific customer by their UUID.

#### Get Customers with Query Parameters
```
GET /customers
GET /customers?name={name}
GET /customers?email={email}
```
- No parameters: Returns all customers
- `name` parameter: Searches for customers by name (partial match)
- `email` parameter: Finds customer with exact email match

#### Get Customers by Membership Tier
```
GET /customers/tier/{tier}
```
Retrieves customers by their calculated membership tier (SILVER, GOLD, PLATINUM).

#### Update Customer
```
PUT /customers/{id}
```
Updates an existing customer record. All fields except `id` can be updated.

#### Delete Customer
```
DELETE /customers/{id}
```
Deletes a customer record by ID.

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+ or Gradle 7+

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd customer-management-api
   ```

2. **Build and run with Maven:**
   ```bash
   ./mvnw spring-boot:run
   ```
3. **Access the application:**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Documentation: `http://localhost:8080/api-docs`
   - H2 Console: `http://localhost:8080/h2-console`

### H2 Database Configuration

For local development, the application uses H2 in-memory database:
- **URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: `password`

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report
```

### Test Coverage

The application includes:
- **Unit Tests**: For individual components (services, mappers, models)
- **Integration Tests**: For full API testing with database
- **Controller Tests**: For REST endpoint testing

### Example API Calls

#### Create a new customer:
```bash
curl -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "annualSpend": 1500.00,
    "lastPurchaseDate": "2024-09-15"
  }'
```

#### Get all customers:
```bash
curl http://localhost:8080/customers
```

#### Search customers by name:
```bash
curl "http://localhost:8080/customers?name=John"
```

#### Get customers by membership tier:
```bash
curl http://localhost:8080/customers/tier/GOLD
```

## Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=password

# H2 Console (Development Only)
spring.h2.console.enabled=true

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# OpenAPI Configuration
springdoc.swagger-ui.path=/swagger-ui.html
```
r details.
