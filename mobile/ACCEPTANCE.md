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
TAG automático queda para la última etapa; conservar peajes manuales y código existente. Priorizar navegación y estabilidad, GPS nativo y sincronización, jornadas y colaciones, agenda y rutas, combustible/gastos/reembolsos, facturación/cobranza, rentabilidad, mantenciones/vencimientos, informes Excel/PDF, cotizaciones/respaldos, notificaciones y pruebas integrales Android/iOS. No marcar entregado sin evidencia. Revisión 31.1: navegación atrás incorporada, compilación y prueba física pendientes.

## Alcance GPS acordado 26/11/2026
No se exige prueba de desconexión voluntaria de internet. Mantener cola persistente y sincronización automática tras pérdidas naturales de cobertura (túneles y zonas sin señal). Verificar ubicación en panel, pantalla bloqueada durante jornada, historial y kilómetros, y cese al cerrar jornada. Si no hay señal GPS, identificar discontinuidades sin inventar posiciones ni distancias. No marcar pruebas como aprobadas sin evidencia.
