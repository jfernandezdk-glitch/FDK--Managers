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
GPS avanzado, Traccar y TAG automático quedan aplazados hasta terminar los demás módulos; conservar GPS existente y peajes manuales. Priorizar navegación y estabilidad, inicio y cierre operativo de jornada (sin control horario ni descansos), agenda y rutas, combustible/gastos/reembolsos, facturación/cobranza, rentabilidad por camión, mantenciones cada 10000 km y vencimientos, informes Excel/PDF y cierre semanal, cotizaciones, respaldos, notificaciones y pruebas integrales Android/iOS. No marcar entregado sin evidencia. Revisión 31.1: navegación atrás incorporada, compilación y prueba física pendientes.

## Alcance GPS acordado 26/11/2026
No se exige prueba de desconexión voluntaria de internet. Mantener cola persistente y sincronización automática tras pérdidas naturales de cobertura (túneles y zonas sin señal). Verificar ubicación en panel, pantalla bloqueada durante jornada, historial y kilómetros, y cese al cerrar jornada. Si no hay señal GPS, identificar discontinuidades sin inventar posiciones ni distancias. No marcar pruebas como aprobadas sin evidencia.

## Evidencia de base de datos — 26/11/2026
Verificación de catálogo PostgreSQL en Supabase existente: `expense_reimbursements` tiene PRIMARY KEY (`expense_id`), FK a `operating_expenses(id)` y CHECK (`amount_clp > 0`). La clave primaria impide dos registros para el mismo gasto incluso ante solicitudes concurrentes. RLS habilitada en las 22 tablas públicas consultadas; esto no acredita la corrección de las políticas. Pendiente prueba funcional de reintento, conciliación, autorización por rol y monto correcto. No se insertaron datos de prueba ni se modificó el esquema.

## Alcance de entrega actualizado
Excluir control de horas trabajadas, descansos, comidas y nómina. Conservar inicio y cierre operativo, odómetros y GPS durante jornada. Completar módulos y comprobaciones técnicas antes de solicitar pruebas físicas de la APK. No confundir implementación con aceptación verificada.

## Agenda y ORS — 26/11/2026
- [ ] Verificar orden manual y notificaciones de cambios de agenda por rol.
- [ ] Optimización ORS: vista previa, confirmación explícita, respeto de restricciones de camión y orden de retiro/entrega; conservar orden manual ante error.
- [ ] Verificar conexión autenticada, cuotas de la cuenta Standard, atribución CC BY-SA 4.0 y condiciones de privacidad antes de transmitir ubicaciones reales. El proveedor confirma uso comercial Standard: https://ask.openrouteservice.org/t/free-plan-in-an-app-with-a-paid-tier-license-cc-by-sa-4-0-personal-data-and-attribution-questions/8079/ . El dominio anterior fue retirado el 24/11/2026; rutas vigentes: https://api.heigit.org/openrouteservice/v2/directions/driving-hgv y optimización https://api.heigit.org/vroom/v0 (https://ask.openrouteservice.org/t/deprecating-api-openrouteservice-org-in-favour-of-api-heigit-org/7912/). Función desplegada v1 solo admite test y exige JWT y rol propietario según código; no se verificó llamada real. Control experimental retirado del panel hasta validación. No exponer clave ni contratar servicios.
- [ ] Compilar APK y completar comprobaciones técnicas antes de solicitar pruebas físicas integrales; no solicitar pruebas parciales.
