# Diagramas UML — Ecosistema CMAC Ica

Referencia visual complementaria a [`arquitectura.md`](../arquitectura.md) y [`HU-RF-TRAZABILIDAD.md`](../HU-RF-TRAZABILIDAD.md).

## Componentes del ecosistema

```mermaid
flowchart TB
    subgraph Clientes["App Clientes (Kotlin MVVM)"]
        C_UI[ui/screens]
        C_VM[ViewModel]
        C_REPO[AuthRepository\nSolicitudRepository]
    end
    subgraph Ventas["App Ventas (Kotlin MVVM + Room)"]
        V_UI[ui/screens + stepper]
        V_VM[VentasViewModel\nOriginacionViewModel]
        V_DOM[domain engines\nRF-47, buró, ficha]
        V_REPO[VentasRepository]
        V_ROOM[(Room\ncartera_cache\nsync_queue\nsync_log)]
        V_SYNC[SyncOutboxProcessor]
    end
    subgraph Comite["Portal Comité (React)"]
        W_UI[components]
        W_SVC[services/auth\nreportesApi]
    end
    subgraph Backend["Firebase cajaica"]
        FS[(Firestore)]
        CF[Cloud Functions]
        RL[Security Rules]
    end
    C_UI --> C_VM --> C_REPO --> FS
    V_UI --> V_VM --> V_DOM
    V_VM --> V_REPO --> FS
    V_VM --> V_SYNC --> V_REPO
    V_SYNC --> V_ROOM
    V_REPO --> V_ROOM
    W_UI --> W_SVC --> FS
    C_REPO --> CF
    V_REPO --> CF
    W_SVC --> CF
    FS --> RL
```

## Casos de uso

```mermaid
flowchart TB
    Cliente((Cliente))
    Asesor((Asesor))
    Supervisor((Supervisor))
    Comite((Comité))

    Cliente --> UC1[Solicitar crédito]
    Cliente --> UC2[Consultar timeline]
    Asesor --> UC3[Gestionar cartera]
    Asesor --> UC4[Originación en campo]
    Asesor --> UC5[Sync offline outbox]
    Supervisor --> UC6[Ver reportes API]
    Comite --> UC7[Decidir expediente]
    UC1 & UC2 & UC3 & UC4 & UC5 & UC6 & UC7 --> Core[(Firestore + Functions)]
```

## Clases — Dominio originación (Ventas)

```mermaid
classDiagram
    class FichaCliente {
        +String documento
        +String nombre
        +SemaforoRiesgo semaforo
        +Boolean elegible
    }
    class ConsultaBuro {
        +String sbsCalificacion
        +Boolean listaNegra
        +Boolean consentimientoFirmado
    }
    class PreEvaluacion {
        +Boolean capacidadPagoOk
        +Boolean buroOk
        +String resultadoObtenido
    }
    class SimulacionCredito {
        +Double monto
        +List~CuotaCronograma~ cronograma
    }
    class CronogramaSimulator {
        +simular(monto, plazo) SimulacionCredito
    }
    class BuroService {
        +consultar(dni) ConsultaBuro
    }
    class FichaClienteBuilder {
        +construir() FichaCliente
    }
    class PreEvaluacionEngine {
        +evaluar(ficha, buro) Tuple
    }
    FichaClienteBuilder --> FichaCliente
    BuroService --> ConsultaBuro
    CronogramaSimulator --> SimulacionCredito
    PreEvaluacionEngine --> PreEvaluacion
```

## Clases — Sync offline (Room)

```mermaid
classDiagram
    class SyncQueueEntity {
        +Long id
        +String solicitudId
        +String action
        +String payloadJson
        +Long createdAt
        +Int retryCount
    }
    class SyncLogEntity {
        +Long id
        +String solicitudId
        +String action
        +String status
        +String message
        +Long syncedAt
    }
    class SyncOutboxRepository {
        +enqueue()
        +observePendingCount() Flow
    }
    class SyncOutboxProcessor {
        +processAll(asesorCodigo) Int
    }
    class VentasRepository {
        +registrarVisita()
        +guardarFicha()
        +promoverAComite()
    }
    SyncOutboxRepository --> SyncQueueEntity
    SyncOutboxRepository --> SyncLogEntity
    SyncOutboxProcessor --> SyncOutboxRepository
    SyncOutboxProcessor --> VentasRepository
```

## Secuencia — Login con RBAC y bloqueo

```mermaid
sequenceDiagram
    actor U as Usuario
    participant A as AuthRepository
    participant L as LoginLockoutService
    participant F as Firebase Auth
    participant CF as syncUserRole

    U->>A: login(email, password)
    A->>L: checkLoginLock(email)
    alt Bloqueado
        L-->>A: locked 15 min
        A-->>U: error
    else OK
        A->>F: signInWithEmailAndPassword
        alt Falla
            F-->>A: error
            A->>L: recordFailedLogin
        else Éxito
            F-->>A: user + JWT
            A->>CF: syncUserRole
            CF-->>A: custom claim role
            A->>L: clearLoginAttempts
            A-->>U: sesión activa
        end
    end
```

## Secuencia — Sync outbox al reconectar

```mermaid
sequenceDiagram
    participant VM as VentasViewModel
    participant SP as SyncOutboxProcessor
    participant OB as sync_queue
    participant VR as VentasRepository
    participant FS as Firestore
    participant LG as sync_log

    VM->>SP: processAll(asesorCodigo)
    SP->>OB: leer pendientes ASC
    loop cada item
        SP->>VR: ejecutar action
        alt OK
            VR->>FS: commit
            SP->>LG: status OK
            SP->>OB: delete
        else ERROR
            SP->>LG: status ERROR
            SP->>OB: retryCount++
        end
    end
    SP-->>VM: procesados N
```

## Estados — Solicitud de crédito

```mermaid
stateDiagram-v2
    [*] --> enviado
    enviado --> recibido_core
    recibido_core --> en_cartera_asesor
    en_cartera_asesor --> visitado
    visitado --> pre_evaluacion_ok
    pre_evaluacion_ok --> documentos_firmados
    documentos_firmados --> promovido_nucleo
    promovido_nucleo --> recibido_comite
    recibido_comite --> en_evaluacion
    en_evaluacion --> aprobado
    en_evaluacion --> condicionado
    en_evaluacion --> rechazado
    aprobado --> desembolsado
    desembolsado --> [*]
    rechazado --> [*]
```

## Secuencia — Originación stepper (RF-47)

```mermaid
sequenceDiagram
    actor A as Asesor
    participant UI as OriginacionStepper
    participant VM as OriginacionViewModel
    participant D as Domain
    participant R as VentasRepository
    participant OB as SyncOutbox

    A->>UI: completar pasos 1-5
    UI->>VM: transmitir()
    VM->>D: CronogramaSimulator (RF-47)
    VM->>R: registrarOriginacionCampo
    alt Online OK
        R-->>VM: EXP-2026-NNNNN
    else Offline / error
        R-->>VM: failure
        VM->>OB: enqueue(ORIGINACION_CAMPO)
        VM-->>UI: badge pendingSync
    end
```
