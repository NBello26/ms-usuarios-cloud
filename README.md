# 👤 Microservicio de Usuarios - Sistema de Gestión de Mascotas

Este repositorio contiene el microservicio de **Usuarios**, desarrollado con Spring Boot. Es el componente encargado de gestionar la lógica relacionada con las cuentas de usuario, perfiles, información de contacto y autenticación dentro del ecosistema. Toda la información persistente de los usuarios se almacena en la base de datos central (SanosDB).

## 🚀 Arquitectura y Prácticas DevOps

Diseñado bajo los principios de sistemas distribuidos, este microservicio es autónomo y está preparado para integrarse perfectamente en el entorno cloud. Su ciclo de vida está completamente gestionado por prácticas de integración y despliegue continuo.

El flujo de despliegue automatizado a través de **GitHub Actions (CI/CD)** incluye:
1. Compilación y testing del código fuente en Java.
2. Creación de una imagen Docker optimizada con el ejecutable del microservicio.
3. Publicación de la imagen en **Amazon Elastic Container Registry (ECR)**.
4. Despliegue en la instancia **EC2** correspondiente (Nodo Back 2) utilizando **AWS Systems Manager (SSM)**.
5. Inyección dinámica de secretos de infraestructura (credenciales de BD, rutas de red interna y Service Discovery).

### 🌐 Ecosistema de Infraestructura en AWS
Este microservicio reside en el segundo nodo de backend, optimizando el uso de recursos al compartir instancia con el servicio de Mascotas:

* **Nodo Web:** ApiGateway y Frontend
* **Nodo Back 1:** Eureka Server y BFF
* **Nodo Back 2:** Microservicio de Mascotas y **Microservicio de Usuarios** (Este repositorio) 📍
* **Nodo Back 3:** Microservicios de Geolocalización y Notificaciones
* **Nodo Back 4:** Motor de Coincidencias
* **Base de Datos:** SanosDB (PostgreSQL)

## 🛠️ Tecnologías Principales

* **Framework:** Java 17 / Spring Boot 3
* **Persistencia:** Spring Data JPA / Hibernate
* **Cloud Native:** Spring Cloud Netflix Eureka Client
* **Contenedores:** Docker
* **CI/CD:** GitHub Actions
* **Infraestructura AWS:** EC2, ECR, SSM, IAM

## ⚙️ Descubrimiento y Redes

* **Integración de Datos:** Se conecta de forma segura a través de la VPC privada de AWS hacia el nodo de la base de datos central.
* **Auto-Registro (Eureka):** Al arrancar el contenedor, el servicio captura la IP local de su instancia EC2 y se registra automáticamente en el servidor Eureka (bajo un identificador como `MS-USUARIOS`). Esto garantiza que el BFF y otros microservicios puedan consumir sus APIs sin requerir configuraciones de red estáticas.

## 📦 Repositorios del Proyecto

Explora el resto de la infraestructura y microservicios de este ecosistema:

**Frontend y Puertas de Enlace**
* 🌐 [Frontend_eft_fullstack_III](https://github.com/NBello26/Frontend_eft_fullstack_III)
* 🚪 [ApiGateway_eft_fullstack_III](https://github.com/NBello26/ApiGateway_eft_fullstack_III)
* 🌉 [BFF_eft_fullstack_III](https://github.com/NBello26/BFF_eft_fullstack_III)

**Descubrimiento y Base de Datos**
* 🧭 [Eureka_eft_fullstack_III](https://github.com/NBello26/Eureka_eft_fullstack_III)
* 🗄️ [BD_eft_fullstack_III](https://github.com/NBello26/BD_eft_fullstack_III)

**Microservicios de Negocio**
* 🐾 [Reporte_Mascota_eft_fullstack_III](https://github.com/NBello26/Reporte_Mascota_eft_fullstack_III)
* 👤 [Usuarios_eft_fullstack_III](https://github.com/NBello26/Usuarios_eft_fullstack_III) *(Estás aquí)*
* 📍 [Geolocalizacion_eft_fullstack_III](https://github.com/NBello26/Geolocalizacion_eft_fullstack_III)
* 🔔 [Notificaciones_eft_fullstack_III](https://github.com/NBello26/Notificaciones_eft_fullstack_III)
* 🧩 [Coincidencias_eft_fullstack_III](https://github.com/NBello26/Coincidencias_eft_fullstack_III)

---
*Desarrollado como parte del proyecto final de integración de arquitectura DevOps.*
