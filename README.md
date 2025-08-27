# 🏃‍♂️ FitMatch

**Find Your Perfect Fitness Partner, Anytime, Anywhere**

Are you tired of working out alone? Struggling to find motivation for your fitness routine? Looking for like-minded individuals who share your passion for staying active? **FitMatch** is the solution you've been waiting for.

FitMatch connects fitness enthusiasts by location, activity preferences, and fitness levels, making it easier than ever to find workout partners, join group activities, and build a supportive fitness community around you. Whether you're a beginner looking for encouragement or an experienced athlete seeking new challenges, FitMatch helps you discover local fitness events and connect with people who will keep you motivated on your health journey.

## ✨ Key Features

- **🎯 Smart Matching**: Find fitness partners based on location, activity preferences, and fitness levels
- **📍 Location-Based Discovery**: Discover fitness events and partners within your preferred radius
- **🏅 Activity Diversity**: Support for 15+ activities across 5 categories (Endurance, Strength, Flexibility, Team Sports, Outdoor)
- **📅 Event Management**: Create, join, and manage fitness events with ease
- **🔐 Secure Authentication**: JWT-based authentication with profile completion workflow
- **🌐 Microservices Architecture**: Scalable and maintainable distributed system

## 🛠️ Tech Stack

### Backend
- **Java 21** - Modern Java with latest features
- **Spring Boot 3.5.5** - Production-ready application framework
- **Spring Cloud 2025.0.0** - Microservices ecosystem
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **Spring Cloud Gateway** - API gateway and routing
- **Eureka Discovery** - Service registry and discovery

### Database & Persistence
- **PostgreSQL with PostGIS** - Spatial database for location-based queries
- **Hibernate** - ORM with spatial data support
- **JTS (Java Topology Suite)** - Geometric data handling

### Security & Authentication
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing

### Development & Code Quality
- **Maven** - Build automation and dependency management
- **Spotless** - Code formatting (Google Java Format)
- **Lombok** - Boilerplate code reduction
- **JUnit 5** - Unit testing framework

### Infrastructure
- **Docker & Docker Compose** - Containerization
- **Multiple Database Support** - Separate databases for different services

## 🚀 Getting Started

### Prerequisites

Before running FitMatch, make sure you have the following installed:

- **Java 21** or higher
- **Maven 3.9+**
- **Docker & Docker Compose**
- **Git**

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/fitmatch.git
   cd fitmatch
   ```

2. **Start the databases**
   ```bash
   docker-compose up -d
   ```
   This will start:
   - User database (PostgreSQL with PostGIS) on port `5433`
   - Events database (PostgreSQL with PostGIS) on port `5434`

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Start the services** (in separate terminals or use your IDE)
   
   Start services in this order:
   
   ```bash
   # 1. Discovery Service (Eureka Server)
   cd discovery && mvn spring-boot:run
   
   # 2. User Service
   cd user && mvn spring-boot:run
   
   # 3. Events Service
   cd events && mvn spring-boot:run
   
   # 4. API Gateway
   cd gateway && mvn spring-boot:run
   ```

5. **Verify the services are running**
   - Discovery Server: http://localhost:8761
   - User Service: http://localhost:8090
   - Events Service: http://localhost:8888
   - API Gateway: http://localhost:8222

## 📖 Usage Guide

### Service Ports

| Service | Port | Description |
|---------|------|-------------|
| Discovery (Eureka) | 8761 | Service registry |
| User Service | 8090 | User management and authentication |
| Events Service | 8888 | Event management |
| API Gateway | 8222 | Main entry point for all requests |
| User Database | 5433 | PostgreSQL with PostGIS |
| Events Database | 5434 | PostgreSQL with PostGIS |

### Basic Workflow

1. **Register an account** via the gateway endpoint
2. **Complete your profile** with fitness preferences and location
3. **Browse nearby events** based on your location and interests
4. **Create or join fitness events**
5. **Connect with like-minded fitness enthusiasts**

## 📚 API Documentation

All API requests should go through the API Gateway at `http://localhost:8222`.

### Authentication Endpoints

**Register a new user**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword",
  "fullName": "John Doe"
}
```

**Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword"
}
```

### User Endpoints

**Get user by ID**
```http
GET /api/users/{userId}
Authorization: Bearer {jwt-token}
```

**Complete user profile**
```http
POST /api/users/profile
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "fitnessLevel": "INTERMEDIATE",
  "activityInterests": ["RUNNING", "CYCLING", "YOGA"],
  "latitude": 40.7128,
  "longitude": -74.0060,
  "searchRadiusKm": 15
}
```

### Events Endpoints

**Create a new event**
```http
POST /api/events
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "title": "Morning Running Group",
  "description": "Join us for a refreshing morning run in Central Park!",
  "activity": "RUNNING",
  "fitnessLevel": "BEGINNER",
  "startsAt": "2024-01-15T07:00:00",
  "capacity": 10,
  "latitude": 40.7829,
  "longitude": -73.9654
}
```

**Get nearby events**
```http
GET /api/events/nearby
Authorization: Bearer {jwt-token}
```

**Join an event**
```http
POST /api/events/{eventId}/join
Authorization: Bearer {jwt-token}
```

**Leave an event**
```http
DELETE /api/events/{eventId}/leave
Authorization: Bearer {jwt-token}
```

**Delete an event** (organizer only)
```http
DELETE /api/events/{eventId}
Authorization: Bearer {jwt-token}
```

### Supported Activities

The system supports the following activities across 5 categories:

**Endurance:** Running, Cycling, Swimming, Walking, Rowing, Spinning  
**Flexibility:** Yoga, Pilates  
**Strength:** Strength Training, HIIT  
**Team Sports:** Football, Basketball  
**Outdoor:** Hiking, Climbing  

**Fitness Levels:** Beginner, Intermediate, Advanced

## 🔧 Development

### Project Structure

```
fitmatch/
├── common/              # Shared DTOs, enums, and utilities
├── discovery/           # Eureka Discovery Server
├── events/              # Events microservice
├── gateway/             # API Gateway
├── user/                # User management and authentication
├── docker-compose.yml   # Database containers
└── pom.xml             # Parent POM
```

### Code Quality

The project uses **Spotless** with Google Java Format for consistent code formatting:

```bash
# Check code formatting
mvn spotless:check

# Apply code formatting
mvn spotless:apply
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific service
cd user && mvn test
```

### Database Schema

The application uses automatic schema generation via Hibernate's `ddl-auto: update` configuration. The spatial data is handled using PostGIS extensions for location-based queries.

### Adding New Features

1. Follow the existing microservice pattern
2. Use the `common` module for shared components
3. Implement proper error handling and validation
4. Add comprehensive tests
5. Update API documentation

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Ready to find your fitness tribe?** Get started with FitMatch and transform your workout routine today! 💪

For support or questions, please open an issue in the repository.