# Documentación académica — Ecosistema CMAC Ica

Paquete de entrega **Criterio 5** (calidad de datos, arquitectura y documentación).

## Índice

| Documento | Contenido |
|-----------|-----------|
| [`arquitectura.md`](arquitectura.md) | Capas por pieza, componentes, flujo sync |
| [`HU-RF-TRAZABILIDAD.md`](HU-RF-TRAZABILIDAD.md) | Historias de usuario, RF trazados, matrices |
| [`diagramas/uml.md`](diagramas/uml.md) | UML Mermaid: clases, secuencia, estados, componentes |
| [`DDL/001_bd_core_mobile.sql`](DDL/001_bd_core_mobile.sql) | Modelo lógico SQL (`cr_*`, `sync_outbox`, `sync_log`) |
| [`DDL/002_seed_demo.sql`](DDL/002_seed_demo.sql) | Datos demo SQL |
| [`seed/firestore_demo.json`](seed/firestore_demo.json) | Referencia seed Firestore |

## Proyectos del ecosistema

| Proyecto | Ruta | Stack |
|----------|------|-------|
| App Clientes | `CajaIcaHomeBanking` | Kotlin Compose MVVM |
| App Ventas | `CajaIcaVentas` | Kotlin Compose MVVM + Room |
| Portal Comité | `CajaIcaComite` | React + Vite |
| Cloud Functions | `CajaIcaHomeBanking/functions` | Node.js |

## Mapeo SQL ↔ Firestore ↔ Room

```
Firestore solicitudes_credito  →  cr_solicitud_credito
Firestore cartera_dia/items    →  cr_cartera_dia / cartera_cache (Room)
Room sync_queue                →  sync_outbox (SQL lógico)
Room sync_log                  →  sync_log (SQL lógico)
```

## Deploy backend

```bash
cd CajaIcaHomeBanking/functions
npm install
firebase deploy --only functions,firestore:rules
```

## Usuarios demo

| Rol | Email | Clave |
|-----|-------|-------|
| Cliente | `{DNI}@clientes.cmacica.pe` | 123456 |
| Asesor | `EMP-45821@ventas.cmacica.pe` | 123456 |
| Supervisor | `SUP-45821@ventas.cmacica.pe` | 123456 |
| Admin | `ADM-001@ventas.cmacica.pe` | 123456 |
| Comité | `COM-001@comite.cmacica.pe` | 123456 |
