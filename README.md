# FarmDirect — Farm-to-Table Platform

A full-stack Java web application connecting Tamil Nadu farmers directly with customers, eliminating middlemen.

## Tech Stack

| Layer       | Technology                       |
|-------------|----------------------------------|
| Backend     | Java 17, Spring Boot 3.2.5       |
| Frontend    | Vaadin Flow 24.3.10 (Java UI)    |
| Database    | H2 (file mode — data persists)   |
| Security    | Spring Security + BCrypt         |
| Build       | Maven                            |

## Quick Start

```bash
# 1. Clone / extract the project
cd farmdirect

# 2. Build and run
./mvnw spring-boot:run
# or: mvn spring-boot:run

# 3. Open browser
http://localhost:8080
```

The database seeds automatically on first run with Tamil Nadu dummy data. Local development uses a file-based H2 database; cloud deployments should use PostgreSQL.

## Deploy to Render

This is a Maven/Spring Boot application. Render should build it from the `Dockerfile` in the repository root.

1. Create a PostgreSQL database in Render.
2. Create a new **Web Service** from this GitHub repository and select the `main` branch.
3. Choose **Docker** as the runtime. Render detects the root `Dockerfile`; leave the root directory as `.`.
4. In the web service's **Environment** settings, add:

   | Key | Value |
   |-----|-------|
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<internal-host>:5432/<database-name>` |
   | `SPRING_DATASOURCE_USERNAME` | The database's internal username |
   | `SPRING_DATASOURCE_PASSWORD` | The database's password |
   | `SPRING_H2_CONSOLE_ENABLED` | `false` |

   Use the PostgreSQL host, database name, and credentials shown by Render. Keep the password in Render's environment settings; do not put it in Git. Render supplies `PORT` automatically, and Spring Boot uses it. No separate build or start command is needed for a Docker web service.

5. Deploy the service and open its `onrender.com` URL. The first startup seeds the new database with demo data.

The application still defaults to local H2 when `SPRING_DATASOURCE_URL` is not set. Hibernate detects the database dialect from the configured JDBC URL and driver.

## Login Credentials

All accounts use password: **pass123**

| Username  | Role     | Description                    |
|-----------|----------|--------------------------------|
| admin     | Admin    | Platform administrator         |
| murugan   | Farmer   | Organic vegetables, Pollachi   |
| lakshmi   | Farmer   | Fruits, Kodaikanal             |
| senthil   | Farmer   | Grains & pulses, Thanjavur     |
| kavitha   | Farmer   | Spices, Theni                  |
| ravi      | Farmer   | Dairy products, Erode          |
| priya     | Customer | Coimbatore                     |
| arjun     | Customer | Madurai                        |
| deepa     | Customer | Salem                          |

## Features by Role

### Customer
- Browse products with search & category filters
- Freshness badges (FRESH / USE SOON / EXPIRED)
- Organic product indicators
- Add to favourites
- Cart grouped by farmer
- Mock payment (Razorpay/GPay style with MOCKPAY-XXXXXXXX IDs)
- Amazon-style order tracking timeline
- Nearby farmers map (Haversine distance, OpenStreetMap)
- Rate & review delivered orders

### Farmer
- Dashboard with stats (products, orders, earnings)
- Full product CRUD (with Tamil names, harvest/expiry dates)
- Order management with status workflow
- Auto-assigns delivery agent on dispatch
- Profile management

### Admin
- Platform-wide dashboard (users, products, orders, agents)
- User management (activate/deactivate)
- View all orders and products
- Delivery agent CRUD

## Order Status Flow

```
PLACED → PAID → CONFIRMED → PACKED → OUT_FOR_DELIVERY → DELIVERED
                                    → READY_FOR_PICKUP → DELIVERED
Any stage → CANCELLED
```

## Database

H2 file mode — data persists across restarts at `./data/farmdirect`.

H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/farmdirect`
- Username: `sa` / Password: (empty)

## Project Structure

```
farmdirect/
├── pom.xml
├── frontend/themes/farmdirect/styles.css     # Farm theme + animations
├── src/main/resources/application.properties
└── src/main/java/com/farmdirect/
    ├── FarmDirectApplication.java
    ├── config/DataSeeder.java                # Dummy data seeder
    ├── entity/                               # 11 JPA entities + 3 enums
    ├── repository/                           # 9 Spring Data repositories
    ├── service/                              # 8 service classes
    ├── security/                             # Spring Security config
    └── views/                                # 15 Vaadin views
        ├── LoginView, RegisterView, MainLayout
        ├── customer/   (Browse, Cart, Orders, Favourites, NearbyFarmers)
        ├── farmer/     (Dashboard, Products, Orders, Profile)
        └── admin/      (Dashboard, Users, Orders, Products, Delivery)
```

## Key Technical Details

- **Haversine formula** calculates real distances to nearby farmers (≤50 km)
- **Session-scoped cart** via `@VaadinSessionScope`
- **Mock payment** always succeeds, generates `MOCKPAY-XXXXXXXX` transaction IDs
- **Freshness calculation** based on days until expiry (transient on entity)
- **CSS animations** recreate React Bits style effects (staggered fadeSlideUp, pulse badges)
- **Role-based routing** — each role sees only their views
