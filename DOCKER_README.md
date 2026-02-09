# Product Management Backend - Docker Setup

## Quick Start

### 1. Start Database Services
```bash
docker-compose up -d
```

### 2. Verify Services
```bash
docker-compose ps
docker-compose logs
```

### 3. Run Application
```bash
cd backend-product
./gradlew bootRun
```

### 4. Access Application
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console (test): http://localhost:8080/h2-console

## Services

### PostgreSQL
- **Port**: 5432
- **Database**: product_db
- **User**: postgres
- **Password**: postgres

### Redis
- **Port**: 6379

## Useful Commands

### Stop Services
```bash
docker-compose down
```

### Stop and Remove Volumes
```bash
docker-compose down -v
```

### View Logs
```bash
docker-compose logs -f postgres
docker-compose logs -f redis
```

### Connect to PostgreSQL
```bash
docker exec -it product-postgres psql -U postgres -d product_db
```

### Connect to Redis
```bash
docker exec -it product-redis redis-cli
```

## Troubleshooting

### Port Already in Use
```bash
# Check what's using the port
lsof -i :5432
lsof -i :6379

# Kill the process or change port in docker-compose.yml
```

### Database Connection Failed
```bash
# Check if PostgreSQL is running
docker-compose ps

# Check logs
docker-compose logs postgres

# Restart services
docker-compose restart
```
