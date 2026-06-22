# Requerimientos del Sistema — Gestión de Planillas La Cesta

> Especificación de requerimientos para la **aplicación de gestión automatizada de planillas y prestaciones sociales** de Supermercado La Cesta, S.A. de C.V. Define qué debe hacer el sistema, sus reglas de negocio y los parámetros legales que debe respetar.
>
> **Prioridades:** **Alta** (indispensable), **Media** (importante), **Baja** (deseable).

## 1. Objetivo del sistema

Automatizar el registro de empleados y el cálculo de la planilla quincenal de Supermercado La Cesta (55 empleados, turnos rotativos con fines de semana y asuetos), garantizando el cálculo exacto de ingresos, deducciones de ley del trabajador (ISSS, AFP, ISR), aportes patronales (ISSS, AFP), prestaciones (vacaciones, aguinaldo) y el control de ausencias e incapacidades, con cumplimiento de la normativa salvadoreña vigente.

## 2. Actores y roles

| Actor | Descripción | Acciones principales |
| --- | --- | --- |
| Administrador de RR. HH. | Usuario principal del sistema | Registrar empleados, calcular y aprobar planillas, exportar reportes |
| Auxiliar de RR. HH. | Apoyo operativo | Registrar asistencia, horas extras, incapacidades; consultar empleados |
| Gerencia | Consulta | Visualizar indicadores y totales de planilla |

## 3. Requerimientos funcionales

### 3.1. Módulo Inicio / Tablero

| ID | Requerimiento | Prioridad |
| --- | --- | --- |
| RF-01 | Mostrar un panel principal con el total de empleados activos. | Alta |
| RF-02 | Ofrecer accesos rápidos a Control de Empleados, Agregar Empleado y Gestionar Planilla. | Media |
| RF-03 | Mostrar indicadores generales del último período procesado (total bruto, total neto, total deducciones). | Media |
| RF-42 | Presentar una interfaz con el logo de Supermercado La Cesta y un menú de navegación hacia todos los módulos del sistema. | Alta |

### 3.2. Módulo Control de Empleados

| ID | Requerimiento | Prioridad |
| --- | --- | --- |
| RF-04 | Listar todos los empleados con ID, nombre, departamento, puesto y estado (activo/inactivo). | Alta |
| RF-05 | Mostrar contadores de empleados totales, activos e inactivos. | Media |
| RF-06 | Permitir búsqueda por nombre, ID o departamento. | Alta |
| RF-07 | Permitir filtrar por departamento y estado laboral. | Media |
| RF-08 | Permitir ver el detalle y editar el registro de cada empleado. | Alta |
| RF-09 | Permitir activar/inactivar empleados sin eliminar su historial. | Alta |

### 3.3. Módulo Registro de Empleado

| ID | Requerimiento | Prioridad |
| --- | --- | --- |
| RF-10 | Capturar datos personales: nombre, apellido, DUI, NIT, correo, teléfono, fecha de nacimiento, género. | Alta |
| RF-11 | Capturar dirección completa, municipio y departamento. | Media |
| RF-12 | Capturar datos laborales: cargo, departamento (Ventas/Cajas, Bodega/Inventario, Administración/RR. HH., Limpieza y mantenimiento), fecha de ingreso, tipo de contrato (indefinido, temporal/plazo fijo, servicios profesionales) y salario base. | Alta |
| RF-13 | Capturar datos de seguridad social: AFP y número de ISSS. | Alta |
| RF-14 | Capturar contacto de emergencia (nombre y teléfono). | Baja |
| RF-15 | Validar formato de campos clave (DUI, NIT, correo, salario numérico) antes de guardar. | Alta |
| RF-43 | Validar que el salario de un empleado de tiempo completo no sea menor al salario mínimo del sector Comercio y Servicios ($408.80); mostrar advertencia o bloquear el registro. | Alta |
| RF-16 | Permitir guardar como borrador o registrar definitivamente. | Media |

### 3.4. Módulo Gestionar Planilla

| ID | Requerimiento | Prioridad |
| --- | --- | --- |
| RF-17 | Seleccionar el período de pago quincenal (1.ª quincena 1–15 y 2.ª quincena 16–fin de mes). El sistema no genera planilla mensual. | Alta |
| RF-18 | Capturar por empleado: días laborados, horas extras diurnas y nocturnas, comisiones, bonificaciones, incapacidades y descuentos voluntarios. | Alta |
| RF-19 | Calcular automáticamente salario bruto, deducciones (ISSS, AFP, ISR) y salario neto por empleado. | Alta |
| RF-20 | Mostrar totales generales del período (bruto, ISSS, AFP, ISR, neto, aportes patronales y costo total para la empresa). | Alta |
| RF-21 | Exportar la planilla en formato PDF. | Media |
| RF-22 | Ejecutar un flujo de aprobación del período (calcular → revisar → aprobar/procesar). | Media |
| RF-23 | Guardar el histórico de planillas procesadas para consulta posterior. | Media |
| RF-34 | Generar e imprimir la boleta/comprobante de pago individual por empleado, con desglose de ingresos, deducciones del trabajador y aportes patronales. | Alta |

### 3.5. Motor de cálculo (reglas de ley)

| ID | Requerimiento | Prioridad |
| --- | --- | --- |
| RF-24 | Calcular horas extra: diurnas con recargo del 100% (doble) y nocturnas con recargo del 125% (2.25×). | Alta |
| RF-25 | Aplicar recargo por día de asueto/séptimo día trabajado (100% adicional + descanso compensatorio). | Alta |
| RF-26 | Descontar ISSS: 3% del trabajador sobre base máxima de $1,000 (máx. $30). | Alta |
| RF-27 | Descontar AFP: 7.25% del trabajador sobre el salario real (sin tope máximo cotizable). | Alta |
| RF-35 | Calcular el aporte patronal de ISSS: 7.5% sobre base máxima de $1,000 (máx. $75 por empleado). | Alta |
| RF-36 | Calcular el aporte patronal de AFP: 8.75% sobre el salario real (sin tope máximo cotizable). | Alta |
| RF-44 | Integrar a la base de cotización de ISSS, AFP e ISR las bonificaciones y comisiones habituales y constantes (Art. 142 CT); las bonificaciones ocasionales (p. ej. bono navideño) gravan solo ISR. | Alta |
| RF-28 | Calcular base gravada = salario bruto − ISSS − AFP. | Alta |
| RF-29 | Aplicar tabla de ISR por tramos (exento hasta $550; cuotas $17.67 / $60.00 / $288.57). | Alta |
| RF-30 | Calcular vacaciones: 15 días de salario + prima del 30% (Art. 177 CT). | Alta |
| RF-31 | Calcular aguinaldo según antigüedad (15/19/21 días), exento de ISR según límite legal. Para quien no cumple un año de servicio, calcular proporcional: (salario diario × 15 / 365) × días trabajados, tomando como corte la fecha de pago seleccionada. Permitir elegir la fecha de pago dentro de la ventana legal del 20 oct al 20 dic (no fija en código). | Alta |
| RF-32 | Soportar la Quincena 25 (50% del salario) como concepto configurable, aplicando de forma estricta el tope de $1,500: hasta ese monto el pago se mantiene exento de ISR, ISSS y AFP. El motor de cálculo debe garantizar que no se aplique ninguna deducción a este concepto dentro del tope. | Media |
| RF-33 | Permitir parametrizar tasas, topes y tablas para futuras reformas legales sin reprogramar. | Alta |

### 3.6. Módulo de Ausencias e Incapacidades

| ID | Requerimiento | Prioridad |
| --- | --- | --- |
| RF-37 | Registrar ausencias/incapacidades por empleado mediante un tipo seleccionable (ver tabla de tipos), con fechas de inicio y fin y número de días u horas. | Alta |
| RF-38 | Adjuntar o referenciar el documento de respaldo (constancia médica, incapacidad ISSS) cuando aplique. | Media |
| RF-39 | Aplicar automáticamente la regla matemática de cada tipo sobre la planilla (pago/descuento de días y manejo del séptimo día), según la tabla de tipos. | Alta |
| RF-40 | Para Incapacidad Común, respetar el parámetro configurable "La empresa asume los primeros 3 días": si está activo, pagar los días 1–3 (al % configurado) y descontar del 4.º en adelante; si está inactivo, descontar todos los días (el trabajador solo recibe el subsidio del ISSS desde el 4.º día). | Alta |
| RF-45 | Para Incapacidad ISSS Total (maternidad y riesgo profesional), registrar la licencia sin generar pago de planilla y reportar los días a ISSS/AFP para no interrumpir el tiempo de servicio. El sistema no calcula complemento del 25% ni replica el salario medio base del ISSS. | Alta |
| RF-41 | Mostrar el historial de ausencias e incapacidades por empleado y consolidado por período. | Media |

**Tipos de ausencia/incapacidad y su regla automática:**

| Opción (dropdown) | Situaciones que agrupa | Regla matemática automática |
| --- | --- | --- |
| Incapacidad Común | Enfermedad o accidente común | Días 1–3 según parámetro de empresa; del día 4 en adelante descuenta (paga el ISSS) |
| Incapacidad ISSS Total | Maternidad y riesgo profesional (accidente de trabajo) | Descuenta todos los días en planilla (el ISSS paga el 100% desde el primer día) |
| Permiso con Goce de Sueldo | Paternidad, duelo, matrimonio, enfermedad grave de familiares | Paga el salario normal. No descuenta el séptimo día |
| Permiso sin Goce de Sueldo | Permisos personales, emergencias no cubiertas por ley | Descuenta los días ausentes. No descuenta el séptimo día (hubo aviso) |
| Falta Injustificada | Faltas sin avisar, ausencias no autorizadas, suspensiones | Descuenta el día ausente y descuenta el séptimo día |
| Ausencia por Horas | Citas médicas ISSS, llegadas tardías, lactancia | Descuenta solo las horas/minutos exactos (o paga si es cita ISSS/lactancia). No afecta el séptimo día |

## 4. Parámetros legales (configurables, vigentes 2026)

| Concepto | Valor | Base legal |
| --- | --- | --- |
| ISSS trabajador | 3% (tope $1,000 → máx. $30) | Ley del Seguro Social |
| AFP trabajador | 7.25% (sin tope) | Ley Integral del Sistema de Pensiones, Art. 14 |
| ISSS patronal | 7.5% (tope $1,000 → máx. $75) | Ley del Seguro Social |
| AFP patronal | 8.75% (sin tope) | Ley Integral del Sistema de Pensiones, Art. 14 |
| ISR exento | Hasta $550 mensual | Decreto Ejecutivo N.º 10 (2025) |
| Hora extra diurna / nocturna | +100% / +125% | Código de Trabajo, Arts. 161 y ss. |
| Aguinaldo | 15 / 19 / 21 días | Código de Trabajo, Arts. 196–200 |
| Vacaciones | 15 días + 30% prima | Código de Trabajo, Art. 177 |
| Salario mínimo (Comercio y Servicios) | $408.80 mensual | Decreto de salario mínimo vigente |

### 4.1. Parámetros configurables por empresa

| Parámetro | Descripción |
| --- | --- |
| Asume primeros 3 días de incapacidad común | Toggle on/off. Define si la empresa paga los días 1–3 de la Incapacidad Común, según el Reglamento Interno de cada empresa. |
| Porcentaje de pago de los primeros 3 días | % aplicable cuando el toggle anterior está activo (p. ej. 75% o 100%). |
| Fecha de pago del aguinaldo | Fecha seleccionable dentro de la ventana legal (20 oct – 20 dic); define el corte de días trabajados para el cálculo proporcional. |

## 5. Requerimientos no funcionales

| ID | Requerimiento | Categoría |
| --- | --- | --- |
| RNF-01 | Los cálculos deben ser exactos al centavo y consistentes en todo el sistema. | Confiabilidad |
| RNF-02 | La interfaz debe ser sencilla para usuarios administrativos sin perfil técnico. | Usabilidad |
| RNF-03 | El acceso debe requerir autenticación de usuario. | Seguridad |
| RNF-04 | Los datos personales de empleados deben almacenarse de forma protegida. | Seguridad |
| RNF-05 | El cálculo de una planilla de 55 empleados debe completarse en pocos segundos. | Rendimiento |
| RNF-06 | Los parámetros legales deben poder actualizarse sin modificar el código fuente. | Mantenibilidad |

## 6. Restricciones y supuestos

- Solución de **uso interno** específica para La Cesta; no contempla implementación genérica en otras empresas.
- **No** integra con sistemas externos (ISSS, AFP, Ministerio de Hacienda); las declaraciones se siguen presentando por separado.
- La exactitud depende de la correcta entrada de datos (asistencia, horas extra, etc.).
- El sistema requiere actualización manual de parámetros ante cambios de ley.

## 7. Fuera de alcance

- Evaluación de desempeño, reclutamiento, control biométrico y analítica de RR. HH.
- Respaldo automatizado en la nube y acceso concurrente a gran escala.
- Integración directa con bancos para dispersión de pagos.

---

> **Pendientes a reflejar en el motor de cálculo:** precisar el **recargo por día de asueto** (RF-25) y eliminar el tope de AFP también en el diagrama de flujo del software.
>
> **Alcance de salario mínimo:** el sistema valida únicamente el sector **Comercio y Servicios ($408.80)**; no se integran otros sectores (agropecuario o maquila) por tratarse de una solución de uso interno exclusiva para La Cesta.
