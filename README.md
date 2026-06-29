# SumativaCloudNative

Proyecto Cloud Native desarrollado en Java/Spring Boot, orientado a la gestión de guías y operaciones sobre archivos en AWS S3 + persistencia en EFS.

## Descripción

Este repositorio contiene una API REST que permite:

- Crear, actualizar, eliminar y listar **guías de despacho**.
- Descargar documentos asociados a guías.
- Gestionar objetos en S3 (listar, subir, descargar, mover, eliminar).
- Guardar archivos en EFS como parte del flujo de carga.

## Tecnologías principales

- **Java** (Spring Boot)
- **Dockerfile** (contenedorización)
- **AWS S3** (almacenamiento de objetos)
- **AWS EFS** (almacenamiento de archivos)
- OAuth2 Resource Server (JWT issuer configurado)

## Configuración base

Archivo: `src/main/resources/application.properties`

- `server.port=8080`
- `efs.path=/app/efs`
- `aws.s3.bucket=${S3_BUCKET_NAME}`

> Debes definir la variable de entorno `S3_BUCKET_NAME` para los endpoints de guías que usan bucket por configuración.

## Cómo ejecutar

### Requisitos

- Java 17+ (recomendado según versión de Spring usada)
- Maven/Gradle (según build del proyecto)
- Variables y credenciales AWS configuradas
- Acceso a bucket S3 y ruta EFS

### Ejecución local (general)

```bash
# ejemplo típico con Maven
./mvnw spring-boot:run
```

La API quedará disponible en:

- `http://localhost:8080`

---

## Endpoints disponibles

## 1) Módulo Guías (`/guias`)

### 1.1 Crear guía
- **Método:** `POST`
- **Ruta:** `/guias`
- **Descripción:** Crea una guía, guarda en EFS y sube a S3.
- **Body (JSON):** `GuiaRequest`
- **Respuesta:** `201 Created` con `GuiaDespacho`

```bash
curl -X POST "http://localhost:8080/guias" \
  -H "Content-Type: application/json" \
  -d '{
    "campoEjemplo": "valor"
  }'
```

---

### 1.2 Descargar guía
- **Método:** `GET`
- **Ruta:** `/guias/download`
- **Query param requerido:** `s3Key`
- **Descripción:** Descarga una guía PDF por ruta completa en S3.
- **Respuesta:** `200 OK` (`application/pdf`)

```bash
curl -X GET "http://localhost:8080/guias/download?s3Key=2026-06-29/transportista/guia-001.pdf" \
  --output guia-001.pdf
```

---

### 1.3 Actualizar guía
- **Método:** `PUT`
- **Ruta:** `/guias`
- **Query param requerido:** `s3KeyOriginal`
- **Body (JSON):** `GuiaRequest`
- **Descripción:** Modifica una guía existente.
- **Respuesta:** `200 OK` con `GuiaDespacho`

```bash
curl -X PUT "http://localhost:8080/guias?s3KeyOriginal=2026-06-29/transportista/guia-001.pdf" \
  -H "Content-Type: application/json" \
  -d '{
    "campoEjemplo": "nuevoValor"
  }'
```

---

### 1.4 Eliminar guía
- **Método:** `DELETE`
- **Ruta:** `/guias`
- **Query param requerido:** `s3Key`
- **Descripción:** Elimina guía por ruta completa.
- **Respuesta:** `204 No Content`

```bash
curl -X DELETE "http://localhost:8080/guias?s3Key=2026-06-29/transportista/guia-001.pdf"
```

---

### 1.5 Listar guías por transportista (y fecha opcional)
- **Método:** `GET`
- **Ruta:** `/guias/{transportista}`
- **Path param:** `transportista`
- **Query param opcional:** `fecha`
- **Descripción:** Lista keys en S3 filtrando por transportista, con opción de filtrar además por fecha.
- **Respuesta:** `200 OK` con `List<String>`

```bash
# sin fecha
curl -X GET "http://localhost:8080/guias/transportes-xyz"

# con fecha
curl -X GET "http://localhost:8080/guias/transportes-xyz?fecha=2026-06-29"
```

---

### 1.6 Listar todas las guías
- **Método:** `GET`
- **Ruta:** `/guias`
- **Descripción:** Lista todas las guías del bucket configurado (`aws.s3.bucket`).
- **Respuesta:** `200 OK` con `List<String>`

```bash
curl -X GET "http://localhost:8080/guias"
```

---

## 2) Módulo S3 (`/s3`)

> Estos endpoints reciben el nombre del bucket por path param: `/{bucket}`.

### 2.1 Listar objetos de un bucket
- **Método:** `GET`
- **Ruta:** `/s3/{bucket}/objects`
- **Descripción:** Lista objetos con metadatos.
- **Respuesta:** `200 OK` con `List<S3ObjectDto>`

```bash
curl -X GET "http://localhost:8080/s3/mi-bucket/objects"
```

---

### 2.2 Descargar objeto
- **Método:** `GET`
- **Ruta:** `/s3/{bucket}/object`
- **Query param requerido:** `key`
- **Descripción:** Descarga un objeto como bytes.
- **Respuesta:** `200 OK` (`application/octet-stream`)

```bash
curl -X GET "http://localhost:8080/s3/mi-bucket/object?key=carpeta/archivo.pdf" \
  --output archivo.pdf
```

---

### 2.3 Subir objeto
- **Método:** `POST`
- **Ruta:** `/s3/{bucket}/object`
- **Form params requeridos:**
  - `key` (string)
  - `file` (multipart file)
- **Descripción:** Sube archivo a S3 y además lo guarda en EFS.
- **Respuesta:** `201 Created`

```bash
curl -X POST "http://localhost:8080/s3/mi-bucket/object" \
  -F "key=carpeta/archivo.pdf" \
  -F "file=@./archivo.pdf"
```

---

### 2.4 Mover objeto
- **Método:** `POST`
- **Ruta:** `/s3/{bucket}/move`
- **Query params requeridos:**
  - `sourceKey`
  - `destKey`
- **Descripción:** Mueve un objeto dentro del mismo bucket.
- **Respuesta:** `200 OK`

```bash
curl -X POST "http://localhost:8080/s3/mi-bucket/move?sourceKey=origen/a.pdf&destKey=destino/a.pdf"
```

---

### 2.5 Eliminar objeto
- **Método:** `DELETE`
- **Ruta:** `/s3/{bucket}/object`
- **Query param requerido:** `key`
- **Descripción:** Elimina un objeto del bucket.
- **Respuesta:** `204 No Content`

```bash
curl -X DELETE "http://localhost:8080/s3/mi-bucket/object?key=carpeta/archivo.pdf"
```

---

## Seguridad

El proyecto tiene configuración de Resource Server JWT con issuer de Azure AD en `application.properties`.  
Para ambientes protegidos, envía token Bearer válido en tus requests:

```bash
-H "Authorization: Bearer <token>"
```

---

## Estructura sugerida del proyecto

- `controller/GuiaController.java` → endpoints de guías
- `controller/AwsS3Controller.java` → endpoints de operaciones S3
- `service/...` → lógica de negocio (S3, EFS, guías)
- `resources/application.properties` → configuración principal

---

## Estado del proyecto

Proyecto académico en evolución, con cambios iterativos durante el curso/sumativas.
