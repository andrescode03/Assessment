# Bitácora de Desarrollo - CoopCredit

Este documento registra cronológicamente todas las actividades, decisiones técnicas y componentes implementados durante la construcción del proyecto **CoopCredit**. Sirve como referencia para entender la evolución y el estado actual del sistema.

---

## 📅 Resumen de Actividades

### Finitud del Proyecto
- **Estado Actual**: Funcional (Happy Path Completo).
- **Cobertura**: ~75-80% de los requisitos originales.
- **Backend**: Java / Spring Boot.
- **Frontend**: HTML/JS (Single Page Application).
- **Infraestructura**: Docker (PostgreSQL).

---

## 🛠️ Fase 1: Inicialización y Arquitectura
**Objetivo**: Establecer los cimientos del proyecto basándonos en Arquitectura Hexagonal.

1.  **Estructura del Proyecto**:
    - Se creó un proyecto Maven Spring Boot 3.4.0 (Java 21).
    - Se definieron paquetes hexagonales:
        - `domain`: Lógica pura, sin frameworks.
        - `application`: Casos de uso (`Service`).
        - `infrastructure`: Adaptadores (Web, DB, Security).
2.  **Configuración Base**:
    - `pom.xml`: Dependencias para Web, JPA, Flyway, Security (JWT), Actuator, Testcontainers.
    - `docker-compose.yml`: Base de datos PostgreSQL.
    - `application.properties`: Configuración de conexión y puertos.

---

## 📦 Fase 2: Dominio y Lógica de Negocio
**Objetivo**: Modelar el problema sin dependencias tecnológicas.

1.  **Modelos (`Domain`)**:
    - `Affiliate`: Datos del afiliado (salario, fecha ingreso, estado).
    - `CreditApplication`: Solicitud con montos, plazos y estado.
    - `RiskEvaluation`: Resultado de la evaluación de riesgo externa.
2.  **Puertos (Interfaces)**:
    - *Input*: `ManageAffiliateUseCase`, `ProcessCreditApplicationUseCase`.
    - *Output*: `AffiliateRepositoryPort`, `RiskServicePort`, `CreditApplicationRepositoryPort`.

---

## 🔌 Fase 3: Adaptadores de Infraestructura
**Objetivo**: Conectar el dominio con el mundo real (Base de datos y API).

1.  **Persistencia (JPA + Flyway)**:
    - **Entidades**: `AffiliateEntity`, `CreditApplicationEntity`, `UserEntity`.
    - **Migración V1**: Script SQL para crear tablas `users`, `affiliates`, `credit_applications`.
    - **Adaptadores**: Clases que traducen entre Entidades JPA y Modelos de Dominio.
2.  **Mock de Riesgo**:
    - Se creó `RiskMockController` simulado dentro de la misma app para evitar dependencias externas complejas.
    - Se implementó `RiskServiceAdapter` (`RestTemplate`) para consumir este servicio.

---

## 🔒 Fase 4: Seguridad (JWT)
**Objetivo**: Proteger la aplicación con autenticación moderna y "stateless".

1.  **Componentes**:
    - `JwtService`: Generación y validación de tokens.
    - `JwtAuthenticationFilter`: Intercepta cada petición HTTP para validar el token Bearer.
    - `AuthController`: Endpoints para `register` (crear usuario) y `login` (obtener token).
    - `SecurityConfig`: Configuración de Spring Security para permitir acceso público solo a Auth y Mock.

---

## 🧪 Fase 5: Verificación y Testing
**Objetivo**: Asegurar que las piezas funcionen juntas.

1.  **Pruebas de Integración**:
    - Se creó `HappyPathIntegrationTest` usando **Testcontainers**.
    - **Reto Solucionado**: Conflictos de versiones en `pom.xml` y configuración de Docker resolvieron en múltiples iteraciones.
2.  **Diagnóstico de Problemas**:
    - Detectamos conflicto de puertos (`5432` y `8080`) ocupados por servicios locales.
    - **Solución Final**: Movimos la BD al puerto `5433` y la App al `8082`.

---

## 🖥️ Fase 6: Frontend (Extra)
**Objetivo**: Proveer una interfaz visual para probar el sistema fácilmente.

1.  **Implementación SPA**:
    - Sin frameworks pesados (Vanilla JS + HTML5 + CSS3).
    - **Ubicación**: `src/main/resources/static`.
    - **Funcionalidad**:
        - Login/Registro automático.
        - Gestión de Afiliados.
        - Solicitud de Créditos con feedback visual (Aprobado/Rechazado).
    - **Diseño**: Estilo moderno "Glassmorphism".
2.  **Ajustes de Conexión**: Configuración dinámica de la URL de la API para evitar errores de CORS/Red.

---

## ⚙️ Fase 7: Reglas Avanzadas y Optimización (Final)
**Objetivo**: Completar los requisitos funcionales complejos y mejorar el rendimiento.

1.  **Reglas de Negocio Implementadas**:
    - **Antigüedad de Afiliado**: Se valida que tenga > 6 meses de registro.
    - **Capacidad de Pago**: Se rechaza la solicitud si la cuota mensual supera el 50% del salario.
2.  **Endpoints Adicionales**:
    - `PUT /api/afiliados/{doc}`: Actualización de datos.
    - `GET /api/solicitudes`: Listado completo de historial.
3.  **Frontend**:
    - Nueva funcionalidad para visualizar la lista de solicitudes en el Dashboard.
4.  **Optimización Técnica**:
    - Se resolvió el problema **N+1** en `SpringDataCreditApplicationRepository` utilizando `@EntityGraph` para traer los afiliados junto con los créditos en una sola consulta.

---

## 📋 Estado Final de Componentes

| Componente | Estado | Notas |
| :--- | :--- | :--- |
| **Backend API** | ✅ Funcional | Puerto 8082. Endpoints CRUD completos. |
| **Database** | ✅ Conectada | PostgreSQL en Puerto 5433. |
| **Seguridad** | ✅ Activa | JWT Token requerido para operaciones. |
| **Frontend** | ✅ Desplegado | Accesible en `http://localhost:8082`. Lista historial. |
| **Test Auto** | ✅ Configurado | Requiere Docker environment limpio. |
| **Reglas Negocio** | ✅ Completo | Cobertura total de requisitos (Salario, Antigüedad). |
| **Rendimiento** | ✅ Optimizado | Sin problemas N+1 detectados. |

## 🚀 Cómo Retomar el Proyecto

Para continuar trabajando o consultar el proyecto en el futuro:

1.  **Levantar Infraestructura**:
    ```bash
    sudo docker compose up -d
    ```
    *(Verifica que no haya conflictos en el puerto 5433).*

2.  **Ejecutar Aplicación**:
    ```bash
    ./mvnw spring-boot:run
    ```

3.  **Acceder al Sistema**:
    - Web: `http://localhost:8082`
    - Credenciales demo: `admin` / `password`

---

./mvnw spring-boot:run

sudo docker compose down && sudo docker compose up -d