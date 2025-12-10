# Guía de Arquitectura e Implementación - CoopCredit

Este documento explica **cómo funciona** la aplicación por dentro, por qué se tomaron ciertas decisiones técnicas y cómo se implementaron los requerimientos clave.

---

## 1. Arquitectura Hexagonal (Puertos y Adaptadores)

El corazón de este proyecto es la **Arquitectura Hexagonal**. Su objetivo principal es **desacoplar la lógica de negocio de la tecnología** (Google, Oracle, Spring, Docker, etc.).

### ¿Cómo se estructura?

Imagina la aplicación como un circulo con capas:

1.  **El Núcleo (Domain)**:
    *   **Ubicación**: `com.coopcredit.domain`
    *   **Qué hay aquí**: Las clases `Affiliate`, `CreditApplication`. Son objetos puros de Java (POJOs).
    *   **Regla de Oro**: ¡Aquí NO hay Spring, ni Hibernate, ni SQL! Solo Java. Si cambias de Framework mañana, esta capa no se toca.

2.  **La Capa de Aplicación (Service)**:
    *   **Ubicación**: `com.coopcredit.application.service`
    *   **Qué hay aquí**: Los "Casos de Uso". La orquestación del proceso.
    *   **Ejemplo**: `CreditApplicationService`. Este servicio dice: *"Primero busca al afiliado, luego valida su salario, luego llama a riesgos, y si todo está bien, guarda"*.

3.  **Los Puertos (Ports)**:
    *   **Ubicación**: `com.coopcredit.domain.port`
    *   **Qué son**: Son "Enchufes" (Interfaces).
    *   **Input Ports**: Lo que la aplicación ofrece hacer (ej: `ProcessCreditApplicationUseCase`).
    *   **Output Ports**: Lo que la aplicación necesita de afuera (ej: `AffiliateRepositoryPort` para guardar datos, `RiskServicePort` para preguntar riesgos).

4.  **La Infraestructura (Adapters)**:
    *   **Ubicación**: `com.coopcredit.infrastructure`
    *   **Qué hay aquí**: Los "Cables" que se conectan a los enchufes.
    *   **Web Adapter**: `CreditApplicationController` (Convierte JSON web a objetos de Dominio).
    *   **Persistence Adapter**: `CreditApplicationRepositoryAdapter` (Convierte objetos de Dominio a Entidades JPA y guarda en BD).

---

## 2. Flujo de una Solicitud (Paso a Paso)

Veamos qué pasa cuando alguien crea un crédito (`POST /api/solicitudes`):

1.  **El Usuario** envía un JSON desde el Frontend.
2.  **Infraestructura (Controller)**: `CreditApplicationController` recibe el JSON.
    *   No hace lógica. Solo llama al Puerto de Entrada: `processCreditApplicationUseCase.registerApplication()`.
3.  **Aplicación (Service)**: `CreditApplicationService` toma el control.
    *   *Paso 1*: Llama al Puerto de Salida `affiliateRepository.findByDocument()` para ver si el afiliado existe. (El servicio no sabe si es Oracle o Postgres, solo sabe que alguien le devolverá un afiliado).
    *   *Paso 2*: Aplica **Reglas de Negocio** (¿Es antigüedad > 6 meses? ¿Le alcanza el sueldo?).
    *   *Paso 3*: Llama al Puerto de Salida `riskService.evaluateRisk()`.
    *   *Paso 4*: Decide si APRUEBA o RECHAZA.
    *   *Paso 5*: Llama al Puerto de Salida `creditRepository.save()`.
4.  **Infraestructura (DB Adapter)**: `CreditApplicationRepositoryAdapter` recibe la orden de guardar.
    *   Convierte el objeto `CreditApplication` (Dominio) a `CreditApplicationEntity` (JPA).
    *   Usa Spring Data para hacer el `INSERT` en PostgreSQL.

---

## 3. Implementación de Requerimientos Clave

### A. Seguridad (JWT)
Hemos implementado seguridad **Stateless** (Sin sesiones en servidor).
*   **Login**: El usuario envía credenciales. Si son válidas, `JwtService` genera un "Token" firmado criptográficamente que contiene su usuario y rol.
*   **Validación**: Tenemos un `JwtAuthenticationFilter`. Este filtro intercepta **cada petición**. Revisa si el encabezado `Authorization` trae un token válido. Si es así, deja pasar la petición al Controller.

### B. Persistencia y Problema N+1
*   **Problema**: Originalmente, al pedir "Dames los créditos", Hibernate traía la lista de créditos (1 consulta) y luego, por cada crédito, hacía otra consulta para traer el nombre del afiliado asociado. Si hay 100 créditos, hacía 101 consultas.
*   **Solución**: Usamos `@EntityGraph(attributePaths = "affiliate")` en el Repositorio. Esto le dice a JPA: *"Cuando traigas créditos, haz un JOIN FETCH con afiliados inmediatamente"*. Resultado: **1 sola consulta**.

### C. Reglas de Negocio
Las implementamos en la capa de **Aplicación/Dominio**, no en el Controlador ni en la Base de Datos.
*   **Antigüedad**: `LocalDate.now().minusMonths(6)`.
*   **Capacidad**: `cuota.compareTo(salario * 0.5)`.
Esto asegura que las reglas sean fáciles de probar con Tests Unitarios sin necesidad de levantar la base de datos.

### D. Frontend
Creamos una "Single Page Application" (SPA) simple.
*   No usa React ni Angular para no complicar el despliegue.
*   Usa `fetch` nativo de JavaScript para hablar con la API (que acabamos de explicar).
*   Está servida por el mismo Spring Boot (`src/main/resources/static`), simplificando el despliegue a un solo archivo `.jar`.

---
*Este documento fue generado para explicar la lógica interna de CoopCredit.*
