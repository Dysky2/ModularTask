# ModularTask

Zaawansowany system zarządzania zadaniami dla zespołów z modularną architekturą, wspierający rozproszoną pracę i automatyczne powiadomienia w czasie rzeczywistym.

## Spis treści

- [Opis projektu](#opis-projektu)
- [Główne funkcjonalności](#główne-funkcjonalności)
- [Architektura](#architektura)
- [Technologie](#technologie)
- [Wymagania wstępne](#wymagania-wstępne)
- [Instalacja i uruchomienie](#instalacja-i-uruchomienie)
- [Struktura projektu](#struktura-projektu)
- [API Documentation](#api-documentation)
- [Bezpieczeństwo](#bezpieczeństwo)

---

## Opis projektu

**ModularTask** to nowoczesna aplikacja webowa do zarządzania zadaniami zespołowymi zbudowana na architekturze mikroserwisów. System umożliwia:

- **Zarządzanie zadaniami** z hierarchią (zadania główne i podzadania)
- **Przydzielanie zasobów** do zadań z limitami pracowników
- **Śledzenie postępu** poprzez zmiany statusu i historię zmian
- **Powiadomienia w czasie rzeczywistym** via WebSocket
- **Automatyczne powiadomienia e-mail** o zmianach w zadaniach
- **Dwuskładnikową autentykację** (2FA) z Google Authenticator
- **Generowanie raportów PDF** z podsumowaniami zadań
- **Event-driven architecture** z Apache Kafka dla asynchronicznego przetwarzania

---

## Główne funkcjonalności

###  Zarządzanie zadaniami
- Tworzenie i edycja zadań z hierarchią (subtask support)
- Definiowanie terminów i limitów przydzielonych pracowników
- Przechowalnia zadań (IN_POOL, NEW, IN_PROGRESS, PENDING_ACCEPTANCE, COMPLETED)
- Automatyczne zarządzanie statusem na podstawie pojemności

### Zarządzanie zespołem
- Przydzielanie i usuwanie pracowników z zadań
- System uprawnień oparty na rolach
- Zarządzanie kompetencjami i umiejętnościami użytkowników
- Dwuskładnikowa autentykacja (TOTP)

### Powiadomienia
- Real-time powiadomienia WebSocket
- Notyfikacje o zmianach statusu zadań
- Notyfikacje o akceptacji/odrzuceniu przez twórcę

### Raporty
- Generowanie raportów PDF z podsumowaniem zadań i podzadań
- Szablony raportów (Thymeleaf)
- Export danych z zadań

### Bezpieczeństwo
- JWT (JSON Web Tokens) dla bezstanowej autentykacji
- Spring Security z filtrowaniem żądań
- Rate limiting (Bucket4j)
- Historia audytu zmian w zadaniach

### Panel administracyjny
- Zarządzanie priorytetami, statusami zadań
- Konfiguracja systemowa
- Logi audytu

---

## Architektura

### Główne moduły

```
ModularTask
├── auth                 # Autentykacja i autoryzacja
│                       # - JWT Service
│                       # - Spring Security Configuration
│                       # - Login i 2FA endpoints
│
├── user                # Zarządzanie użytkownikami
│                       # - Profile użytkowników
│                       # - Role i uprawnienia
│                       # - Zarządzanie kontami
│
├── tasks               # Zarządzanie zadaniami
│                       # - Task CRUD operations
│                       # - Task assignment logic
│                       # - Status management
│                       # - Report generation
│
├── subtask             # Podzadania
│                       # - Hierarchia zadań
│                       # - SubTask creation i management
│
├── notification        # System powiadomień
│                       # - Real-time WebSocket notifications
│                       # - Notification persistence
│                       # - Event publishing
│
├── email               # Wysyłanie emaili
│                       # - Email service integration
│                       # - HTML templates
│                       # - Asynchronous delivery
│
├── competence          # Zarządzanie kompetencjami
│                       # - Skill tracking
│                       # - User skill mapping
│
├── organization        # Struktura organizacyjna
│                       # - Departments
│                       # - Teams
│
├── tasktemplate        # Szablony zadań
│                       # - Reusable task templates
│                       # - Default parameters
│
├── config              # Konfiguracja aplikacji
│                       # - JWT configuration
│                       # - Security config
│                       # - WebSocket configuration
│                       # - Scheduler configuration
│
├── exceptions          # Custom exceptions
│                       # - Business logic exceptions
│                       # - Global error handling
│
└── admin               # Panel administracyjny
                        # - System configuration
                        # - Audit logs
                        # - Priority/Status management
```

### Przepływ danych

```
User Request
    ↓
[Spring Security Filter] → Authorization via JWT
    ↓
[Controller] → Request mapping
    ↓
[Service] → Business logic & validation
    ↓
[Repository] → JPA/Hibernate
    ↓
[PostgreSQL Database]
    ↓
[Kafka Events] → Async processing
    ↓
[Notification Service] → WebSocket/Email
    ↓
Response
```

---

## Technologie

### Backend Framework
- **Spring Boot** 4.0.4 - Application framework
- **Spring Data JPA** - ORM layer (Hibernate)
- **Spring Security** - Authentication & Authorization
- **Spring WebSocket** - Real-time bidirectional communication

### Database
- **PostgreSQL** - Primary relational database

### Message Broker
- **Apache Kafka** - Event streaming & asynchronous processing

### Security & Authentication
- **JWT (JJWT 0.12.3)** - Token-based authentication
- **Google Authenticator** - TOTP 2FA support
- **Google Zxing** - QR code generation for 2FA setup

### API & Documentation
- **SpringDoc OpenAPI** 2.5.0 - Swagger UI & API documentation

### File Generation
- **OpenHTMLToPDF** - PDF report generation
- **Thymeleaf** - Template engine

### Utilities
- **Bucket4j** - Rate limiting
- **Lombok** - Code generation
- **Dotenv** - Environment configuration

### Build & Containerization
- **Maven** - Build tool (wrapper included)
- **Docker** - Containerization
- **Alpine Linux** - Lightweight base image (Eclipse Temurin 17)

### Development
- **Java 17** - JDK version
- **Spring Boot DevTools** - Hot reload support

---

## Wymagania wstępne

Przed uruchomieniem aplikacji upewnij się, że masz zainstalowane:

- **Java 17** lub wyżej
  ```powershell
  java -version
  ```

- **Docker i Docker Compose** (dla uruchomienia w kontenerach)
  ```powershell
  docker --version
  docker-compose --version
  ```

- **Maven** 3.6+ lub użyj dołączonego wrappera (mvnw.cmd)
  ```powershell
  mvn -version
  ```

- **PostgreSQL** 12+ (jeśli uruchamiasz lokalnie bez Dockera)
  - Baza danych: `modulartask` (lub zmień w `application.properties`)

- **Kafka** 3.0+ (lub uruchom poprzez docker-compose)

---

## Instalacja i uruchomienie

### Opcja 1: Uruchomienie z Docker Compose

Ta opcja automatycznie uruchomi Kafkę i aplikację w kontenerach.

1. **Skonfiguruj zmienne środowiskowe:**
   
   Utwórz plik `.env` w głównym katalogu projektu (jeśli jeszcze nie istnieje):
   ```env
   DATABASE_URL=jdbc:postgresql://postgres:5432/modulartask
   DATABASE_USERNAME=postgres
   DATABASE_PASSWORD=your_password
   SPRING_JPA_HIBERNATE_DDL_AUTO=update
   
   KAFKA_SERVER=kafka:port
   KAFKA_USER=your_kafka_user
   KAFKA_PASSWORD=your_kafka_password
   
   MAIL_HOST=your_mail_host
   MAIL_PORT=your_mail_port
   MAIL_USERNAME=your_mail_username
   MAIL_PASSWORD=your_mail_password
   
   JWT_SECRET_KEY=your_jwt_secret_key
   
   TWOFA_KEY=your_2fa_secret_key
   ```

2. **Zbuduj obraz Docker:**
   ```powershell
   docker build -t modulartask:latest .
   ```

3. **Uruchom stos aplikacji:**
   ```powershell
   docker-compose up -d
   ```

4. **Sprawdź logi (opcjonalnie):**
   ```powershell
   docker-compose logs -f
   ```

5. **Aplikacja będzie dostępna pod adresem:**
   - **API**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger-ui.html

---


## Struktura projektu

```
ModularTask/
├── pom.xml                      # Maven configuration
├── Dockerfile                   # Docker image definition
├── docker-compose.yml           # Docker Compose orchestration
├── README.md                    # This file
├── mvnw / mvnw.cmd             # Maven wrapper scripts
│
├── src/main/java/net/edu/modulartask/
│   ├── ModularTaskApplication.java
│   │
│   ├── auth/                    # Authentication & Authorization
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── TwoFactorService.java
│   │   └── ...
│   │
│   ├── user/                    # User Management
│   │   ├── User.java
│   │   ├── UserService.java
│   │   ├── UserRepository.java
│   │   └── ...
│   │
│   ├── tasks/                   # Task Management
│   │   ├── Task.java
│   │   ├── TaskService.java
│   │   ├── TaskRepository.java
│   │   ├── TaskController.java
│   │   ├── TaskHistory.java
│   │   ├── TaskStatus.java
│   │   ├── TaskResponseDTO.java
│   │   ├── TaskDetailsResponseDTO.java
│   │   └── ...
│   │
│   ├── subtask/                 # Subtask Management
│   │   ├── SubTask.java
│   │   ├── SubTaskDTO.java
│   │   └── ...
│   │
│   ├── notification/            # Notification System
│   │   ├── Notification.java
│   │   ├── NotificationService.java
│   │   ├── NotificationController.java
│   │   └── ...
│   │
│   ├── email/                   # Email Service
│   │   ├── EmailService.java
│   │   ├── EmailTemplate.java
│   │   └── ...
│   │
│   ├── competence/              # Competence & Skills
│   │   ├── Skill.java
│   │   ├── UserSkill.java
│   │   └── ...
│   │
│   ├── organization/            # Organization Structure
│   │   ├── Organization.java
│   │   ├── OrganizationService.java
│   │   └── ...
│   │
│   ├── tasktemplate/            # Task Templates
│   │   ├── TaskTemplate.java
│   │   ├── TaskTemplateService.java
│   │   └── ...
│   │
│   ├── config/                  # Configuration
│   │   ├── JwtService.java
│   │   ├── SecurityConfig.java
│   │   ├── WebSocketConfig.java
│   │   └── WebSocketSchedulerConfig.java
│   │
│   ├── exceptions/              # Custom Exceptions
│   │   ├── TaskNotFoundException.java
│   │   ├── UserNotFoundException.java
│   │   ├── InvalidDeadlineException.java
│   │   └── ...
│   │
│   ├── admin/                      # Admin Panel
│   │   ├── AdminController.java
│   │   ├── AdminService.java
│   │   ├── AuditLog.java
│   │   ├── IssuePriority.java
│   │   └── ...
│   │
│   └── resources/
│       ├── application.properties   # Spring configuration
│       ├── templates/
│       │   └── reportTemplate.html   # PDF report template
│       ├── static/
│       └── fonts/
│           └── Roboto-Regular.ttf    # Custom font for PDFs
│
├── src/test/java/                  # Unit & Integration Tests
│   ├── ModularTaskApplicationTests.java
│   ├── user/
│   ├── organization/
│   ├── security/
│   └── ...
│
└── target/                         # Build output (generated)
    ├── classes/                    # Compiled classes
    ├── generated-sources/
    └── test-classes/
```

---

## API Documentation

### Swagger UI

Po uruchomieniu aplikacji dokumentacja interaktywna API jest dostępna pod adresem:

```
http://localhost:8080/swagger-ui.html
```

### Główne Endpointy

#### Autentykacja
```
POST   /api/auth/login              # Logowanie
POST   /api/auth/2fa-verify         # Weryfikacja 2FA
POST   /api/auth/2fa-setup          # Konfiguracja 2FA
GET    /api/auth/qr-code            # Pobierz kod QR dla 2FA
```

#### Zadania
```
GET    /api/tasks                   # Wszystkie zadania
GET    /api/tasks/{id}              # Szczegóły zadania
POST   /api/tasks                   # Utwórz zadanie
PUT    /api/tasks/{id}              # Edytuj zadanie
DELETE /api/tasks/{id}              # Usuń zadanie
POST   /api/tasks/{id}/assignee     # Przydziel użytkownika
DELETE /api/tasks/{id}/assignee/{userId}  # Usuń przydzielenie
POST   /api/tasks/{id}/take         # Weź zadanie dla siebie
POST   /api/tasks/{id}/start        # Zacznij pracę
POST   /api/tasks/{id}/status       # Zmień status
POST   /api/tasks/{id}/report       # Zgłoś zdanie
POST   /api/tasks/{id}/accept       # Zaakceptuj zadanie
POST   /api/tasks/{id}/reject       # Odrzuć zadanie
GET    /api/tasks/{id}/report       # Pobierz raport PDF
```

#### Powiadomienia
```
GET    /api/notifications           # Wszystkie powiadomienia
GET    /api/notifications/{id}      # Szczegóły powiadomienia
```

#### Użytkownicy
```
GET    /api/users                   # Wszyscy użytkownicy
GET    /api/users/{id}              # Szczegóły użytkownika
POST   /api/users                   # Utwórz użytkownika
PUT    /api/users/{id}              # Edytuj użytkownika
```

Pełna dokumentacja jest dostępna w Swagger UI po uruchomieniu aplikacji.

---

## Bezpieczeństwo

### Mechanizmy bezpieczeństwa

1. **JWT Authentication**
   - Tokeny dostępu z czasem ważności
   - Refresh tokens dla przedłużenia sesji
   - Szyfrowanie kluczem prywatnym wewnętrznym

2. **Spring Security**
   - Filtry autentykacji
   - Autoryzacja na poziomie endpointów
   - CORS configuration

3. **2FA (Dwuskładnikowa Autentykacja)**
   - TOTP (Time-based One-Time Password) z Google Authenticator
   - Kody QR do konfiguracji
   - Backup codes

4. **Rate Limiting**
   - Bucket4j dla ograniczenia żądań API
   - Zapobieganie brute-force attack

5. **Audit Logging**
   - Historia wszystkich zmian w zadaniach
   - Śledzenie kto i kiedy zmienił zadanie

6. **SQL Injection Prevention**
   - JPA Parameterized Queries
   - Input validation

### Best Practices

- **Zmienne środowiskowe**: Przechowuj sensitywne dane (hasła, API klucze) w `.env`
- **HTTPS**: W produkcji zawsze używaj HTTPS
- **Hasła**: Hasła są haszowane za pomocą Spring Security `PasswordEncoder`
- **Session Management**: Tokeny JWT są bezstanowe

---

## Troubleshooting

### Kafka Connection Error
```
Error: Connection to Kafka broker failed
```
**Rozwiązanie**: Upewnij się, że Kafka jest uruchomiona prawidłowo
```powershell
docker-compose up kafka
```

### Database Connection Error
```
Error: Could not connect to PostgreSQL
```
**Rozwiązanie**: Sprawdź konfigurację w `application.properties` i upewnij się, że PostgreSQL jest uruchomiona

### Port Already in Use
```
Error: Address already in use: bind
```
**Rozwiązanie**: Zmień port w `application.properties` lub zakończ proces na porcie 8080

### JWT Token Expired
```
Error: JWT token has expired
```
**Rozwiązanie**: Zaloguj się ponownie, aby uzyskać nowy token

---

## Development Guidelines

### Konwencje kodu
- Java 17+ syntax
- Lombok dla redukcji boilerplate'u
- Spring Data JPA repositories
- DTOs dla API responses
- Services dla business logic

### Testing
```powershell
# Uruchom testy
.\mvnw test

# Uruchom testy z pokryciem
.\mvnw test jacoco:report
```

### Build
```powershell
# Clean build
.\mvnw clean install

# Skip tests
.\mvnw clean install -DskipTests
```

---

## Przydatne linki

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [JWT Introduction](https://jwt.io/)
- [Docker Documentation](https://docs.docker.com/)

---

## Licencja

Projekt jest dostępny do użytku wewnętrznego.

---

## Kontakt i Support

W przypadku pytań lub problemów prosimy skontaktować się z zespołem deweloperskim.

---

**Ostatnia aktualizacja**: 2026-05-27

