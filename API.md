# API Completa — Sistema de Planillas La Cesta

> Documentación de referencia para el equipo de Frontend. Incluye todos los endpoints REST, roles requeridos, formatos de request/response y las validaciones internas que aplica el backend.
>
> **Base URL:** `http://localhost:8080`  
> **Swagger UI:** `http://localhost:8080/swagger-ui`  
> **Autenticación:** Header `X-API-Key: <api-key>` en todas las peticiones excepto login.

---

## Tabla de contenidos

1. [Autenticación y roles](#1-autenticación-y-roles)
2. [Formato de errores](#2-formato-de-errores)
3. [Resumen de endpoints](#3-resumen-de-endpoints)
4. [Módulo Autenticación](#4-módulo-autenticación)
5. [Módulo Usuarios](#5-módulo-usuarios)
6. [Módulo Empresa y Dashboard](#6-módulo-empresa-y-dashboard)
7. [Módulo Empleados](#7-módulo-empleados)
8. [Módulo Turnos](#8-módulo-turnos)
9. [Módulo Ausencias e Incapacidades](#9-módulo-ausencias-e-incapacidades)
10. [Módulo Planillas](#10-módulo-planillas)
11. [Módulo Boletas y PDF](#11-módulo-boletas-y-pdf)
12. [Módulo Parámetros Legales](#12-módulo-parámetros-legales)
13. [Módulo Parámetros de Empresa](#13-módulo-parámetros-de-empresa)
14. [Enums y valores válidos](#14-enums-y-valores-válidos)
15. [Motor de cálculo — reglas del backend](#15-motor-de-cálculo--reglas-del-backend)
16. [Validaciones internas por módulo](#16-validaciones-internas-por-módulo)
17. [Datos de prueba (DataSeeder)](#17-datos-de-prueba-dataseeder)

---

## 1. Autenticación y roles

### Login

`POST /api/auth/login`

El usuario inicia sesión con correo y contraseña. El backend devuelve una `apiKey` que el frontend debe almacenar y enviar en el header `X-API-Key` de todas las demás peticiones.

### Roles

| Rol | Permisos |
| --- | --- |
| `RRHH` | Acceso total: empleados, usuarios, planillas, parámetros, ausencias, turnos. |
| `AUXILIAR` | Registrar/actualizar ausencias, capturar detalles de planilla, consultar empleados/turnos. |
| `GERENCIA` | Solo consulta: dashboard, planillas, empleados, boletas, parámetros. |

### Seguridad

- Contraseñas y API keys se almacenan con hash SHA-256.
- Todos los endpoints (excepto `/api/auth/login`, Swagger y actuator) requieren `X-API-Key` válida.
- Respuestas de seguridad:
  - `401 NO_AUTENTICADO` → falta o es inválida la API key.
  - `403 ACCESO_DENEGADO` → el rol no tiene permiso.

---

## 2. Formato de errores

Todas las respuestas de error usan el mismo formato:

```json
{
  "codigo": "VALIDACION",
  "mensaje": "La solicitud contiene errores de validación.",
  "detalles": [
    { "campo": "dui", "error": "El DUI debe tener el formato ########-#" }
  ]
}
```

| HTTP | `codigo` | Cuándo ocurre |
| --- | --- | --- |
| `400` | `VALIDACION` | Errores Bean Validation (campos obligatorios, formatos, etc.). |
| `401` | `NO_AUTENTICADO` | Falta o es inválida la `X-API-Key`. |
| `403` | `ACCESO_DENEGADO` | Rol sin permiso para el recurso. |
| `404` | `NO_ENCONTRADO` | Recurso no existe. |
| `409` | `CONFLICTO` | DUI duplicado, planilla Quincena 25 duplicada, etc. |
| `422` | `NEGOCIO` | Regla de negocio violada (salario < mínimo, planilla ya aprobada, etc.). |
| `500` | `ERROR_INTERNO` | Error inesperado del servidor. |

---

## 3. Resumen de endpoints

| Método | Endpoint | Rol | Descripción |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | Público | Login y obtención de API key. |
| POST | `/api/usuarios` | RRHH | Crear usuario. |
| GET | `/api/usuarios` | RRHH | Listar usuarios. |
| PATCH | `/api/usuarios/{id}/activar` | RRHH | Activar usuario. |
| PATCH | `/api/usuarios/{id}/inactivar` | RRHH | Inactivar usuario. |
| POST | `/api/usuarios/{id}/regenerar-api-key` | RRHH | Regenerar API key. |
| PATCH | `/api/usuarios/{id}/cambiar-password` | Cualquiera | Cambiar contraseña propia. |
| GET | `/api/empresa` | Cualquiera | Datos de branding y menú. |
| GET | `/api/dashboard` | Cualquiera | Indicadores del tablero. |
| POST | `/api/empleados` | RRHH | Crear empleado. |
| GET | `/api/empleados` | RRHH/AUX/GER | Listar con búsqueda y filtros. |
| GET | `/api/empleados/contadores` | RRHH/GER | Contadores de empleados. |
| GET | `/api/empleados/{id}` | RRHH/AUX/GER | Obtener empleado. |
| PUT | `/api/empleados/{id}` | RRHH | Actualizar empleado. |
| PATCH | `/api/empleados/{id}/estado` | RRHH | Cambiar estado. |
| PATCH | `/api/empleados/{id}/turno` | RRHH | Asignar turno. |
| POST | `/api/turnos` | RRHH | Crear turno. |
| GET | `/api/turnos` | RRHH/AUX/GER | Listar turnos. |
| GET | `/api/turnos/{id}` | RRHH/AUX/GER | Obtener turno. |
| PUT | `/api/turnos/{id}` | RRHH | Actualizar turno. |
| PATCH | `/api/turnos/{id}/estado` | RRHH | Activar/inactivar turno. |
| POST | `/api/empleados/{id}/ausencias` | RRHH/AUX | Registrar ausencia/incapacidad. |
| GET | `/api/empleados/{id}/ausencias` | RRHH/AUX/GER | Listar por empleado. |
| PUT | `/api/empleados/{id}/ausencias/{ausenciaId}` | RRHH/AUX | Actualizar ausencia. |
| DELETE | `/api/empleados/{id}/ausencias/{ausenciaId}` | RRHH | Eliminar ausencia. |
| GET | `/api/ausencias?periodo=YYYY-MM` | RRHH/AUX/GER | Listar por período. |
| POST | `/api/planillas` | RRHH | Crear planilla. |
| POST | `/api/planillas/{id}/detalles` | RRHH/AUX | Capturar detalle de empleado. |
| POST | `/api/planillas/{id}/calcular` | RRHH | Ejecutar motor de cálculo. |
| GET | `/api/planillas/{id}` | RRHH/GER | Obtener planilla. |
| POST | `/api/planillas/{id}/aprobar` | RRHH | Aprobar planilla. |
| GET | `/api/planillas` | RRHH/GER | Histórico de planillas. |
| GET | `/api/empleados/{id}/prestaciones?tipo=...` | RRHH/GER | Proyección vacaciones/aguinaldo. |
| GET | `/api/planillas/{planillaId}/empleados/{empleadoId}/boleta` | RRHH/GER | Boleta JSON. |
| GET | `/api/planillas/{planillaId}/empleados/{empleadoId}/boleta/pdf` | RRHH/GER | Boleta PDF. |
| GET | `/api/planillas/{planillaId}/exportar/pdf` | RRHH/GER | Planilla completa PDF. |
| GET | `/api/parametros-legales` | RRHH/GER | Listar parámetros legales. |
| POST | `/api/parametros-empresa` | RRHH | Crear/actualizar parámetro empresa. |
| GET | `/api/parametros-empresa` | RRHH/GER | Listar parámetros empresa. |
| GET | `/api/parametros-empresa/{clave}` | RRHH/GER | Obtener parámetro por clave. |

---

## 4. Módulo Autenticación

### POST `/api/auth/login`

Acepta **nombre de usuario (`username`) o correo electrónico** como identificador.

**Request:**

```json
{
  "usuario": "admin",
  "password": "Admin1234!"
}
```

o

```json
{
  "usuario": "admin@lacesta.com",
  "password": "Admin1234!"
}
```

**Response 200:**

```json
{
  "id": 1,
  "nombre": "Administrador",
  "username": "admin",
  "correo": "admin@lacesta.com",
  "rol": "RRHH",
  "apiKey": "a3f8c2d1e9b4..."
}
```

**Validaciones internas:**

- `usuario` y `password` obligatorios.
- Busca primero por `username`; si no lo encuentra, busca por `correo`.
- Verifica credenciales contra hash SHA-256.
- Al login exitoso se regenera la API key y se devuelve una sola vez.
- Usuario inactivo → error.

---

## 5. Módulo Usuarios

### POST `/api/usuarios`

**Request:**

```json
{
  "nombre": "María García",
  "username": "mgarcia",
  "correo": "maria.garcia@lacesta.com.sv",
  "rol": "AUXILIAR",
  "password": "Temporal123"
}
```

**Response 201:** objeto usuario con `apiKeyPlana` (solo esta vez) y `passwordInicial`.

**Validaciones internas:**

- Nombre, `username`, correo y rol obligatorios.
- Rol válido: `RRHH`, `AUXILIAR`, `GERENCIA`.
- `username` único (máximo 50 caracteres).
- Correo único.
- Si no se envía password, se usa el correo como contraseña temporal.

### GET `/api/usuarios`

**Response 200:** array de usuarios (sin `apiKeyPlana` ni `passwordInicial`).

### PATCH `/api/usuarios/{id}/activar`
### PATCH `/api/usuarios/{id}/inactivar`

**Response 200:** usuario actualizado.

### POST `/api/usuarios/{id}/regenerar-api-key`

**Response 200:** usuario con `apiKeyPlana` nueva (solo esta vez).

### PATCH `/api/usuarios/{id}/cambiar-password`

**Request:**

```json
{
  "passwordActual": "Temporal123",
  "passwordNueva": "NuevaSegura456"
}
```

**Response 204.**

**Validaciones internas:**

- `passwordNueva` mínimo 6 caracteres.
- `passwordActual` debe coincidir con el hash almacenado.
- La nueva contraseña se almacena con hash SHA-256.

---

## 6. Módulo Empresa y Dashboard

### GET `/api/empresa`

**Response 200:**

```json
{
  "id": 1,
  "nombre": "Supermercado La Cesta, S.A. de C.V.",
  "logoUrl": "/assets/logo.png",
  "direccion": "San Salvador, El Salvador",
  "nit": "0614-000000-000-0",
  "menu": {
    "titulo": "Sistema de Planillas",
    "version": "1.0.0",
    "modulos": ["empleados", "ausencias", "planillas", "usuarios"]
  }
}
```

### GET `/api/dashboard`

**Response 200:**

```json
{
  "totalEmpleadosActivos": 48,
  "totalEmpleadosInactivos": 7,
  "ultimoPeriodo": "2026-02",
  "totalBrutoUltimoPeriodo": 22484.40,
  "totalNetoUltimoPeriodo": 19753.95,
  "totalDeduccionesUltimoPeriodo": 1730.45
}
```

**Nota:** los campos del último período son `null` si no hay planillas en estado `CALCULADA` o `APROBADA`.  
**Cálculo:** `totalDeduccionesUltimoPeriodo` = `totalIsss` + `totalAfp` + `totalIsr`.

---

## 7. Módulo Empleados

### POST `/api/empleados`

**Request:**

```json
{
  "nombre": "Carlos",
  "apellido": "Martínez López",
  "dui": "12345678-9",
  "nit": "0614-123456-001-0",
  "correo": "carlos.martinez@ejemplo.com",
  "telefono": "72345678",
  "fechaNacimiento": "1990-03-15",
  "genero": "MASCULINO",
  "direccion": "Col. Escalón, San Salvador",
  "municipio": "San Salvador",
  "departamento": "San Salvador",
  "cargo": "Cajero",
  "departamentoLab": "Ventas",
  "fechaIngreso": "2022-06-01",
  "tipoContrato": "TIEMPO_COMPLETO",
  "salarioBase": 450.00,
  "sector": "COMERCIO_SERVICIOS",
  "afp": "CRECER",
  "numIsss": "001234567",
  "contactoEmergenciaNombre": "Ana López",
  "contactoEmergenciaTelefono": "71234567",
  "turnoId": 1,
  "esBorrador": false
}
```

**Response 201:** empleado completo con `id` y `creadoEn`.

**Validaciones internas:**

- `nombre`, `apellido`, `dui`, `fechaIngreso`, `salarioBase`, `sector`, `afp` obligatorios.
- DUI formato `########-#` y único.
- NIT formato `####-######-###-#`.
- Correo formato válido.
- Teléfono 7 u 8 dígitos.
- `salarioBase` > 0.
- Si `esBorrador` es `false` y `sector = COMERCIO_SERVICIOS`, el salario no puede ser menor a `$408.80`.
- Fecha de ingreso no futura.

### GET `/api/empleados`

**Query params opcionales:**

- `q` — búsqueda por nombre, apellido, ID o departamento.
- `estado` — `ACTIVO` | `INACTIVO`.
- `departamento` — nombre exacto del departamento laboral.

**Response 200:** array de empleados.

### GET `/api/empleados/contadores`

**Response 200:**

```json
{
  "total": 55,
  "activos": 48,
  "inactivos": 7
}
```

### GET `/api/empleados/{id}`

**Response 200:** empleado completo, incluyendo turno asignado.

### PUT `/api/empleados/{id}`

**Request/Response:** igual que POST.

**Validaciones internas:**

- No se puede cambiar el DUI a uno ya existente de otro empleado.
- Aplica validación de salario mínimo según sector.

### PATCH `/api/empleados/{id}/estado`

**Request:**

```json
{ "estado": "INACTIVO" }
```

**Response 200:** empleado actualizado.

### PATCH `/api/empleados/{id}/turno`

**Request:**

```json
{ "turnoId": 1 }
```

**Response 200:** empleado con turno asignado.

**Validaciones internas:**

- El turno debe existir y estar activo.

---

## 8. Módulo Turnos

### POST `/api/turnos`

**Request:**

```json
{
  "nombre": "Turno Tienda Lunes-Sábado",
  "descripcion": "Atención en tienda",
  "diasLaborables": ["LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO"],
  "horaEntrada": "08:00",
  "horaSalida": "17:00",
  "horasOrdinariasDiarias": 8.00
}
```

**Response 201:** turno creado.

**Validaciones internas:**

- Nombre obligatorio.
- Al menos un día laborable.
- `horaEntrada` y `horaSalida` obligatorias.
- `horasOrdinariasDiarias` > 0.
- La hora de salida debe ser posterior a la de entrada.

### GET `/api/turnos`

**Response 200:** array de turnos.

### GET `/api/turnos/{id}`

**Response 200:** turno.

### PUT `/api/turnos/{id}`

**Request/Response:** igual que POST.

### PATCH `/api/turnos/{id}/estado?estado=ACTIVO|INACTIVO`

**Response 200:** turno actualizado.

---

## 9. Módulo Ausencias e Incapacidades

### POST `/api/empleados/{id}/ausencias`

**Request:**

```json
{
  "tipo": "INCAPACIDAD_COMUN",
  "fechaInicio": "2026-02-10",
  "fechaFin": "2026-02-12",
  "dias": 3,
  "horas": null,
  "documentoRespaldoUrl": "https://...",
  "pagoPorcentaje": null,
  "observacion": "Resfrío común"
}
```

**Response 201:** ausencia registrada.

**Validaciones internas:**

- Tipo obligatorio.
- `fechaInicio` <= `fechaFin`.
- El empleado debe existir y estar activo.
- No puede solaparse con otra ausencia del mismo empleado en las mismas fechas.
- Para `AUSENCIA_POR_HORAS`, se deben indicar las `horas`.

### GET `/api/empleados/{id}/ausencias`

**Response 200:** array de ausencias del empleado (todos los tipos).

### GET `/api/empleados/{id}/incapacidades`

**Response 200:** array de incapacidades del empleado. Filtra automáticamente los registros de tipo `INCAPACIDAD_COMUN` e `INCAPACIDAD_ISSS_TOTAL`.

**Nota:** aunque el backend unificó ausencias e incapacidades en una sola entidad, este endpoint se mantiene para compatibilidad con el frontend. Devuelve `[]` si el empleado no tiene incapacidades registradas.

### PUT `/api/empleados/{id}/ausencias/{ausenciaId}`

**Request/Response:** igual que POST.

### DELETE `/api/empleados/{id}/ausencias/{ausenciaId}`

**Response 204.**

### GET `/api/ausencias?periodo=YYYY-MM`

**Response 200:** array de ausencias cuyo rango intersecta el mes indicado.

### Tipos de ausencia y su efecto en planilla

| Tipo | Efecto en planilla |
| --- | --- |
| `INCAPACIDAD_COMUN` | Días 1–3 pagados según parámetro empresa (`EMPRESA_PORCENTAJE_PAGO_3_DIAS`); días 4+ descontados. |
| `INCAPACIDAD_ISSS_TOTAL` | Todos los días descontados de planilla; reportados a ISSS/AFP. |
| `PERMISO_CON_GOCE` | No afecta el pago. |
| `PERMISO_SIN_GOCE` | Descuenta los días ausentes. |
| `FALTA_INJUSTIFICADA` | Descuenta los días y el séptimo día de la semana. |
| `AUSENCIA_POR_HORAS` | Descuenta solo las horas indicadas. |

---

## 10. Módulo Planillas

### POST `/api/planillas`

**Request:**

```json
{
  "periodoMes": "2026-02",
  "tipo": "QUINCENAL",
  "numeroQuincena": 1
}
```

**Response 201:** planilla en estado `BORRADOR`.

**Validaciones internas:**

- `periodoMes` obligatorio con formato `YYYY-MM`.
- `tipo` obligatorio: `QUINCENAL` o `QUINCENA_25`.
- Para `QUINCENAL`, `numeroQuincena` obligatorio: `1` o `2`.
- Para `QUINCENA_25`, `numeroQuincena` debe ser `null`.
- Para `QUINCENA_25`, el período debe ser enero (`YYYY-01`) y no puede existir otra planilla de ese tipo en el mismo período.
- Las fechas se calculan automáticamente según el tipo:
  - Quincena 1: 1–15 del mes.
  - Quincena 2: 16–fin de mes.
  - Quincena 25: 15–25 de enero.

### POST `/api/planillas/{id}/detalles`

**Request:**

```json
{
  "empleadoId": 3,
  "diasLaborados": 15,
  "horasExtraDiurnas": 4.0,
  "horasExtraNocturnas": 0,
  "comisiones": 0,
  "bonificaciones": 50.00,
  "descuentosVoluntarios": 0,
  "diasDescansoTrabajados": 1
}
```

**Response 200:** detalle de planilla.

**Validaciones internas:**

- El empleado debe existir y estar activo.
- La planilla no puede estar `APROBADA`.
- `diasLaborados` >= 0.
- `diasDescansoTrabajados` >= 0.

### POST `/api/planillas/{id}/calcular`

**Response 200:** planilla en estado `CALCULADA` con totales y detalles calculados.

**Validaciones internas:**

- La planilla no puede estar `APROBADA`.
- Debe tener al menos un detalle.
- Para `QUINCENA_25`, aplica lógica especial (ver sección 15).

### GET `/api/planillas/{id}`

**Response 200:** planilla completa con detalles.

### POST `/api/planillas/{id}/aprobar`

**Response 200:** planilla en estado `APROBADA`.

**Validaciones internas:**

- La planilla debe estar en estado `CALCULADA`.

### GET `/api/planillas`

**Response 200:** array de planillas ordenadas por fecha descendente.

### GET `/api/empleados/{id}/prestaciones?tipo=vacaciones|aguinaldo`

**Response 200:**

```json
{
  "empleadoId": 3,
  "nombreEmpleado": "Carlos Martínez López",
  "tipo": "vacaciones",
  "monto": 265.72,
  "descripcion": "15 días de salario + 30% de prima vacacional (Art. 177 CT)"
}
```

**Cálculo:**

- Vacaciones: 15 días de salario + 30% de prima.
- Aguinaldo: 15/19/21 días según años completos; proporcional por días trabajados si tiene < 1 año.

---

## 11. Módulo Boletas y PDF

### GET `/api/planillas/{planillaId}/empleados/{empleadoId}/boleta`

**Response 200:** boleta en JSON con desglose completo.

### GET `/api/planillas/{planillaId}/empleados/{empleadoId}/boleta/pdf`

**Response:** `Content-Type: application/pdf` descargable `boleta_{planillaId}_{empleadoId}.pdf`.

### GET `/api/planillas/{planillaId}/exportar/pdf`

**Response:** `Content-Type: application/pdf` descargable `planilla_{planillaId}.pdf`.

**Validaciones internas:**

- La planilla debe tener detalles para exportar.

---

## 12. Módulo Parámetros Legales

### GET `/api/parametros-legales`

**Response 200:** array de parámetros legales.

```json
[
  {
    "id": 1,
    "clave": "ISSS_TRABAJADOR_PORC",
    "valor": "0.03",
    "descripcion": "Porcentaje ISSS trabajador (3%)",
    "vigencia": "2026-01-01"
  }
]
```

**Nota:** si la tabla está vacía, el motor usa valores hardcoded de El Salvador 2026.

### Parámetros legales reconocidos

| Clave | Valor por defecto | Descripción |
| --- | --- | --- |
| `ISSS_TRABAJADOR_PORC` | `0.03` | ISSS trabajador |
| `ISSS_BASE_MAXIMA` | `1000.00` | Base máxima ISSS |
| `ISSS_PATRONO_PORC` | `0.075` | ISSS patronal |
| `AFP_TRABAJADOR_PORC` | `0.0725` | AFP trabajador |
| `AFP_PATRONO_PORC` | `0.0875` | AFP patronal |
| `ISR_TRAMO1_TOPE` | `275.00` | ISR exento (tabla quincenal) |
| `ISR_TRAMO2_INICIO` | `275.01` | Tramo II inicio |
| `ISR_TRAMO2_FIN` | `447.62` | Tramo II fin |
| `ISR_TRAMO2_PORC` | `0.10` | Tramo II % |
| `ISR_TRAMO2_CUOTA` | `8.83` | Tramo II cuota |
| `ISR_TRAMO3_INICIO` | `447.63` | Tramo III inicio |
| `ISR_TRAMO3_FIN` | `1019.05` | Tramo III fin |
| `ISR_TRAMO3_PORC` | `0.20` | Tramo III % |
| `ISR_TRAMO3_CUOTA` | `30.00` | Tramo III cuota |
| `ISR_TRAMO4_INICIO` | `1019.06` | Tramo IV inicio |
| `ISR_TRAMO4_PORC` | `0.30` | Tramo IV % |
| `ISR_TRAMO4_CUOTA` | `144.28` | Tramo IV cuota |
| `HORAS_MENSUALES` | `240` | Horas laborales mensuales |
| `QUINCENA_25_ACTIVA` | `true` | Activa el tipo de planilla Quincena 25 |
| `QUINCENA_25_TOPE_SALARIO` | `1500.00` | Tope salarial para Quincena 25 |

---

## 13. Módulo Parámetros de Empresa

### POST `/api/parametros-empresa`

**Request:**

```json
{
  "clave": "EMPRESA_ASUME_3_DIAS_INCAPACIDAD",
  "valor": "true",
  "tipo": "BOOLEAN",
  "descripcion": "La empresa paga los primeros 3 días de incapacidad común",
  "vigencia": "2026-01-01"
}
```

**Response 201:** parámetro creado/actualizado.

**Validaciones internas:**

- `clave` y `valor` obligatorios.
- `tipo` obligatorio: `BOOLEAN`, `DECIMAL`, `DATE`, `STRING`.
- Si `tipo = BOOLEAN`, valor debe ser `true` o `false`.
- Si `tipo = DECIMAL`, valor debe ser numérico.
- Si `tipo = DATE`, valor debe ser `YYYY-MM-DD`.
- `EMPRESA_PORCENTAJE_PAGO_3_DIAS` debe estar entre 0 y 100.
- `EMPRESA_FECHA_PAGO_AGUINALDO` debe estar entre el 20 de octubre y el 20 de diciembre del mismo año.

### GET `/api/parametros-empresa`

**Response 200:** array de parámetros.

### GET `/api/parametros-empresa/{clave}`

**Response 200:** parámetro por clave.

### Parámetros de empresa reconocidos

| Clave | Tipo | Descripción |
| --- | --- | --- |
| `EMPRESA_ASUME_3_DIAS_INCAPACIDAD` | `BOOLEAN` | ¿La empresa paga los primeros 3 días de incapacidad común? |
| `EMPRESA_PORCENTAJE_PAGO_3_DIAS` | `DECIMAL` | Porcentaje a pagar en esos 3 días (ej. `100.00`). |
| `EMPRESA_FECHA_PAGO_AGUINALDO` | `DATE` | Fecha de corte para cálculo proporcional del aguinaldo. |

---

## 14. Enums y valores válidos

### `TipoPlanilla`

- `QUINCENAL`
- `QUINCENA_25`

### `EstadoPlanilla`

- `BORRADOR`
- `CALCULADA`
- `APROBADA`

### `EstadoEmpleado`

- `ACTIVO`
- `INACTIVO`

### `EstadoTurno`

- `ACTIVO`
- `INACTIVO`

### `Genero`

- `MASCULINO`
- `FEMENINO`
- `OTRO`

### `TipoContrato`

- `TIEMPO_COMPLETO`
- `MEDIO_TIEMPO`
- `POR_HORA`
- `TEMPORAL`

### `SectorEmpleado`

- `COMERCIO_SERVICIOS`
- `INDUSTRIA`
- `MAQUILA_TEXTIL`
- `AGROPECUARIO`

### `DiaSemana`

- `LUNES`, `MARTES`, `MIERCOLES`, `JUEVES`, `VIERNES`, `SABADO`, `DOMINGO`

### `TipoAusencia`

- `INCAPACIDAD_COMUN`
- `INCAPACIDAD_ISSS_TOTAL`
- `PERMISO_CON_GOCE`
- `PERMISO_SIN_GOCE`
- `FALTA_INJUSTIFICADA`
- `AUSENCIA_POR_HORAS`

### `RolUsuario`

- `RRHH`
- `AUXILIAR`
- `GERENCIA`

### `TipoParametroEmpresa`

- `BOOLEAN`
- `DECIMAL`
- `DATE`
- `STRING`

---

## 15. Motor de cálculo — reglas del backend

### Planillas ordinarias (QUINCENAL)

1. **Valor hora** = `salarioBase / 30 / 8`.
2. **Salario proporcional** = `salarioBase * diasEfectivos / 30`.
   - `diasEfectivos = diasLaborados - diasDescontadosPorAusencia`.
3. **Pago parcial** por incapacidad común días 1–3 según `EMPRESA_PORCENTAJE_PAGO_3_DIAS`.
4. **Descuento por horas** = `valorHora * horasDescontadas`.
5. **Recargo día de descanso trabajado** = `salarioBase / 30 * 0.5 * diasDescansoTrabajados`.
6. **Descuento séptimo día** = `salarioBase / 30 * semanasConFaltaInjustificada`.
7. **Horas extra diurnas** = `valorHora * 2 * horasExtraDiurnas`.
8. **Horas extra nocturnas** = `valorHora * 2.25 * horasExtraNocturnas`.
9. **Salario bruto** = proporcional + pago parcial + recargos − descuentos + extras + comisiones + bonificaciones.
10. **ISSS** = `min(salarioBruto, 1000) * 3%`.
11. **AFP** = `salarioBruto * 7.25%`.
12. **Base gravada ISR** = `salarioBruto − ISSS − AFP`.
13. **ISR** según tabla de retención **quincenal** (aplica para todos los tipos de planilla):

| Tramo | Desde | Hasta | % | Sobre exceso de | Cuota fija |
| --- | --- | --- | --- | --- | --- |
| I | $0.01 | $275.00 | Sin retención | — | — |
| II | $275.01 | $447.62 | 10% | $275.00 | $8.83 |
| III | $447.63 | $1,019.05 | 20% | $447.62 | $30.00 |
| IV | $1,019.06 | En adelante | 30% | $1,019.05 | $144.28 |

Fórmula: `ISR = ((baseGravada - sobreExcesoDelTramo) * porcentaje) + cuotaFija`

Ejemplo: base gravada quincenal = $500.00 (Tramo III):  
`ISR = (($500.00 − $447.62) × 0.20) + $30.00 = $40.48`

14. **Salario neto** = `salarioBruto − ISSS − AFP − ISR − descuentosVoluntarios`.
15. **Aportes patronales**:
    - ISSS patronal = `min(salarioBruto, 1000) * 7.5%`.
    - AFP patronal = `salarioBruto * 8.75%`.

### Quincena 25

- Planilla anual independiente, período 15–25 de enero.
- Elegibles: empleados activos con `salarioBase <= QUINCENA_25_TOPE_SALARIO` (default $1,500).
- Beneficio base = `salarioBase * 50%`.
- Antigüedad >= 1 año al 25 de enero → 100% del beneficio.
- Antigüedad < 1 año → proporcional por días trabajados / 365.
- No aplica ISR, ISSS, AFP ni descuentos voluntarios.

### Aguinaldo (proyección)

- < 1 año: `(salarioBase / 30 * 15) * (diasTrabajados / 365)`.
- 1–<3 años: 15 días.
- 3–<10 años: 19 días.
- >= 10 años: 21 días.

### Vacaciones (proyección)

- `15 días de salario + 30% de prima`.

---

## 16. Validaciones internas por módulo

### Empleados

- DUI único.
- Correo único si se envía.
- Formato DUI `########-#`.
- Formato NIT `####-######-###-#`.
- Teléfono 7 u 8 dígitos numéricos.
- Salario mínimo sector Comercio y Servicios: `$408.80` (solo si no es borrador).
- Fecha de ingreso no futura.
- El turno asignado debe existir y estar activo.

### Usuarios

- Correo único.
- Rol válido.
- Contraseña mínimo 6 caracteres al cambiarla.
- La contraseña actual debe coincidir.

### Turnos

- Nombre no vacío.
- Al menos un día laborable.
- Hora de salida posterior a hora de entrada.
- Horas ordinarias diarias > 0.

### Ausencias

- Fecha inicio <= fecha fin.
- No solapamiento con otra ausencia del mismo empleado.
- El empleado debe estar activo.
- Para `AUSENCIA_POR_HORAS`, indicar `horas`.

### Planillas

- `periodoMes` formato `YYYY-MM`.
- Tipo válido.
- `numeroQuincena` solo para `QUINCENAL` (1 o 2).
- `QUINCENA_25` solo en enero y una por año.
- No se puede modificar una planilla `APROBADA`.
- No se puede calcular una planilla vacía.
- Solo se puede aprobar una planilla `CALCULADA`.
- Solo se pueden capturar detalles de empleados activos.

### Parámetros de empresa

- Tipo coherente con el valor.
- Rango 0–100 para porcentaje de pago de incapacidad.
- Fecha de pago de aguinaldo dentro de la ventana legal (20 oct – 20 dic).

---

> **Nota para frontend:** Swagger UI (`/swagger-ui`) refleja en tiempo real todos los endpoints y schemas. Esta guía es un complemento con las reglas de negocio y validaciones que Swagger no muestra explícitamente.

---

## 17. Datos de prueba (DataSeeder)

Al iniciar la aplicación con una base de datos vacía, `DataSeeder` inserta automáticamente un conjunto mínimo de datos de prueba para que el frontend pueda empezar a trabajar sin crear todo manualmente.

### Qué crea

- **Empresa:** *Supermercado La Cesta, S.A. de C.V.*
- **Parámetros legales:** tabla ISR quincenal, ISSS/AFP, Quincena 25, etc.
- **Turno:** *Turno Tienda Lunes-Sábado* (08:00–17:00).
- **8 empleados** activos con salarios variados ($408.80 – $1,200).
- **Horas extras nocturnas** en los detalles de algunos empleados (Ana 6 h, Pedro 4 h, Jorge 8 h).
- **2 ausencias** de prueba: Carlos con incapacidad común (3 días) y Sofía con falta injustificada (1 día).
- **1 planilla quincenal** de prueba (`2026-02`, quincena 1) en estado `BORRADOR` con un detalle por empleado.

### Usuario por defecto

El `InicializadorDatos` crea un usuario administrador:

- **Usuario:** `admin`
- **Correo:** `admin@lacesta.com`
- **Contraseña:** `Admin1234!`
- **Rol:** `RRHH`

### Activar / desactivar el seeder

La propiedad `app.seeder.enabled` controla si se ejecuta (por defecto `true`):

```properties
# application.properties o variable de entorno
app.seeder.enabled=true
```

En el perfil `test` el seeder está desactivado para no contaminar los tests.

### Primeros pasos recomendados

1. Iniciar sesión con `admin / Admin1234!`.
2. Listar empleados: `GET /api/empleados`.
3. Listar planillas: `GET /api/planillas`.
4. Calcular la planilla de prueba: `POST /api/planillas/{id}/calcular`.
5. Revisar el dashboard: `GET /api/dashboard`.
