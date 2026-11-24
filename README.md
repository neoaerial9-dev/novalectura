# Nova Lectura - Tienda de libros usados

Aplicación web para comprar y vender libros usados, desarrollada para **Librería Nova Lectura S.A.C.**

Cualquier usuario puede publicar libros y comprar los de otros (sin comprar los suyos); la librería administra categorías, usuarios y pedidos.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Frontend | Angular 22.2.7, SCSS |
| Backend | Spring Boot 4.1.2, Java 21, Maven, Spring Security + JWT, Spring Data JPA |
| Base de datos | MySQL 8 |
| Documentación API | springdoc / Swagger UI |

Arquitectura cliente-servidor en tres capas: Angular -> API REST -> MySQL.

## Estructura del repositorio

```
.
├── api/    Backend Spring Boot (paquete com.novalectura.api)
├── app/    Frontend Angular
└── README.md
```

## Ejecución

### 1. Requisitos
- JDK 21
- Node.js 22 y npm
- MySQL 8 en ejecución

### 2. Backend
```bash
cd api
export DB_PASSWORD=tu_clave_de_mysql      # Windows (PowerShell): $env:DB_PASSWORD="tu_clave"
./mvnw spring-boot:run
```
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- La base `novalectura` se crea sola.

### 3. Frontend
```bash
cd app
npm install
npm start
```
Abre http://localhost:4200.

### 4. Primer ingreso
Al arrancar por primera vez se crea un administrador:

| Correo | Contraseña |
|---|---|
| `admin@novalectura.com` | `Admin12345` |

Cámbialos con las variables de entorno `ADMIN_CORREO` y `ADMIN_CONTRASENA`. Los usuarios que se registran desde la web son siempre clientes. Antes de crear libros, el administrador debe crear al menos una categoría (**Categorías** en la barra superior).

## Variables de entorno del backend

| Variable | Por defecto | Uso |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | `localhost`, `3306`, `novalectura` | Conexión a MySQL |
| `DB_USER`, `DB_PASSWORD` | `root`, *(vacío)* | Credenciales de MySQL |
| `JWT_SECRET` | clave de desarrollo | Secreto de firma (mínimo 32 caracteres) |
| `JWT_EXPIRATION_MS` | `86400000` | Vigencia del token (24 h) |
| `CORS_ORIGINS` | `http://localhost:4200` | Orígenes permitidos |
| `ADMIN_CORREO`, `ADMIN_CONTRASENA` | ver arriba | Administrador inicial |
| `UPLOADS_DIR` | `uploads` | Carpeta de las portadas subidas (en producción, una ruta persistente y con respaldo) |
| `UPLOADS_RETENCION_HORAS`, `UPLOADS_LIMPIEZA_CRON` | `24`, `0 0 3 * * *` | Limpieza nocturna de portadas que ningún libro usa |

## Pruebas

```bash
cd api && ./mvnw test      # JUnit 5 + Mockito (servicios con TDD); reporte JaCoCo en target/site/jacoco
```

## Funcionalidades

- **Visitantes:** catálogo con búsqueda por título, autor o categoría, filtro y detalle del libro.
- **Usuarios:** registro e inicio de sesión (JWT), carrito, compra (descuenta el stock), historial de pedidos, perfil y cambio de contraseña.
- **Vendedores (cualquier usuario):** publicar libros subiendo la portada desde su computador, editar y eliminar los suyos, y ver sus ventas con los datos del comprador.
- **Accesibilidad y móvil:** buen contraste, navegación por teclado y lectores de pantalla, y diseño adaptable a celulares.
- **Reactividad:** Inicio, catálogo, "Mis libros", carrito, pedidos, ventas y la gestión de usuarios y pedidos del administrador se actualizan solos, sin recargar. Si el stock de un libro del carrito cambia o se da de baja, se avisa antes de comprar.
- **Precios en soles** (ej. `S/ 18.99`).
- **Administrador:** gestión de categorías, consulta de usuarios y de todos los pedidos, y moderación de cualquier libro.

## Decisiones de diseño

- Un único rol de usuario (`CLIENTE`) compra y vende; `ADMINISTRADOR` modera.
- El stock se descuenta con una actualización atómica en la base de datos para evitar sobreventas.
- Eliminar un libro es un borrado lógico, para conservar el historial de pedidos.
- El carrito muestra el precio vigente; el pedido guarda el precio del momento de la compra.
- El JWT no tiene estado en el servidor: cerrar sesión descarta el token en el navegador.
- Las portadas se guardan en disco (carpeta `UPLOADS_DIR`) con nombre aleatorio y se validan por su contenido real; en la base de datos solo queda la ruta. Cada noche se borran las que ningún libro usa.

## Fuera de alcance

Pasarelas de pago, envíos y seguimiento, facturación electrónica, reseñas y calificaciones, recomendaciones con IA, aplicación móvil, varias sucursales y reportes avanzados.
