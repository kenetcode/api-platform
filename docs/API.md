# API Planillas — Supermercado La Cesta, S.A. de C.V.

> Contrato de integración entre la API REST y el frontend.
> Base URL: `http://localhost:8080`
> Autenticación: header `X-API-Key: <clave>`
> Swagger UI: `http://localhost:8080/swagger-ui`

---

## Índice

1. [Autenticación y roles](#autenticación-y-roles)
2. [Formato de errores](#formato-de-errores)
3. [Usuarios](#usuarios)
4. [Empresa y Dashboard](#empresa-y-dashboard)
5. [Empleados](#empleados)
6. [Ausencias e Incapacidades](#ausencias-e-incapacidades)
7. [Planillas y Motor de Cálculo](#planillas-y-motor-de-cálculo)
8. [Boletas](#boletas)
9. [Parámetros Legales](#parámetros-legales)
10. [Tabla de parámetros legales](#tabla-de-parámetros-legales)
11. [Ejemplo de boleta JSON](#ejemplo-de-boleta-json)

---

## Autenticación y roles

Toda solicitud (excepto `/swagger-ui`, `/api-docs`, `/actuator/health`) debe incluir:

```
X-API-Key: tu-api-key-aqui
```

| Rol | Permisos |
|-----|----------|
| `RRHH` | Todo: empleados, planillas, usuarios, parámetros |
| `AUXILIAR` | Registrar ausencias/incapacidades, capturar detalles de planilla, consultar empleados |
| `GERENCIA` | Solo consulta (indicadores, listados, planillas aprobadas) |

**Respuestas de seguridad:**
- `401` → falta o es inválida la `X-API-Key`
- `403` → el rol no autoriza la acción

---

## Formato de errores

Todas las respuestas de error usan este formato:

```json
{
  "codigo": "VALIDACION",
  "mensaje": "La solicitud contiene errores de validación.",
  "detalles": [
    { "campo": "dui", "error": "El DUI debe tener el formato ########-#" },
    { "campo": "salarioBase", "error": "El salario no puede ser menor al mínimo del sector" }
  ]
}
```

| Código HTTP | `codigo` | Descripción |
|-------------|----------|-------------|
| 400 | `VALIDACION` | Errores de validación Bean (campos inválidos) |
| 401 | `NO_AUTENTICADO` | Falta o es inválida la X-API-Key |
| 403 | `ACCESO_DENEGADO` | El rol no tiene permiso |
| 404 | `NO_ENCONTRADO` | Recurso no existe |
| 409 | `CONFLICTO` | DUI duplicado, solapamiento de fechas, etc. |
| 422 | `NEGOCIO` | Regla de negocio violada (salario < mínimo, planilla ya aprobada, etc.) |
| 500 | `ERROR_INTERNO` | Error inesperado del servidor |

---

## Usuarios

> Todos los endpoints de usuarios requieren rol **RRHH**.

### POST /api/usuarios — Crear usuario

La API key se devuelve **una sola vez** en texto plano al crear. Guárdala de inmediato.

**Request:**
```json
{
  "nombre": "María García",
  "correo": "maria.garcia@lacesta.com.sv",
  "rol": "AUXILIAR"
}
```
Valores de `rol`: `RRHH`, `AUXILIAR`, `GERENCIA`

**Response 201:**
```json
{
  "id": 1,
  "nombre": "María García",
  "correo": "maria.garcia@lacesta.com.sv",
  "rol": "AUXILIAR",
  "activo": true,
  "creadoEn": "2026-01-15T09:00:00",
  "apiKeyPlana": "a3f8c2d1e9b4...f7a2c1"
}
```

### GET /api/usuarios — Listar usuarios

**Response 200:**
```json
[
  {
    "id": 1,
    "nombre": "María García",
    "correo": "maria.garcia@lacesta.com.sv",
    "rol": "AUXILIAR",
    "activo": true,
    "creadoEn": "2026-01-15T09:00:00"
  }
]
```
> `apiKeyPlana` **no** aparece en el listado.

### PATCH /api/usuarios/{id}/activar — Activar usuario
### PATCH /api/usuarios/{id}/inactivar — Inactivar usuario

**Response 200:** igual que el objeto de usuario sin `apiKeyPlana`.

### POST /api/usuarios/{id}/regenerar-api-key — Regenerar API key

Invalida la key anterior. La nueva se devuelve **una sola vez**.

**Response 200:** objeto usuario con `apiKeyPlana` incluida.

---

## Empresa y Dashboard

### GET /api/empresa — Datos de empresa y menú

> Rol mínimo: cualquier autenticado

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

### GET /api/dashboard — Indicadores del tablero

**Response 200:**
```json
{
  "totalEmpleadosActivos": 48,
  "totalEmpleadosInactivos": 7,
  "ultimoPeriodo": null,
  "totalBrutoUltimoPeriodo": null,
  "totalNetoUltimoPeriodo": null,
  "totalDeduccionesUltimoPeriodo": null
}
```

---

## Empleados

### POST /api/empleados — Crear empleado
> Rol: **RRHH**

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
  "esBorrador": false
}
```

Valores de `genero`: `MASCULINO`, `FEMENINO`, `OTRO`
Valores de `tipoContrato`: `TIEMPO_COMPLETO`, `MEDIO_TIEMPO`, `POR_HORA`, `TEMPORAL`
Valores de `sector`: `COMERCIO_SERVICIOS`, `INDUSTRIA`, `MAQUILA_TEXTIL`, `AGROPECUARIO`

**Response 201:** objeto empleado completo con `id` y `creadoEn`.

**Errores posibles:**
- `409` si el DUI ya existe
- `422` si el salario es menor al mínimo del sector

### GET /api/empleados — Listar empleados
> Rol: **RRHH, AUXILIAR, GERENCIA**

**Query params:**
- `q` — búsqueda por nombre, apellido, ID o departamento
- `estado` — `ACTIVO` | `INACTIVO`
- `departamento` — nombre exacto del departamento

**Response 200:** array de empleados.

### GET /api/empleados/{id} — Detalle de empleado

**Response 200:** objeto empleado completo.

### PUT /api/empleados/{id} — Actualizar empleado
> Rol: **RRHH**

Mismo body que creación. **Response 200:** empleado actualizado.

### PATCH /api/empleados/{id}/estado — Cambiar estado
> Rol: **RRHH**

**Request:**
```json
{ "estado": "INACTIVO" }
```
**Response 200:** empleado con estado actualizado.

### GET /api/empleados/contadores — Totales
> Rol: **RRHH, GERENCIA**

**Response 200:**
```json
{ "total": 55, "activos": 48, "inactivos": 7 }
```

---

## Ausencias e Incapacidades

### POST /api/empleados/{id}/ausencias — Registrar ausencia
> Rol: **RRHH, AUXILIAR**

**Request:**
```json
{
  "tipo": "FALTA",
  "fechaInicio": "2026-02-10",
  "fechaFin": "2026-02-10",
  "justificada": false,
  "observacion": "Sin aviso previo"
}
```
Valores de `tipo`: `FALTA`, `PERMISO`

**Response 201:**
```json
{
  "id": 1,
  "empleadoId": 3,
  "nombreEmpleado": "Carlos Martínez López",
  "tipo": "FALTA",
  "fechaInicio": "2026-02-10",
  "fechaFin": "2026-02-10",
  "justificada": false,
  "observacion": "Sin aviso previo"
}
```

### GET /api/empleados/{id}/ausencias — Listar ausencias del empleado
### PUT /api/empleados/{id}/ausencias/{ausenciaId} — Actualizar ausencia
### DELETE /api/empleados/{id}/ausencias/{ausenciaId} — Eliminar ausencia (204 sin body)

### GET /api/ausencias?periodo=YYYY-MM — Ausencias del período

**Response 200:** array de ausencias del mes especificado.

### POST /api/empleados/{id}/incapacidades — Registrar incapacidad

**Request:**
```json
{
  "tipo": "ENFERMEDAD",
  "fechaInicio": "2026-02-15",
  "fechaFin": "2026-02-20",
  "dias": 6,
  "documentoUrl": "https://..."
}
```
Valores de `tipo`: `ENFERMEDAD`, `MATERNIDAD`, `RIESGO_PROFESIONAL`

**Response 201:** objeto incapacidad con empleadoId y nombreEmpleado.

### GET /api/empleados/{id}/incapacidades
### PUT /api/empleados/{id}/incapacidades/{incId}
### DELETE /api/empleados/{id}/incapacidades/{incId} — 204 sin body

---

## Planillas y Motor de Cálculo

### POST /api/planillas — Crear período de planilla
> Rol: **RRHH**

**Request:**
```json
{
  "periodoMes": "2026-02",
  "tipo": "MENSUAL"
}
```
Valores de `tipo`: `QUINCENAL`, `MENSUAL`

**Response 201:**
```json
{
  "id": 1,
  "periodoMes": "2026-02",
  "tipo": "MENSUAL",
  "estado": "BORRADOR",
  "creadoEn": "2026-02-01T08:00:00"
}
```

### POST /api/planillas/{id}/detalles — Capturar datos de empleado
> Rol: **RRHH, AUXILIAR**

**Request:**
```json
{
  "empleadoId": 3,
  "diasLaborados": 28,
  "horasExtraDiurnas": 4.0,
  "horasExtraNocturnas": 0,
  "comisiones": 0,
  "bonificaciones": 50.00,
  "descuentosVoluntarios": 0
}
```

**Response 200:** objeto DetallePlanilla con los datos capturados (antes del cálculo, los campos de resultado son null).

### POST /api/planillas/{id}/calcular — Ejecutar motor de cálculo
> Rol: **RRHH**

No requiere body. Calcula ISSS, AFP, ISR y neto para todos los detalles capturados.

**Response 200:** planilla en estado `CALCULADA` con todos los detalles y totales calculados.

### GET /api/planillas/{id} — Obtener planilla completa
> Rol: **RRHH, GERENCIA**

**Response 200:**
```json
{
  "id": 1,
  "periodoMes": "2026-02",
  "tipo": "MENSUAL",
  "estado": "CALCULADA",
  "totalBruto": 22484.40,
  "totalIsss": 674.53,
  "totalAfp": 1630.12,
  "totalIsr": 425.80,
  "totalNeto": 19753.95,
  "creadoEn": "2026-02-01T08:00:00",
  "detalles": [...]
}
```

### POST /api/planillas/{id}/aprobar — Aprobar planilla
> Rol: **RRHH**. La planilla debe estar en estado `CALCULADA`.

**Response 200:** planilla en estado `APROBADA` con `aprobadoEn`.

### GET /api/planillas — Histórico de planillas
> Rol: **RRHH, GERENCIA**

**Response 200:** array de planillas ordenadas por fecha descendente.

### GET /api/empleados/{id}/prestaciones?tipo=vacaciones — Proyección de prestaciones
> Rol: **RRHH, GERENCIA**

Valores de `tipo`: `vacaciones`, `aguinaldo`

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

---

## Boletas

### GET /api/planillas/{planillaId}/empleados/{empleadoId}/boleta — Boleta JSON
> Rol: **RRHH, GERENCIA**

Ver sección [Ejemplo de boleta JSON](#ejemplo-de-boleta-json).

### GET /api/planillas/{planillaId}/empleados/{empleadoId}/boleta/pdf — Boleta PDF
> Rol: **RRHH, GERENCIA**

**Response:** `Content-Type: application/pdf` — archivo descargable `boleta_{planillaId}_{empleadoId}.pdf`

### GET /api/planillas/{planillaId}/exportar/pdf — Planilla completa en PDF
> Rol: **RRHH, GERENCIA**

**Response:** `Content-Type: application/pdf` — archivo descargable `planilla_{planillaId}.pdf`

---

## Parámetros Legales

### GET /api/parametros-legales — Listar parámetros
> Rol: **RRHH, GERENCIA**

**Response 200:**
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

---

## Tabla de parámetros legales

| Clave | Valor 2026 | Descripción |
|-------|-----------|-------------|
| `ISSS_TRABAJADOR_PORC` | `0.03` | ISSS trabajador 3% |
| `ISSS_BASE_MAXIMA` | `1000.00` | Base máxima ISSS ($1,000) |
| `ISSS_PATRONO_PORC` | `0.075` | ISSS patrono 7.5% |
| `AFP_TRABAJADOR_PORC` | `0.0725` | AFP trabajador 7.25% |
| `AFP_PATRONO_PORC` | `0.0875` | AFP patrono 8.75% |
| `ISR_TRAMO1_TOPE` | `550.00` | ISR exento hasta $550.00 |
| `ISR_TRAMO2_INICIO` | `550.01` | Tramo II inicio |
| `ISR_TRAMO2_FIN` | `895.24` | Tramo II fin |
| `ISR_TRAMO2_PORC` | `0.10` | Tramo II tasa 10% |
| `ISR_TRAMO2_CUOTA` | `17.67` | Tramo II cuota fija |
| `ISR_TRAMO3_INICIO` | `895.25` | Tramo III inicio |
| `ISR_TRAMO3_FIN` | `2038.10` | Tramo III fin |
| `ISR_TRAMO3_PORC` | `0.20` | Tramo III tasa 20% |
| `ISR_TRAMO3_CUOTA` | `60.00` | Tramo III cuota fija |
| `ISR_TRAMO4_INICIO` | `2038.11` | Tramo IV inicio |
| `ISR_TRAMO4_PORC` | `0.30` | Tramo IV tasa 30% |
| `ISR_TRAMO4_CUOTA` | `288.57` | Tramo IV cuota fija |
| `HORAS_MENSUALES` | `240` | Horas laborales estándar/mes |

> Los valores se toman de la tabla `parametros_legales`. Si la tabla está vacía, la API usa estos valores como fallback hardcoded.

---

## Ejemplo de boleta JSON

```json
{
  "empresa": "Supermercado La Cesta, S.A. de C.V.",
  "periodoMes": "2026-02",
  "tipoPlanilla": "MENSUAL",
  "fechaGeneracion": "2026-02-28T16:30:00",
  "empleadoId": 3,
  "nombreCompleto": "Carlos Martínez López",
  "dui": "12345678-9",
  "cargo": "Cajero",
  "departamento": "Ventas",
  "afp": "CRECER",
  "diasLaborados": 28,
  "salarioBase": 450.00,
  "salarioProporcional": 420.00,
  "horasExtraDiurnas": 0.00,
  "horasExtraNocturnas": 0.00,
  "comisiones": 0.00,
  "bonificaciones": 50.00,
  "totalPercepciones": 470.00,
  "isss": 14.10,
  "afpMonto": 34.08,
  "isr": 0.00,
  "descuentosVoluntarios": 0.00,
  "totalDeducciones": 48.18,
  "salarioNeto": 421.82,
  "aportePatronalIsss": 35.25,
  "aportePatronalAfp": 41.13,
  "totalAportePatronal": 76.38
}
```

**Nota:** ISR = $0 porque la base gravada ($470 - $14.10 - $34.08 = $421.82) es menor al tramo exento ($550.00).
