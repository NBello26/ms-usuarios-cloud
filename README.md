# Pedidos360 - Microservicio de Usuarios (`ms-usuarios`)

Microservicio responsable de la gestión de usuarios de la plataforma **Pedidos360**. Permite registrar, consultar, actualizar y eliminar usuarios, además de proporcionar un mecanismo de inicio de sesión mediante **SSO de Microsoft/Azure AD**.

El microservicio utiliza **DTOs** para controlar la información expuesta hacia el exterior, evitando exponer directamente la entidad de persistencia y permitiendo separar los datos utilizados para creación, actualización, autenticación y respuesta.

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.2.x
* **Persistencia:** Spring Data JPA / PostgreSQL
* **Validación:** Jakarta Bean Validation
* **Seguridad:** Spring Security / OAuth2 Resource Server / JWT
* **Autenticación:** Microsoft Azure AD / SSO
* **Documentación:** OpenAPI / Swagger
* **Librerías:** Lombok
* **Contenedorización:** Docker

## 🏗️ Arquitectura

El microservicio utiliza una arquitectura basada en capas:

```text
ms-usuarios
│
├── controller
│   ├── UsuarioController
│   └── AdminInternalController
│
├── service
│   └── UsuarioService
│
├── repository
│   └── UsuarioRepository
│
├── model
│   └── Usuario
│
├── dto
│   ├── UsuarioDTO
│   ├── UsuarioRequestDTO
│   ├── UsuarioUpdateDTO
│   └── SsoLoginRequestDTO
│
└── security
    └── SecurityConfig
```

La separación por capas permite mantener desacopladas las responsabilidades de control HTTP, lógica de negocio, persistencia, representación de datos y seguridad.

## 🗄️ Persistencia

El microservicio utiliza PostgreSQL mediante **Spring Data JPA**.

### Base de Datos

```text
db_usuarios
└── usuarios
```

### Tabla `usuarios`

| Campo          | Tipo        | Restricciones / Descripción                   |
| :------------- | :---------- | :-------------------------------------------- |
| `id`           | BIGINT      | Identificador único, generado automáticamente |
| `nombre`       | VARCHAR(50) | Nombre del usuario                            |
| `edad`         | INTEGER     | Edad entre 18 y 100 años                      |
| `genero`       | VARCHAR     | Género del usuario                            |
| `correo`       | VARCHAR     | Correo único del usuario                      |
| `tipo_usuario` | VARCHAR     | Rol del usuario: `admin` o `cliente`          |
| `telefono`     | VARCHAR     | Teléfono de contacto                          |
| `foto_url`     | VARCHAR     | URL de imagen de perfil                       |
| `ocupacion`    | VARCHAR     | Ocupación del usuario                         |
| `direccion`    | VARCHAR     | Dirección del usuario                         |

La entidad `Usuario` utiliza JPA para mapear la tabla `usuarios` y Jakarta Validation para aplicar reglas de validación sobre los datos.

## 🔐 Seguridad

El microservicio está preparado para trabajar como **OAuth2 Resource Server**, utilizando **JWT emitidos por Microsoft Azure AD**.

La aplicación utiliza un enfoque **stateless**, por lo que no mantiene sesiones de usuario en el servidor.

```text
Cliente
   │
   │ Bearer Token (JWT)
   ▼
ms-usuarios
   │
   ├── Spring Security
   │       │
   │       └── Validación JWT
   │
   ▼
Azure AD / Microsoft
   │
   └── Validación de issuer, tenant y claves públicas
```

### Configuración de seguridad

La configuración de seguridad contempla:

* Validación de tokens JWT.
* Integración preparada para Microsoft Azure AD.
* Autenticación mediante `Bearer Token`.
* Aplicación sin estado mediante `STATELESS`.
* Protección de todos los endpoints que no estén explícitamente permitidos.
* Configuración CORS.
* Endpoint de login SSO disponible sin autenticación previa.
* Endpoint de health check disponible públicamente.
* Documentación Swagger disponible públicamente.
* Respuesta `401 Unauthorized` personalizada para solicitudes sin autenticación válida.

### Endpoints públicos

Los siguientes endpoints no requieren un JWT:

```text
POST /api/usuarios/login

GET /actuator/health

GET /v3/api-docs/**

GET /swagger-ui/**

GET /swagger-ui.html
```

Las solicitudes `OPTIONS` también están permitidas para facilitar el funcionamiento de CORS.

### Endpoints protegidos

Todos los demás endpoints requieren autenticación mediante:

```http
Authorization: Bearer <JWT>
```

La configuración actual deja preparado el microservicio para validar tokens emitidos por Azure AD mediante su `issuer-uri`, `tenant-id` y las claves públicas correspondientes.

## 👤 Gestión de Usuarios

### Endpoints Principales

| Método   | Endpoint              | Autenticación | Descripción                  |
| :------- | :-------------------- | :------------ | :--------------------------- |
| `POST`   | `/api/usuarios`       | JWT           | Crear un nuevo usuario       |
| `GET`    | `/api/usuarios`       | JWT           | Listar todos los usuarios    |
| `GET`    | `/api/usuarios/{id}`  | JWT           | Obtener usuario por ID       |
| `POST`   | `/api/usuarios/login` | Público       | Login mediante SSO Microsoft |
| `PUT`    | `/api/usuarios/{id}`  | JWT           | Actualizar perfil de usuario |
| `DELETE` | `/api/usuarios/{id}`  | JWT           | Eliminar usuario             |

## 🔑 Login mediante SSO

El endpoint:

```http
POST /api/usuarios/login
```

permite realizar la búsqueda del usuario utilizando el correo obtenido desde el proceso de autenticación SSO.

El request utiliza `SsoLoginRequestDTO`:

```json
{
  "correo": "usuario@ejemplo.com"
}
```

El endpoint devuelve los datos del usuario mediante `UsuarioDTO`, sin exponer información sensible de la entidad.

## 👑 Endpoints Internos de Administración

El microservicio dispone de endpoints internos destinados a operaciones administrativas realizadas desde el BFF.

Base:

```text
/internal/admin/usuarios
```

| Método | Endpoint                        | Descripción                          |
| :----- | :------------------------------ | :----------------------------------- |
| `POST` | `/internal/admin/usuarios`      | Crear usuario con rol seleccionado   |
| `PUT`  | `/internal/admin/usuarios/{id}` | Actualizar usuario incluyendo su rol |

Estos endpoints están diseñados para ser consumidos internamente por el **BFF**, permitiendo realizar operaciones administrativas que no forman parte del flujo normal de gestión del usuario.

## 📦 DTOs

Para evitar exponer directamente la entidad `Usuario`, el microservicio utiliza diferentes DTOs según la operación.

### `UsuarioDTO`

DTO utilizado para entregar información del usuario como respuesta.

Incluye:

```text
id
nombre
telefono
correo
edad
genero
direccion
ocupacion
fotoUrl
tipoUsuario
```

No contiene contraseñas ni otros campos sensibles.

### `UsuarioRequestDTO`

DTO utilizado para crear usuarios.

Incluye validaciones para:

* Nombre entre 3 y 50 caracteres.
* Edad entre 18 y 100 años.
* Correo válido.
* Teléfono obligatorio.
* Dirección obligatoria.
* Tipo de usuario obligatorio.
* Rol limitado a:

```text
admin
cliente
```

### `UsuarioUpdateDTO`

DTO utilizado para actualizar los datos de un usuario.

Permite modificar:

```text
nombre
edad
genero
telefono
fotoUrl
ocupacion
direccion
tipoUsuario
```

El correo no forma parte del DTO de actualización, evitando que pueda modificarse mediante esta operación.

### `SsoLoginRequestDTO`

DTO utilizado exclusivamente para el proceso de login mediante SSO.

```text
correo
```

El correo es validado mediante Jakarta Validation.

## ✅ Validaciones

El microservicio utiliza Jakarta Bean Validation para validar los datos recibidos desde las peticiones HTTP.

Entre las principales validaciones se encuentran:

```text
Nombre:
3 - 50 caracteres

Edad:
18 - 100 años

Correo:
Formato válido y obligatorio

Teléfono:
Obligatorio

Dirección:
Obligatoria durante el registro

Tipo de usuario:
admin | cliente
```

Las validaciones se ejecutan mediante `@Valid` en los controladores.

## 🌐 CORS

El microservicio cuenta con una configuración CORS que permite la comunicación con el frontend y el API Gateway/BFF.

Se permiten los siguientes métodos:

```text
GET
POST
PUT
DELETE
OPTIONS
```

Además, se permiten headers necesarios para la autenticación mediante JWT.

## 📖 Documentación API

La API utiliza **OpenAPI / Swagger** para documentar los endpoints disponibles.

Rutas disponibles:

```text
/swagger-ui.html
/swagger-ui/**
/v3/api-docs/**
```

Estas rutas están configuradas como públicas para facilitar la consulta y prueba de la API durante el desarrollo.

## ⚙️ Instalación y Ejecución

### Requisitos Previos

* JDK 17
* Maven 3.8+
* PostgreSQL 15
* Docker
* Cuenta/configuración de Microsoft Azure AD para autenticación SSO

### Variables de Entorno

La configuración de seguridad utiliza propiedades para definir el `issuer` y `tenant` de Azure AD.

| Variable / Propiedad                                   | Descripción                          |
| :----------------------------------------------------- | :----------------------------------- |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | URL base del issuer de Azure AD      |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_TENANT_ID`  | Identificador del tenant de Azure AD |
| `SPRING_DATASOURCE_URL`                                | URL de conexión a PostgreSQL         |
| `SPRING_DATASOURCE_USERNAME`                           | Usuario de PostgreSQL                |
| `SPRING_DATASOURCE_PASSWORD`                           | Contraseña de PostgreSQL             |

Ejemplo:

```text
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=<ISSUER_AZURE>
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_TENANT_ID=<TENANT_ID>
SPRING_DATASOURCE_URL=jdbc:postgresql://<HOST_BD>:5432/db_usuarios
SPRING_DATASOURCE_USERNAME=<USUARIO_BD>
SPRING_DATASOURCE_PASSWORD=<PASSWORD_BD>
```

> **Nota:** Los valores reales de Azure AD y las credenciales de PostgreSQL deben configurarse mediante variables de entorno y no deben almacenarse directamente en el repositorio.

## 🔨 Compilación Local

Para compilar el proyecto sin ejecutar los tests:

```bash
mvn clean package -DskipTests
```

Para ejecutar los tests:

```bash
mvn test
```

Para ejecutar la aplicación localmente:

```bash
mvn spring-boot:run
```

## 🐳 Despliegue con Docker

### Construir la imagen

```bash
docker build -t pedidos360/ms-usuarios:v1 .
```

### Ejecutar el contenedor

```bash
docker run -d \
  --name ms-usuarios \
  -p 8082:8082 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://<HOST_BD>:5432/db_usuarios" \
  -e SPRING_DATASOURCE_USERNAME="<USUARIO_BD>" \
  -e SPRING_DATASOURCE_PASSWORD="<PASSWORD_BD>" \
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI="<ISSUER_AZURE>" \
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_TENANT_ID="<TENANT_ID>" \
  pedidos360/ms-usuarios:v1
```

## 🔄 Flujo General

```text
                    ┌─────────────────┐
                    │    Frontend     │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   BFF / API     │
                    │    Gateway      │
                    └────────┬────────┘
                             │
                    JWT / HTTP Request
                             │
                             ▼
                  ┌─────────────────────┐
                  │    ms-usuarios      │
                  │                     │
                  │  Controller         │
                  │       ↓             │
                  │  Service            │
                  │       ↓             │
                  │  Repository         │
                  └─────────┬───────────┘
                            │
                            ▼
                    ┌─────────────────┐
                    │  PostgreSQL     │
                    │  db_usuarios    │
                    └─────────────────┘

          ┌──────────────────────────────┐
          │ Microsoft Azure AD / SSO     │
          │                              │
          │ Validación de JWT            │
          └──────────────────────────────┘
```

## 🔗 Integración con Pedidos360

El microservicio forma parte del ecosistema de microservicios de **Pedidos360**:

```text
Pedidos360
│
├── ms-usuarios
│      └── db_usuarios
│
├── ms-productos
│      └── db_productos
│
├── ms-carrito
│      └── db_carrito
│
├── BFF / API Gateway
│
└── Frontend React
```

Cada microservicio mantiene responsabilidades independientes y su propia base de datos, mientras que el BFF actúa como punto de orquestación entre el frontend y los servicios backend.

## 🔒 Consideraciones de Seguridad

* Los endpoints protegidos requieren un JWT válido.
* El microservicio está preparado para autenticación mediante Microsoft Azure AD.
* La aplicación utiliza sesiones `STATELESS`.
* Las contraseñas no forman parte de la entidad ni de los DTO actuales.
* Los DTO permiten controlar explícitamente qué información se expone.
* El correo no puede modificarse mediante el endpoint normal de actualización.
* Los roles permitidos son `admin` y `cliente`.
* Las credenciales de base de datos y configuración sensible de Azure deben manejarse mediante variables de entorno.
* La configuración de seguridad permite incorporar progresivamente reglas de autorización basadas en roles.

---
## 🔗 Ecosistema de Repositorios

### Backend

* [Microservicio Carrito](https://github.com/Raynagah/cloud-backend-carrito)
* [BFF Orchestrator](https://github.com/Raynagah/cloud-backend-bff)
* [Microservicio Producto](https://github.com/Raynagah/cloud-backend-producto)
* **Este repositorio:** Microservicio que maneja Usuarios

### Frontend

* [Frontend React](https://github.com/Raynagah/cloud-frontend.git)

### Base de Datos

* [Base de Datos](https://github.com/NBello26/ms-bd-cloud.git)

---
## 🚀 Pedidos360

**ms-usuarios** proporciona la gestión centralizada de usuarios para la plataforma **Pedidos360**, integrándose con PostgreSQL para persistencia y con Microsoft Azure AD para autenticación basada en tokens JWT.
