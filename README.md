# KMS Corporate Management System

Este proyecto es una solución integral para la gestión de activaciones KMS (Key Management Service) en entornos corporativos. Combina un emulador de KMS (`vlmcsd`) con una potente interfaz de administración construida en **Spring Boot** y **Kotlin**.

## Características

- **Puerto Unificado**: Todo el sistema funciona bajo un solo puerto (por defecto 8080). El sistema detecta automáticamente si el tráfico es Web (HTTP) o de Activación (KMS).
- **Dashboard Premium**: Interfaz moderna con *Glassmorphism* para monitorear activaciones y estadísticas.
- **Seguridad Robusta**: Sistema de login para administradores con BCrypt.
- **Base de Datos Flexible**: Usa Spring Data JPA con PostgreSQL, incluyendo migraciones automáticas con Flyway.
- **Dockerizado**: Un solo contenedor que compila y ejecuta todo el stack.

## Requisitos Previos

- Docker y Docker Compose (opcional para despliegue).
- Java 21 (si se corre localmente).
- PostgreSQL.

## Configuración

Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:

```env
# Configuración del Servidor
PORT=8080
WEB_PORT=8081

# Base de Datos
DB_HOST=tu-host.com
DB_PORT=5432
DB_NAME=nombre_db
DB_USER=usuario_db
DB_PASSWORD=tu_password

# Administrador Inicial
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
```

## Despliegue con Docker

Para construir y levantar el sistema completo en un solo contenedor:

```bash
docker build -t kms-corporate .
docker run -p 8080:8080 --env-file .env kms-corporate
```

## Estructura del Proyecto

- `src/main/kotlin`: Lógica de la aplicación Spring Boot.
- `src/main/resources`: Plantillas HTML (Thymeleaf), CSS y archivos de migración (SQL).
- `docker-vlmcsd`: Código fuente y Dockerfile del emulador KMS.
- `TcpMultiplexerService.kt`: El "cerebro" que permite que la Web y el KMS compartan el mismo puerto.

## API de Registro

Puedes registrar activaciones manualmente o desde scripts externos:

```bash
curl -X POST "http://localhost:8080/api/v1/register-activation?machineName=PC-CONTABILIDAD&ipAddress=10.0.0.5&softwareName=Office+2021"
```

## Licencia

Este proyecto es para uso corporativo interno.
