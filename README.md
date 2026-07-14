# Sistema de Clínica Interna

Este proyecto es una plataforma web para la gestión integral de una clínica interna. Está compuesto por un backend robusto en Spring Boot y una interfaz moderna de usuario construida en Angular.

---

## 🛠️ Tecnologías y Requisitos

### Backend (API)
*   **Java 17** (JDK)
*   **Spring Boot 3.3.6** (Spring Web, JPA, Security, Actuator, Mail)
*   **PostgreSQL**
*   **Maven** para la gestión de dependencias y empaquetado.

### Frontend
*   **Angular 20**
*   **Vite** (servidor de desarrollo integrado de Angular)
*   **Bootstrap 5** & **Popper.js** para maquetación y estilos.

### Infraestructura
*   **Docker & Docker Compose** para contenedores y servicios en producción.
*   **Nginx** como proxy inverso y servidor de estáticos.

---

## 💻 Desarrollo Local

Para ejecutar el proyecto localmente y mantener el flujo de trabajo en la terminal, sigue estos pasos:

### 1. Preparar las Variables de Entorno (`.env`)
Copia el archivo de ejemplo para crear tu entorno local:
```bash
cp .env.example .env
```
Asegúrate de configurar las variables necesarias como credenciales de base de datos, tokens de API y claves SMTP en el archivo `.env`.

### 2. Levantar la Base de Datos (Docker)
Inicia la base de datos PostgreSQL en segundo plano:
```bash
docker compose up postgres -d
```

### 3. Iniciar el Backend (Spring Boot)
Ejecuta la API de Spring Boot en tu terminal:
```bash
./mvnw spring-boot:run
```
El backend estará disponible en `http://localhost:8080`.

### 4. Iniciar el Frontend (Angular)
Entra en la carpeta del frontend, instala las dependencias y arranca el servidor de desarrollo:
```bash
cd frontend
npm install
npm start
```
El frontend estará disponible en `http://localhost:4200` y proxyará automáticamente cualquier petición a `/api` hacia el backend en el puerto `8080` de manera transparente.

---

## 🐳 Ejecución de la Pila Completa en Contenedores (Docker Compose)

Si deseas probar la compilación y ejecución de todos los servicios (Base de Datos, Backend y Frontend) simulando el entorno de producción, puedes levantar la pila completa de Docker:

1. Crea o verifica tu archivo `.env`.
2. Ejecuta el comando de construcción y arranque:
```bash
docker compose --env-file .env up -d --build
```
*   **Frontend (Nginx + Angular):** Mapeado en `http://localhost:4175`
*   **Backend (Spring Boot):** Mapeado en `http://localhost:8081` (interno `8080`)
*   **Base de datos (PostgreSQL):** Mapeado en el puerto `5432`

---

## 🚀 Despliegue en Producción

Para el despliegue en producción mediante la máquina virtual Debian y Nginx, por favor consulta la **[Guía de Despliegue](docs/Deploy.md)** o el documento de referencia local generado.
