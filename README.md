# ChocoAventura-back
Este repositorio contiene el backend del proyecto ChocoAventura

# Setup
1. Levantar base de datos:
docker compose up -d

2. Ejecutar proyecto:
.\mvnw spring-boot:run

3. Ver tablas en pgAdmin:
http://localhost:5050
email: admin@chocoaventura.com
password: admin123
Servers
  → tu servidor
    → Databases
      → chocoaventura_db
        → Schemas
          → public
            → Tables