# Oil Production Service (osm-prod)

Core microservice for managing oil deliveries, unified production workflows, and quality control results.

## 📖 Functional Overview
This service is the heart of the oil production lifecycle. It handles the transition from raw material reception to a quality-verified product ready for sale or storage.

### Key Features
- **Unified Delivery Management**: Tracks the arrival of oil from various suppliers, including quantities and initial status.
- **Quality Control (QC)**: Manages QC rules and stores test results (e.g., acidity, peroxide levels) against specific deliveries.
- **Oil Sale Workflow**: Orchestrates the sale of bulk oil, including pricing and genealogy tracking.
- **Genealogy Tracking**: Maintains a history of where each batch of oil originated.


## 🛠 Tech Stack
- **Language:** Java 21 (Eclipse Temurin)
- **Framework:** Spring Boot 3.4.4
- **Database:** PostgreSQL (`osmproduction`)
- **Discovery:** Netflix Eureka

## 🚀 Getting Started
### Local Development
```bash
./mvnw spring-boot:run
```

### Docker Build
This service uses multi-stage builds and requires access to private Maven packages (osm-parent). Use BuildKit secrets to pass your settings:
```bash
docker build --secret id=maven_settings,src=$HOME/.m2/settings.xml -t oilproductionservice .
```

## ⚙️ Configuration (Environment Variables)
| Variable | Default | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8083` | Service port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/osmproduction` | Database URL |
| `LOG_LEVEL_EUREKA` | `WARN` | Level for Discovery Client logs |
| `LOG_LEVEL_REST` | `WARN` | Level for RestTemplate logs |
| `LOG_LEVEL_WEB` | `INFO` | Level for Spring Web logs |

## 🔗 CI/CD
Automated builds and deployments are configured via GitHub Actions:
- **Registry:** `ghcr.io/x-dev-grp/oilproductionservice`
- **Workflow:** `.github/workflows/release-deploy.yml`
