# Pedidos360

Proyecto académico — **DSY1107 Desarrollo Cloud Native I**
Entrega **EP1 — Encargo**

**Integrante:** Gino Alfonso Trujillo Mariangeli (trabajo individual)

---

## Descripción

Pedidos360 es una aplicación de gestión de pedidos y catálogo de productos, con autenticación y autorización basada en roles mediante Microsoft Entra ID. El sistema implementa un flujo de negocio completo e integrado:

- **Gestión de Pedidos**: creación, consulta y seguimiento por estados (`CREADO → ACEPTADO → EN_PREPARACION → DESPACHADO → ENTREGADO`, con posibilidad de `CANCELADO`).
- **Catálogo de Productos y Stock**: administración de productos, precios y stock, con descuento automático de stock al aceptar un pedido.

Reglas de negocio implementadas:
- Un pedido no puede pasar a `DESPACHADO` si no fue previamente `ACEPTADO`.
- Al aceptar un pedido, el stock de los productos involucrados disminuye automáticamente.
- Si un pedido aceptado se cancela, el stock se restaura.

## Arquitectura

```
Usuario
  │
  ▼
Angular + MSAL (login, guards, interceptor)
  │  Authorization Code + PKCE
  ▼
Microsoft Entra ID  →  emite Access Token (JWT)
  │
  ▼
AWS API Gateway (HTTP API)
  │  JWT Authorizer (valida issuer + audience) · CORS
  ▼
Spring Boot + Spring Security  (EC2, puerto 8080)
  │  valida JWT · autorización por rol (@PreAuthorize)
  ▼
PostgreSQL en AWS RDS
```

**Flujo end-to-end:** Angular → Microsoft Entra ID → Access Token → API Gateway → Spring Boot (EC2) → Base de datos cloud (RDS).

## Tecnologías

| Capa | Tecnología |
|---|---|
| Frontend | Angular 22, `@azure/msal-angular`, `@azure/msal-browser` |
| Autenticación | Microsoft Entra ID, OAuth 2.0 / OpenID Connect (Authorization Code + PKCE) |
| Puerta de entrada | AWS API Gateway (HTTP API), JWT Authorizer, CORS |
| Backend | Java 21, Spring Boot 4.1.1, Spring Security (Resource Server, JWT) |
| Persistencia | Spring Data JPA / Hibernate, PostgreSQL (AWS RDS) |
| Infraestructura | AWS EC2 (Amazon Linux 2023), AWS RDS, AWS API Gateway |

## Estructura del repositorio

```
pedidos360/
├── src/main/java/com/pedidos360/
│   ├── config/          # Seguridad (SecurityConfig: JWT, roles, CORS)
│   ├── catalog/          # Módulo Catálogo (entity, repository, service, controller, dto)
│   ├── orders/            # Módulo Pedidos (entity, repository, service, controller, dto)
│   └── common/exception/  # Manejo global de errores
├── src/main/resources/
│   ├── application.yml       # Config base (perfil dev por defecto, usa H2)
│   └── application-prod.yml  # Config para producción (RDS, vía variables de entorno)
├── pom.xml
└── pedidos360-web/            # Frontend Angular
    └── src/app/
        ├── auth-config.ts         # Configuración de MSAL (tenant, client id, scopes)
        ├── app.config.ts          # Providers de MSAL, interceptor, guard
        ├── guards/auth.guards.ts
        ├── services/               # AuthHelperService, ApiService
        └── pages/                  # login, dashboard, orders, catalog
```

## Roles y permisos

| Acción | Admin | Operador | Cliente |
|---|:---:|:---:|:---:|
| Ver catálogo | ✅ | ✅ | ✅ |
| Crear/editar producto | ✅ | ❌ | ❌ |
| Crear pedido | ✅ | ✅ | ✅ |
| Ver pedidos propios | ✅ | ✅ | ✅ |
| Ver todos los pedidos | ✅ | ✅ | ❌ |
| Cambiar estado de un pedido | ✅ | ✅ | ❌ |

La autorización se aplica en el backend con `@PreAuthorize` sobre cada endpoint, a partir del claim `roles` del Access Token emitido por Entra ID.

## Endpoints principales

**Catálogo**
```
GET    /api/catalog/products
POST   /api/catalog/products      (solo Admin)
PUT    /api/catalog/products/{id} (solo Admin)
```

**Pedidos**
```
POST   /api/orders
GET    /api/orders
GET    /api/orders/{id}
PUT    /api/orders/{id}/status    (solo Admin/Operador)
```

Todos los endpoints requieren un Access Token válido (`Authorization: Bearer <token>`), emitido por el tenant de Entra ID configurado, con audiencia igual al Client ID de la API.

## Cómo ejecutar en local

### Backend

Requisitos: Java 21, Maven (incluido como `mvnw`).

```bash
cd pedidos360
./mvnw clean spring-boot:run
```

Por defecto corre con el perfil `dev`, que usa una base de datos H2 en memoria (no requiere instalar PostgreSQL). Queda disponible en `http://localhost:8080`.

Para correr contra una base PostgreSQL real (perfil `prod`), definir las variables de entorno antes de arrancar:

```bash
export DB_HOST=<host-de-postgresql>
export DB_USER=<usuario>
export DB_PASSWORD=<contraseña>
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Frontend

Requisitos: Node.js 24+, Angular CLI.

```bash
cd pedidos360-web
npm install
ng serve
```

Disponible en `http://localhost:4200`. Requiere iniciar sesión con un usuario registrado en el tenant de Microsoft Entra ID del proyecto.

## Despliegue en AWS

El sistema está desplegado y operativo en AWS Academy Learner Lab:

- **Backend**: instancia EC2 (Amazon Linux 2023), ejecutando el `.jar` de Spring Boot en el puerto 8080.
- **Base de datos**: instancia RDS PostgreSQL, accesible solo desde la EC2 (Security Group restringido).
- **Puerta de entrada**: AWS API Gateway (HTTP API), con JWT Authorizer validando los tokens de Entra ID y reglas de CORS.

> **Nota:** por tratarse de un entorno de AWS Academy Learner Lab, los recursos (EC2 y RDS) pueden estar detenidos fuera de las sesiones de laboratorio. La URL pública de la API queda documentada como evidencia en el video de la EP2.

## Seguridad

- Las credenciales de base de datos y configuración sensible se gestionan mediante variables de entorno (`application-prod.yml`), nunca escritas en el código fuente.
- Los tokens JWT son validados tanto en el API Gateway (JWT Authorizer) como en el backend (Spring Security Resource Server), verificando `issuer` y `audience`.
- El control de acceso por rol se aplica en cada endpoint del backend, no solo en el frontend.
