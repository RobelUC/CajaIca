# Arquitectura — Ecosistema CMAC Ica

**Versión:** 1.0.0 · **Backend compartido:** Firebase Firestore (`cajaica`)

## Piezas del ecosistema

| Pieza | Tecnología | Usuario | Paquete / ruta |
|-------|------------|---------|----------------|
| App Clientes | Kotlin + Compose + MVVM | Cliente (DNI) | `CajaIcaHomeBanking` |
| App Ventas | Kotlin + Compose + MVVM + Room | Asesor / Supervisor | `CajaIcaVentas` |
| Portal Comité | React + Vite | Operador comité | `CajaIcaComite` |
| Core backend | Cloud Functions + Firestore Rules | — | `CajaIcaHomeBanking/functions` |

## Modelo de datos compartido

Ver [`DDL/001_bd_core_mobile.sql`](DDL/001_bd_core_mobile.sql) — modelo lógico SQL espejo de Firestore.

```
Firestore (runtime)          SQL lógico (documentación)
─────────────────────        ─────────────────────────
clientes/{uid}           →   cr_cliente
solicitudes_credito      →   cr_solicitud_credito
core_cola                →   cr_core_cola
cartera_dia/.../items    →   cr_cartera_dia
contadores               →   cr_contadores
(Room Ventas) sync_queue →   sync_outbox
(Room Ventas) sync_log   →   sync_log
```

## Arquitectura por capas

### App Clientes — MVVM

```
ui/screens          → Presentación (Compose)
ui/viewmodel        → ViewModel (estado UI)
data/repository     → Repositorios (Firestore)
data/model          → Modelos de dominio
security/           → JWT, RBAC, bloqueo login
```

### App Ventas — MVVM offline-first

```
ui/                 → Presentación (Compose + stepper originación)
ui/viewmodel        → ViewModel
domain/             → Reglas negocio (RF-47, buró, ficha, filtros)
data/repository     → Firestore + offline
data/local/         → Room (cartera_cache, sync_outbox, sync_log)
data/sync/          → Procesador cola sync
security/           → JWT, RBAC
```

### Portal Comité — Capas web

```
components/         → UI React
services/           → Acceso datos (Firestore, API reportes)
security/           → RBAC, tokenStore
```

### Core — Functions (equivalente controlador + servicio)

```
functions/index.js
  checkLoginLock      → Servicio bloqueo
  syncUserRole        → Servicio RBAC (custom claims)
  apiReportes         → API HTTP 401/403
  apiClientePerfil    → API cliente exclusivo
firestore.rules.example → Autorización BD
```

## Diagrama de componentes

```mermaid
flowchart TB
    subgraph Mobile
        AC[App Clientes\nMVVM]
        AV[App Ventas\nMVVM + Room]
    end
    subgraph Web
        PC[Portal Comité\nReact]
    end
    subgraph Core
        FS[(Firestore)]
        CF[Cloud Functions]
        RL[Security Rules]
    end
    subgraph Local
        RM[(Room\nsync_outbox\nsync_log\ncartera_cache)]
    end
    AC --> FS
    AV --> FS
    AV --> RM
    PC --> FS
    AC --> CF
    AV --> CF
    PC --> CF
    FS --> RL
```

## Flujo de sincronización offline (Ventas)

```mermaid
sequenceDiagram
    participant UI as OriginacionStepper
    participant VM as ViewModel
    participant OB as sync_outbox
    participant RP as SyncProcessor
    participant FS as Firestore
    participant LG as sync_log

    UI->>VM: registrarVisita()
    VM->>FS: actualizar (online)
    alt Sin conexión / error
        VM->>OB: enqueue(REGISTRAR_VISITA)
        VM->>UI: pendingSync badge
    end
    Note over RP: Al reconectar / iniciar app
    RP->>OB: leer pendientes
    RP->>FS: reintentar
    RP->>LG: registrar OK/ERROR
    RP->>OB: eliminar procesado
```

## Convenciones

- **Expediente:** `EXP-{año}-{5 dígitos}` vía `contadores/expedientes`
- **Estados solicitud:** 14 estados (`enviado` → `desembolsado`)
- **Semáforo riesgo:** VERDE / AMARILLO / ROJO (originación + mora)
- **RF-47:** Cronograma francés en `domain/OriginacionEngines.kt`

## Scripts versionados

| Script | Propósito |
|--------|-----------|
| `docs/DDL/001_bd_core_mobile.sql` | DDL modelo lógico |
| `docs/DDL/002_seed_demo.sql` | Datos demo SQL |
| `docs/seed/firestore_demo.json` | Referencia seed Firestore |
| `firestore.rules.example` | Reglas RBAC |
| `firestore.indexes.json` | Índices compuestos |
