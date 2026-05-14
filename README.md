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

## Setup & Despliegue

Sigue estos pasos para un despliegue exitoso en entornos de nube (como Oracle Cloud) usando Dokploy.

### 1. Configuración de Red (Oracle Cloud / VPS)
El sistema KMS utiliza el puerto **1688 (TCP)**. Debes abrirlo en tu firewall:
- **Consola de Oracle Cloud**: Añade una "Ingress Rule" para el puerto `1688` (TCP) desde `0.0.0.0/0`.
- **Servidor (Firewall interno)**: Ejecuta los siguientes comandos para permitir el tráfico:
  ```bash
  sudo iptables -I INPUT -p tcp --dport 1688 -j ACCEPT
  sudo netfilter-persistent save
  ```

### 2. Configuración en Dokploy
1.  **Variables de Entorno**: Configura todas las variables del `.env` en la pestaña "Environment" de tu aplicación en Dokploy.
2.  **Mapeo de Puertos**: En la sección de "Ports", añade el siguiente mapeo:
    - **Published Port**: `1688`
    - **Target Port**: `8080`
    - **Protocol**: `TCP`
3.  **Despliegue**: El sistema generará automáticamente un archivo `.env` interno basado en tus variables de Dokploy.

### 3. DNS y Cloudflare (IMPORTANTE)
Si usas Cloudflare para gestionar tu dominio (`kms.joss.red`):
- **MODO DNS ONLY**: El registro de tipo A para tu subdominio **DEBE** estar en modo "Nube Gris" (DNS Only).
- **¿Por qué?**: El proxy de Cloudflare (Nube Naranja) solo soporta tráfico HTTP/HTTPS y bloqueará la conexión KMS de Windows.

---

## Guía de Activación

Una vez que el servidor esté corriendo, usa estos comandos en una terminal como **Administrador**:

### Windows 10/11 Pro
```powershell
slmgr.vbs /upk
slmgr.vbs /ipk W269N-WFGWX-YVC9B-4J6C9-T83GX
slmgr.vbs /skms kms.joss.red
slmgr.vbs /ato
```

### Office (LTSC / Volume)
*Navega a la carpeta de instalación de Office (ej. `C:\Program Files\Microsoft Office\Office16`)*
```powershell
cscript ospp.vbs /sethst:kms.joss.red
cscript ospp.vbs /act
```

## Estructura del Proyecto

- `src/main/kotlin`: Lógica de la aplicación Spring Boot.
- `src/main/resources`: Plantillas HTML (Thymeleaf), CSS y archivos de migración (SQL).
- `entrypoint.sh`: Script de arranque que genera el `.env` dinámicamente en producción.
- `TcpMultiplexerService.kt`: El "cerebro" que permite que la Web y el KMS compartan el mismo puerto.

## Licencia

Este proyecto es para uso corporativo interno.
