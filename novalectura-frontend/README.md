# Nova Lectura — frontend

Frontend basado en Angular 21.2, TypeScript 5.9 y NGXS 21.

El frontend Angular consume la API del backend Spring Boot. La URL base de la API
se configura en `src/enviroment/enviroment.ts` (`http://localhost:8080/api` por
defecto).

El inicio de sesión envía `correo` y `contrasena` a `POST /auth/login`. Al
autenticarse, conserva el JWT devuelto por el backend y lo envía como
`Authorization: Bearer <token>` en las siguientes solicitudes a la API.

El registro e inicio de sesión usan `POST /auth/register` y `POST /auth/login`.
Ambos conservan el JWT devuelto por el backend para las solicitudes posteriores.
La administración consulta `GET /users` de forma paginada y muestra la fecha de
registro; el backend actual no registra la fecha del último acceso. La sección
**Mi cuenta** usa `PUT /auth/password` para cambiar la contraseña y
`POST /auth/logout` para cerrar sesión.

## Desarrollo

Requiere Node.js 20.19+, 22.12+ o 24+. Ejecuta `npm install` y luego `npm start`
para abrir el frontend en `http://localhost:4200`. `npm run build` compila la
aplicación.