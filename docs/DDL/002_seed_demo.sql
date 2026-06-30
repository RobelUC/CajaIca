-- =============================================================================
-- Seed demo calibrado — CMAC Ica (v1.0.0)
-- Productos coherentes, mora con semáforo, solicitudes en distintos estados
-- =============================================================================

INSERT INTO cr_producto (id, codigo, nombre, tasa_tea, plazo_min_meses, plazo_max_meses, activo) VALUES
('PROD-MCP', 'MCP-01', 'Microcrédito Productivo', 0.28, 6, 24, 1),
('PROD-CONS', 'CONS-01', 'Crédito Consumo', 0.32, 12, 36, 1),
('PROD-NV', 'NV-01', 'Crédito Negocio Verde', 0.26, 12, 48, 1);

INSERT INTO cr_cliente (uid, documento, nombre, email, agencia_id, created_at) VALUES
('uid-demo-41250677', '41250677', 'Ludim Perez', '41250677@clientes.cmacica.pe', 'AG-Ica-01', 1704067200000),
('uid-demo-72345681', '72345681', 'Maria Quispe', '72345681@clientes.cmacica.pe', 'AG-Ica-01', 1706745600000);

INSERT INTO cr_contadores (clave, anio, ultimo) VALUES ('expedientes', 2026, 3);

INSERT INTO cr_solicitud_credito (
    id, expediente, cliente_uid, documento_cliente, nombre_cliente,
    monto, plazo_meses, destino, garantia, canal, estado,
    agencia_id, asesor_codigo, producto_id, semaforo, created_at, updated_at
) VALUES
('sol-demo-001', 'EXP-2026-00001', 'uid-demo-41250677', '41250677', 'Ludim Perez',
 15000, 12, 'Capital de trabajo', 'Sin garantía', 'cliente', 'recibido_core',
 'AG-Ica-01', NULL, 'PROD-MCP', 'AMARILLO', 1717200000000, 1717200000000),
('sol-demo-002', 'EXP-2026-00002', 'uid-demo-72345681', '72345681', 'Maria Quispe',
 8000, 18, 'Mejora vivienda', 'Garantía personal', 'cliente', 'en_cartera_asesor',
 'AG-Ica-01', 'EMP-45821', 'PROD-CONS', 'VERDE', 1717286400000, 1717286400000),
('sol-demo-003', 'EXP-2026-00003', NULL, '45678912', 'Carlos Ruiz',
 25000, 24, 'Ampliación negocio', 'Hipoteca', 'fuerza_ventas', 'documentos_firmados',
 'AG-Ica-01', 'EMP-45821', 'PROD-NV', 'ROJO', 1717372800000, 1717372800000);

INSERT INTO cr_core_cola (solicitud_id, expediente, estado, agencia_id, asesor_codigo, created_at) VALUES
('sol-demo-001', 'EXP-2026-00001', 'pendiente_promocion', 'AG-Ica-01', NULL, 1717200000000);

INSERT INTO cr_cartera_dia (
    asesor_codigo, solicitud_id, expediente, documento_cliente, nombre_cliente,
    tipo_gestion, estado, monto, visita_registrada, semaforo, created_at
) VALUES
('EMP-45821', 'sol-demo-002', 'EXP-2026-00002', '72345681', 'Maria Quispe',
 'NUEVA_SOLICITUD', 'en_cartera_asesor', 8000, 0, 'VERDE', 1717286400000),
('EMP-45821', 'sol-demo-003', 'EXP-2026-00003', '45678912', 'Carlos Ruiz',
 'NUEVA_SOLICITUD', 'documentos_firmados', 25000, 1, 'ROJO', 1717372800000);

-- Mora calibrada con semáforo (días mora → riesgo)
INSERT INTO cr_mora (solicitud_id, dias_mora, semaforo, cuotas_vencidas, updated_at) VALUES
('sol-demo-001', 0,  'VERDE',  0, 1717200000000),
('sol-demo-002', 15, 'AMARILLO', 1, 1717286400000),
('sol-demo-003', 45, 'ROJO',   2, 1717372800000);

-- Ejemplo outbox pendiente (offline)
INSERT INTO sync_outbox (solicitud_id, action, payload_json, created_at, retry_count) VALUES
('sol-demo-002', 'REGISTRAR_VISITA',
 '{"observacion":"Visita programada campo","latitud":-14.068,"longitud":-75.7255}', 1717290000000, 0);

INSERT INTO sync_log (solicitud_id, action, status, message, synced_at) VALUES
('sol-demo-003', 'REGISTRAR_ORIGINACION', 'OK', 'Expediente transmitido a Firestore', 1717372800000);
