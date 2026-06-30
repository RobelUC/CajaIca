-- =============================================================================
-- bd_core_mobile — Modelo lógico CMAC Ica (v1.0.0)
-- Espejo del núcleo crediticio + tablas móviles de sincronización
-- Implementación runtime: Firebase Firestore (proyecto cajaica)
-- =============================================================================

PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------------------
-- Catálogo núcleo (espejo cr_*)
-- ---------------------------------------------------------------------------

CREATE TABLE cr_producto (
    id              TEXT PRIMARY KEY,
    codigo          TEXT NOT NULL UNIQUE,
    nombre          TEXT NOT NULL,
    tasa_tea        REAL NOT NULL CHECK (tasa_tea >= 0),
    plazo_min_meses INTEGER NOT NULL,
    plazo_max_meses INTEGER NOT NULL,
    activo          INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1))
);

CREATE TABLE cr_cliente (
    uid             TEXT PRIMARY KEY,
    documento       TEXT NOT NULL UNIQUE,
    nombre          TEXT NOT NULL,
    email           TEXT NOT NULL,
    agencia_id      TEXT,
    created_at      INTEGER NOT NULL
);

CREATE TABLE cr_solicitud_credito (
    id              TEXT PRIMARY KEY,
    expediente      TEXT NOT NULL UNIQUE,
    cliente_uid     TEXT REFERENCES cr_cliente(uid),
    documento_cliente TEXT NOT NULL,
    nombre_cliente  TEXT NOT NULL,
    monto           REAL NOT NULL CHECK (monto > 0),
    plazo_meses     INTEGER NOT NULL,
    destino         TEXT,
    garantia        TEXT,
    canal           TEXT NOT NULL CHECK (canal IN ('cliente','agencia','fuerza_ventas')),
    estado          TEXT NOT NULL,
    agencia_id      TEXT,
    asesor_codigo   TEXT,
    producto_id     TEXT REFERENCES cr_producto(id),
    semaforo        TEXT CHECK (semaforo IN ('VERDE','AMARILLO','ROJO')),
    created_at      INTEGER NOT NULL,
    updated_at      INTEGER NOT NULL
);

CREATE TABLE cr_core_cola (
    solicitud_id    TEXT PRIMARY KEY REFERENCES cr_solicitud_credito(id) ON DELETE CASCADE,
    expediente      TEXT NOT NULL,
    estado          TEXT NOT NULL,
    agencia_id      TEXT NOT NULL,
    asesor_codigo   TEXT,
    created_at      INTEGER NOT NULL
);

CREATE TABLE cr_cartera_dia (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    asesor_codigo   TEXT NOT NULL,
    solicitud_id    TEXT NOT NULL REFERENCES cr_solicitud_credito(id) ON DELETE CASCADE,
    expediente      TEXT NOT NULL,
    documento_cliente TEXT NOT NULL,
    nombre_cliente  TEXT NOT NULL,
    tipo_gestion    TEXT NOT NULL,
    estado          TEXT NOT NULL,
    monto           REAL NOT NULL,
    visita_registrada INTEGER NOT NULL DEFAULT 0,
    semaforo        TEXT,
    created_at      INTEGER NOT NULL,
    UNIQUE (asesor_codigo, solicitud_id)
);

CREATE TABLE cr_mora (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    solicitud_id    TEXT NOT NULL REFERENCES cr_solicitud_credito(id) ON DELETE CASCADE,
    dias_mora       INTEGER NOT NULL DEFAULT 0,
    semaforo        TEXT NOT NULL CHECK (semaforo IN ('VERDE','AMARILLO','ROJO')),
    cuotas_vencidas INTEGER NOT NULL DEFAULT 0,
    updated_at      INTEGER NOT NULL
);

CREATE TABLE cr_contadores (
    clave           TEXT PRIMARY KEY,
    anio            INTEGER NOT NULL,
    ultimo          INTEGER NOT NULL
);

-- ---------------------------------------------------------------------------
-- Puente móvil: outbox + log (Room SQLite en App Ventas)
-- ---------------------------------------------------------------------------

CREATE TABLE sync_outbox (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    solicitud_id    TEXT NOT NULL,
    action          TEXT NOT NULL,
    payload_json    TEXT NOT NULL,
    created_at      INTEGER NOT NULL,
    retry_count     INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE sync_log (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    solicitud_id    TEXT NOT NULL,
    action          TEXT NOT NULL,
    status          TEXT NOT NULL CHECK (status IN ('OK','ERROR')),
    message         TEXT,
    synced_at       INTEGER NOT NULL
);

-- Cache local cartera (offline-first App Ventas)
CREATE TABLE cartera_cache (
    solicitud_id    TEXT PRIMARY KEY,
    expediente      TEXT NOT NULL,
    documento_cliente TEXT NOT NULL,
    nombre_cliente  TEXT NOT NULL,
    tipo_gestion    TEXT NOT NULL,
    estado          TEXT NOT NULL,
    monto           REAL NOT NULL,
    created_at      INTEGER NOT NULL,
    visita_registrada INTEGER NOT NULL DEFAULT 0,
    pending_sync    INTEGER NOT NULL DEFAULT 0,
    semaforo        TEXT,
    asesor_codigo   TEXT NOT NULL,
    last_synced_at  INTEGER NOT NULL
);

-- ---------------------------------------------------------------------------
-- Índices
-- ---------------------------------------------------------------------------

CREATE INDEX idx_solicitud_cliente ON cr_solicitud_credito(cliente_uid);
CREATE INDEX idx_solicitud_estado ON cr_solicitud_credito(estado);
CREATE INDEX idx_cartera_asesor ON cr_cartera_dia(asesor_codigo);
CREATE INDEX idx_mora_semaforo ON cr_mora(semaforo);
CREATE INDEX idx_outbox_created ON sync_outbox(created_at);
CREATE INDEX idx_sync_log_solicitud ON sync_log(solicitud_id);

-- ---------------------------------------------------------------------------
-- Mapeo Firestore ↔ SQL (referencia)
-- ---------------------------------------------------------------------------
-- Firestore                    → Tabla SQL
-- clientes/{uid}               → cr_cliente
-- solicitudes_credito/{id}     → cr_solicitud_credito
-- core_cola/{id}               → cr_core_cola
-- cartera_dia/{a}/items/{id}   → cr_cartera_dia
-- contadores/expedientes       → cr_contadores
-- (Room local) sync_queue        → sync_outbox
-- (Room local) sync_log          → sync_log
