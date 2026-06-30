/**
 * Backend RBAC — CMAC Ica
 * Matriz de roles:
 *   cliente        → solo datos propios (clientes/{uid})
 *   asesor         → cartera, cola, originación
 *   supervisor     → asesor + reportes
 *   administrador  → supervisor + contadores
 *   comite         → evaluación de solicitudes
 */
const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");

setGlobalOptions({ region: "us-central1" });
admin.initializeApp();

const MAX_ATTEMPTS = 5;
const LOCK_MS = 15 * 60 * 1000;

function roleFromEmail(email) {
  if (!email) return "cliente";
  const e = email.toLowerCase();
  const local = e.split("@")[0] || "";
  if (e.endsWith("@clientes.cmacica.pe")) return "cliente";
  if (e.endsWith("@comite.cmacica.pe")) return "comite";
  if (e.endsWith("@ventas.cmacica.pe")) {
    if (local.startsWith("adm-")) return "administrador";
    if (local.startsWith("sup-")) return "supervisor";
    return "asesor";
  }
  return "cliente";
}

async function getLockDoc(identifier) {
  const id = (identifier || "").toLowerCase().trim();
  if (!id) throw new HttpsError("invalid-argument", "identifier requerido");
  const ref = admin.firestore().collection("login_attempts").doc(id);
  const snap = await ref.get();
  return { ref, id, data: snap.exists ? snap.data() : { count: 0 } };
}

exports.checkLoginLock = onCall(async (request) => {
  const { data } = await getLockDoc(request.data.identifier);
  const lockedUntil = data.lockedUntil?.toMillis?.() || 0;
  if (lockedUntil > Date.now()) {
    return { locked: true, attempts: data.count || MAX_ATTEMPTS, lockedUntil };
  }
  return { locked: false, attempts: data.count || 0 };
});

exports.recordFailedLogin = onCall(async (request) => {
  const { ref, data } = await getLockDoc(request.data.identifier);
  const count = (data.count || 0) + 1;
  const update = {
    count,
    lastAttempt: admin.firestore.FieldValue.serverTimestamp(),
  };
  if (count >= MAX_ATTEMPTS) {
    update.lockedUntil = admin.firestore.Timestamp.fromMillis(Date.now() + LOCK_MS);
  }
  await ref.set(update, { merge: true });
  const snap = await ref.get();
  const next = snap.data();
  const lockedUntil = next.lockedUntil?.toMillis?.() || null;
  return {
    attempts: next.count,
    locked: lockedUntil != null && lockedUntil > Date.now(),
    lockedUntil,
  };
});

exports.clearLoginAttempts = onCall(async (request) => {
  const { ref } = await getLockDoc(request.data.identifier);
  await ref.delete();
  return { ok: true };
});

exports.syncUserRole = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "401: JWT requerido");
  }
  const email = request.auth.token.email || "";
  const role = roleFromEmail(email);
  await admin.auth().setCustomUserClaims(request.auth.uid, { role });
  return { role, email };
});

exports.apiReportes = onRequest({ cors: true }, async (req, res) => {
  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return;
  }
  const header = req.headers.authorization || "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : "";
  if (!token) {
    res.status(401).json({ error: "401: JWT no proporcionado" });
    return;
  }
  try {
    const decoded = await admin.auth().verifyIdToken(token);
    const role = decoded.role || roleFromEmail(decoded.email);
    if (role !== "supervisor" && role !== "administrador") {
      res.status(403).json({
        error: "403: Reportes restringidos a supervisor o administrador",
        rol: role,
      });
      return;
    }
    const snap = await admin.firestore().collection("solicitudes_credito").limit(200).get();
    const porEstado = {};
    snap.docs.forEach((d) => {
      const est = d.data().estado || "desconocido";
      porEstado[est] = (porEstado[est] || 0) + 1;
    });
    res.json({
      reporte: "cartera_consolidada",
      rol,
      generadoEn: new Date().toISOString(),
      total: snap.size,
      porEstado,
    });
  } catch {
    res.status(401).json({ error: "401: JWT inválido o expirado" });
  }
});

exports.apiClientePerfil = onRequest({ cors: true }, async (req, res) => {
  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return;
  }
  const header = req.headers.authorization || "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : "";
  if (!token) {
    res.status(401).json({ error: "401: JWT no proporcionado" });
    return;
  }
  try {
    const decoded = await admin.auth().verifyIdToken(token);
    if ((decoded.role || roleFromEmail(decoded.email)) !== "cliente") {
      res.status(403).json({ error: "403: Endpoint exclusivo de cliente" });
      return;
    }
    const uid = decoded.uid;
    const requestedUid = req.query.uid || req.body?.uid;
    if (requestedUid && requestedUid !== uid) {
      res.status(403).json({ error: "403: Solo puede consultar su propio perfil" });
      return;
    }
    const doc = await admin.firestore().collection("clientes").doc(uid).get();
    if (!doc.exists) {
      res.status(404).json({ error: "Perfil no encontrado" });
      return;
    }
    res.json({ uid, perfil: doc.data() });
  } catch {
    res.status(401).json({ error: "401: JWT inválido" });
  }
});
