# FDK Manager — criterios de entrega
No marcar una función como terminada sin evidencia de prueba.
- [ ] Android: servicio de ubicación foreground, permiso explícito, notificación persistente, inicio y parada vinculados a jornada.
- [ ] iOS: propietario PWA instalada; conductor nativo con ubicación de fondo y firma/distribución autorizadas.
- [ ] GPS: prueba de pantalla bloqueada, pérdida de red, recuperación, cierre de jornada, ausencia de rastreo fuera de jornada.
- [ ] Cola local persistente, deduplicación y sincronización autorizada de posiciones históricas.
- [ ] Mantención: odómetro físico, aviso único por ciclo de 10000 km, reinicio tras mantención.
- [ ] Notificaciones externas Android/iOS.
- [ ] Gastos fijos, facturación, cobranza, conciliación sin duplicar reembolsos.
- [ ] Informes XLSX/PDF, cierre semanal con auditoría y reapertura autorizada.
- [ ] Pruebas RLS propietario/conductor, protección de credenciales.
- [ ] APK compilado y probado; instalación iPhone probada.
- [ ] Prueba integral con dos teléfonos.
- [ ] TAG: pórticos y tarifas verificadas, estimación identificada, desconocidos pendientes.
Sin nómina, sin comprobantes de entrega obligatorios, sin rastreo fuera de jornada. No contratar servicios pagados sin autorización.

## Prioridad acordada
GPS avanzado, Traccar y TAG automático quedan aplazados hasta terminar los demás módulos; conservar GPS existente y peajes manuales. Priorizar navegación y estabilidad, jornadas y descansos, agenda y rutas, combustible/gastos/reembolsos, facturación/cobranza, rentabilidad por camión, mantenciones cada 10000 km y vencimientos, informes Excel/PDF y cierre semanal, cotizaciones, respaldos, notificaciones y pruebas integrales Android/iOS. No marcar entregado sin evidencia. Revisión 31.1: navegación atrás incorporada, compilación y prueba física pendientes.

## Alcance GPS acordado 26/11/2026
No se exige prueba de desconexión voluntaria de internet. Mantener cola persistente y sincronización automática tras pérdidas naturales de cobertura (túneles y zonas sin señal). Verificar ubicación en panel, pantalla bloqueada durante jornada, historial y kilómetros, y cese al cerrar jornada. Si no hay señal GPS, identificar discontinuidades sin inventar posiciones ni distancias. No marcar pruebas como aprobadas sin evidencia.

## Evidencia de base de datos — 26/11/2026
Verificación de catálogo PostgreSQL en Supabase existente: `expense_reimbursements` tiene PRIMARY KEY (`expense_id`), FK a `operating_expenses(id)` y CHECK (`amount_clp > 0`). La clave primaria impide dos registros para el mismo gasto incluso ante solicitudes concurrentes. RLS habilitada en las 22 tablas públicas consultadas; esto no acredita la corrección de las políticas. Pendiente prueba funcional de reintento, conciliación, autorización por rol y monto correcto. No se insertaron datos de prueba ni se modificó el esquema.
