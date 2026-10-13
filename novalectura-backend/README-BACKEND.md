# NovaLectura – Backend

API REST para una plataforma web de compra y venta de **libros usados**, independiente de cualquier organización: cualquier usuario registrado puede publicar libros y comprar los de otros.

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.1 (Web MVC, Data JPA, Security, Validation) |
| Base de datos | MySQL 8 o superior |
| Autenticación | JWT (JJWT 0.13.0), contraseñas con BCrypt |
| Mapeo entidad ↔ DTO | MapStruct 1.6.3 + Lombok |
| Documentación de la API | springdoc-openapi 3.1.1 (Swagger UI) |
| Construcción | Maven (incluye el wrapper `mvnw`, no necesitas instalar Maven) |

---

## Requisitos previos

1. **JDK 25** instalado. Comprueba con `java -version`.
2. **MySQL 8+** instalado y **en ejecución** (puerto 3306 por defecto).
3. Un usuario de MySQL con permiso para crear bases de datos (por ejemplo `root`).
4. IntelliJ IDEA con el plugin de **Lombok** y el procesamiento de anotaciones activado (*Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing*).

La base de datos `used_books` **se crea sola** al primer arranque y las tablas también (`ddl-auto=update`); no hace falta ejecutar ningún script.

---

## Configuración

### Archivos de configuración

| Archivo | Contenido |
|---|---|
| `src/main/resources/application.properties` | Configuración común, **sin secretos** |
| `src/main/resources/application-local.properties` | Tus valores reales: clave de MySQL, clave JWT, administrador |
| `src/main/resources/application-local.properties.txt` | Plantilla para crear el anterior |

**1. `application.properties`** debe quedar así:

```properties
spring.application.name=used-books-backend

spring.datasource.url=jdbc:mysql://localhost:3306/used_books?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.default_batch_fetch_size=50

app.jwt.secret=${JWT_SECRET:}
app.jwt.expiration-minutes=60
app.cors.allowed-origins=http://localhost:4200
app.admin.correo=${ADMIN_CORREO:}
app.admin.contrasena=${ADMIN_CONTRASENA:}
```

**2. Crea `application-local.properties`** copiando la plantilla `application-local.properties.example` y completando tus valores (ver más abajo cómo generar la clave JWT).

### Variables de configuración

| Propiedad | Variable de entorno equivalente | Obligatoria | Descripción |
|---|---|---|---|
| `spring.datasource.username` / `password` | `DB_USERNAME` / `DB_PASSWORD` | Sí | Credenciales de MySQL |
| `app.jwt.secret` | `JWT_SECRET` | **Sí** | Clave para firmar tokens: Base64 de **al menos 32 bytes** |
| `app.jwt.expiration-minutes` | — | No (60) | Vigencia del token en minutos |
| `app.admin.correo` / `contrasena` | `ADMIN_CORREO` / `ADMIN_CONTRASENA` | Recomendada | Crea el primer administrador al arrancar |
| `app.cors.allowed-origins` | — | No | Orígenes permitidos para el frontend, separados por coma |

Cada propiedad se puede dar en `application-local.properties` **o** como variable de entorno; si no está en ninguno de los dos, se usa el valor por defecto de `application.properties`.

### Generar la clave JWT

Cualquiera de estas opciones imprime una clave válida (cópiala en `app.jwt.secret`):

```powershell
# Windows PowerShell
$b = New-Object byte[] 32; (New-Object System.Security.Cryptography.RNGCryptoServiceProvider).GetBytes($b); [Convert]::ToBase64String($b)
```

```bash
# Python (cualquier sistema)
python -c "import secrets,base64;print(base64.b64encode(secrets.token_bytes(32)).decode())"

# OpenSSL / Git Bash / Linux / macOS
openssl rand -base64 32
```

#### Sobre la clave JWT

- **Es un secreto del entorno, no de la máquina.** Cada entorno puede tener la suya. Solo debe coincidir entre las instancias del backend que tengan que aceptar los mismos tokens (por ejemplo, varias réplicas detrás de un balanceador).
- Si cambias la clave, **todos los tokens emitidos dejan de ser válidos** y los usuarios deben iniciar sesión de nuevo.
- En producción usa una clave distinta a la de desarrollo, entregada por variable de entorno.

---

## Cómo ejecutar el backend

### Opción A: desde IntelliJ IDEA

1. Abre la carpeta del proyecto y espera a que Maven importe las dependencias.
2. *Run → Edit Configurations →* selecciona `UsedBooksBackendApplication`.
3. En **Active profiles** escribe `local` y guarda.
4. Pulsa *Run*.

### Opción B: desde la terminal

```bash
# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

```powershell
# Windows (PowerShell): las comillas son necesarias
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

### Opción C: generar un JAR y ejecutarlo

```bash
./mvnw clean package
java -jar target/*.jar --spring.profiles.active=local
```

### Comprobar que funciona

1. En el log debe aparecer `Started UsedBooksBackendApplication` y, la primera vez, `Usuario administrador creado: <tu correo>`.
2. Abre <http://localhost:8080/api/books>: debe responder una página vacía en JSON (`"contenido": []`).

---

## Ejecutar las pruebas

```bash
./mvnw test                         # todas
./mvnw test -Dtest=LibroServiceTest # una clase
```

Son pruebas unitarias (JUnit 5, Mockito, AssertJ): no necesitan MySQL ni la clave JWT.
Cubren entidades, seguridad, servicios, mappers, validaciones y controladores. No cubren las consultas de los repositorios ni la matriz de permisos por ruta (eso requiere pruebas de integración).

---

## Estructura del proyecto

```
src/main/java/com/novalectura/used_books_backend/
├── config/        SecurityConfig (reglas de acceso, CORS, BCrypt), AdminInitializer
├── controller/    Controladores REST (reciben la petición y delegan)
├── service/       Lógica de negocio y reglas (propiedad, stock, transacciones)
├── repository/    Acceso a datos (Spring Data JPA)
├── entity/        Entidades JPA y enums (Usuario, Libro, Categoria, Carrito, Pedido...)
├── dto/           Objetos de entrada (request) y salida (response)
├── mapper/        Conversión entidad ↔ DTO (MapStruct)
├── security/      JWT: generación, filtro, UserDetails
└── exception/     ApiException y manejo global de errores
```

Arquitectura de tres capas: **controller → service → repository**. Los controladores nunca devuelven entidades, solo DTOs.

---

## API

Todas las rutas empiezan por `/api`. Los endpoints protegidos exigen la cabecera `Authorization: Bearer <token>`. El token se obtiene en `/api/auth/login` o `/api/auth/register`.

**Niveles de acceso:** *Público* (sin sesión) · *Sesión* (cualquier usuario autenticado) ·
*Dueño o ADMIN* · *ADMIN*.

### Autenticación y cuenta

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Crea una cuenta (rol USUARIO) y devuelve un token |
| POST | `/api/auth/login` | Público | Inicia sesión y devuelve un token |
| POST | `/api/auth/logout` | Sesión | Cierra sesión (el cliente descarta el token) |
| PUT | `/api/auth/password` | Sesión | Cambia la contraseña (exige la actual) |
| GET / PUT | `/api/users/me` | Sesión | Ver / editar mi perfil |

### Usuarios (administración)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/users` | ADMIN | Lista paginada de usuarios |
| GET | `/api/users/{id}` | ADMIN | Consulta un usuario |

### Categorías

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/categories` | Público | Lista las categorías |
| POST | `/api/categories` | ADMIN | Crea una categoría |
| PUT / DELETE | `/api/categories/{id}` | ADMIN | Edita / elimina (no si tiene libros) |

### Libros

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/books` | Público | Catálogo paginado |
| GET | `/api/books/search?q=` | Público | Busca por título, autor o categoría |
| GET | `/api/books/{id}` | Público | Detalle de un libro |
| GET | `/api/books/mine` | Sesión | Mis publicaciones |
| POST | `/api/books` | Sesión | Publica un libro (el vendedor eres tú) |
| PUT / DELETE | `/api/books/{id}` | Dueño o ADMIN | Edita / da de baja un libro |

Parámetros de los listados: `pagina` (desde 0), `tamano` (1–50, por defecto 12), `idCategoria`,
`q` (solo en `/search`) y `orden` (`precio_asc`, `precio_desc`; por defecto, más recientes primero).

### Carrito

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/cart` | Sesión | Ver mi carrito |
| POST | `/api/cart/items` | Sesión | Agregar un libro |
| PUT | `/api/cart/items/{id}` | Sesión | Cambiar la cantidad de una línea |
| DELETE | `/api/cart/items/{id}` | Sesión | Quitar una línea |
| DELETE | `/api/cart` | Sesión | Vaciar el carrito |

### Pedidos

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/orders` | Sesión | Registra el pedido desde mi carrito (sin cuerpo) |
| GET | `/api/orders` | Sesión | Mi historial (el ADMIN ve todos) |
| GET | `/api/orders/{id}` | Dueño o ADMIN | Detalle de un pedido |

### Ejemplos de cuerpos (JSON)

```jsonc
// POST /api/auth/register
{
  "nombres": "Ana",
  "apellidos": "Quispe",
  "correo": "ana@mail.com",
  "contrasena": "clave12345"
}

// POST /api/auth/login
{
  "correo": "ana@mail.com",
  "contrasena": "clave12345"
}

// POST /api/categories (solo ADMIN)
{
  "nombre": "Fantasía",
  "descripcion": "Magia y aventuras"
}

// POST /api/books
{
  "titulo": "Dune",
  "autor": "Frank Herbert",
  "precio": 24.99,
  "stock": 1,
  "descripcion": "Edición de bolsillo",
  "imagen": "https://ejemplo.com/dune.jpg",
  "condicion": "BUENO",
  "idCategoria": 1
}
// condicion: COMO_NUEVO | MUY_BUENO | BUENO | ACEPTABLE

// POST /api/cart/items
{
  "idLibro": 1,
  "cantidad": 1
}
```

### Flujo de prueba sugerido

1. Inicia sesión con el **administrador** y crea una categoría (`POST /api/categories`).
2. Registra un **usuario A** y publica un libro (`POST /api/books`).
3. Registra un **usuario B**, agrega ese libro al carrito (`POST /api/cart/items`) y confirma con `POST /api/orders`.
4. Comprueba que el stock del libro bajó y que el pedido aparece en `GET /api/orders`.
5. Intenta que el usuario A agregue su propio libro al carrito: debe responder **400**.

### Errores

Los errores usan el formato estándar `ProblemDetail` (`status`, `detail`). Los de validación incluyen además `errores` con el mensaje por campo.

| Código | Cuándo |
|---|---|
| 400 | Datos inválidos o regla de negocio (carrito vacío, comprar tu propio libro...) |
| 401 | Token ausente, inválido o expirado; credenciales incorrectas |
| 403 | Sin permisos (editar un libro ajeno, ruta solo para ADMIN) |
| 404 | El recurso no existe (o un pedido que no es tuyo) |
| 409 | Conflicto: correo repetido, stock insuficiente, categoría con libros |

---

## Reglas de negocio principales

- **Roles:** el registro público siempre crea `USUARIO`. El único `ADMIN` inicial lo crea `AdminInitializer` a partir de `ADMIN_CORREO` / `ADMIN_CONTRASENA`.
- **Libros:** un usuario solo edita o elimina **sus** libros (el ADMIN puede con todos). Eliminar es una **baja lógica**: el libro desaparece del catálogo pero los pedidos históricos se conservan.
- **Carrito:** no puedes agregar tu propio libro; no puedes pedir más que el stock disponible. Al consultarlo se actualizan los precios y se retiran los libros dados de baja.
- **Pedidos:** al confirmar se valida todo antes de modificar nada; si falta stock en cualquier libro, no se crea el pedido ni se descuenta nada (todo o nada). Si dos compradores se llevan el último ejemplar a la vez, uno recibe 409. El pedido guarda el precio de ese momento y queda en estado `PENDIENTE`.
- **Categorías:** no se puede eliminar una categoría que todavía tiene libros.

---

## Seguridad

- Contraseñas almacenadas con **BCrypt**; nunca se devuelven en ninguna respuesta.
- API **stateless** con JWT; CSRF desactivado porque no se usan cookies de sesión.
- El orden de los listados solo acepta valores de una lista blanca.
- Los errores internos de base de datos no se exponen al cliente.
- **Cerrar sesión no invalida el token en el servidor:** sigue siendo válido hasta que expire (por eso la vigencia por defecto es de 60 minutos).

---

## Solución de problemas

| Síntoma | Causa y solución |
|---|---|
| `Falta la clave JWT. Define la variable de entorno JWT_SECRET` | No hay `app.jwt.secret`. Verifica que `application-local.properties` exista y que el perfil `local` esté activo (o define `JWT_SECRET`) |
| `JWT_SECRET debe tener al menos 32 bytes` | La clave es muy corta o no es Base64. Genera una nueva con los comandos de arriba |
| `Communications link failure` / `Connection refused` | MySQL no está en ejecución o no escucha en el puerto 3306 |
| `Access denied for user 'root'` | Usuario o contraseña de MySQL incorrectos en `application-local.properties` |
| `Port 8080 was already in use` | Otro programa usa el puerto; agrega `server.port=8081` al archivo local |
| 401 en una ruta protegida | Falta la cabecera `Authorization: Bearer ...` o el token expiró (60 min) |
| 403 en una ruta protegida | Tu rol no alcanza (por ejemplo, una ruta solo para ADMIN) |
| IntelliJ marca en rojo clases `*MapperImpl` | Las genera MapStruct al compilar. Recarga Maven, activa *Annotation processing* y ejecuta *Build → Rebuild Project* |
| Errores de Lombok (`cannot find symbol` en getters) | Instala el plugin de Lombok y activa el procesamiento de anotaciones |
| Quieres reiniciar los datos | Elimina la base de datos `used_books` (`DROP DATABASE used_books;`) y vuelve a arrancar |

---

## Alcance y limitaciones

Proyecto con **alcance académico**. Quedan fuera: pasarelas de pago, gestión de envíos y seguimiento de pedidos, facturación electrónica, reseñas, recomendaciones y aplicación móvil.
Además:

- Las imágenes de los libros son una **URL** que envía el cliente; no hay subida de archivos.
- No hay recuperación de contraseña, verificación de correo ni renovación de tokens (*refresh tokens*).
- No hay forma de contactar al vendedor desde el pedido (solo se muestra su nombre).
