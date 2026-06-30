package pe.cmacica.clientes.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import pe.cmacica.clientes.data.AuthCredentials
import pe.cmacica.clientes.security.LoginLockoutService
import pe.cmacica.clientes.security.RoleSyncService
import pe.cmacica.clientes.security.SecureTokenStore
import pe.cmacica.clientes.security.UserRole

class AuthRepository(
    context: Context? = null,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val tokenStore: SecureTokenStore = SecureTokenStore(
        (context ?: pe.cmacica.clientes.AppContextHolder.appContext)
    ),
    private val lockout: LoginLockoutService = LoginLockoutService(),
    private val roleSync: RoleSyncService = RoleSyncService()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentDocumento: String?
        get() = AuthCredentials.emailToDocumento(currentUser?.email).ifBlank { null }

    val storedJwt: String?
        get() = tokenStore.getJwt()

    val currentRole: UserRole
        get() = UserRole.from(tokenStore.getRole() ?: UserRole.inferFromEmail(currentUser?.email).value)

    suspend fun signIn(documento: String, password: String): Result<FirebaseUser> = runCatching {
        val email = AuthCredentials.documentoToEmail(documento)
        val lock = lockout.checkLock(email)
        if (lock.locked) {
            error("Cuenta bloqueada por $MAX_ATTEMPTS intentos fallidos. Intente más tarde.")
        }
        val user = try {
            auth.signInWithEmailAndPassword(email, password).await().user
                ?: error("No se pudo iniciar sesión.")
        } catch (e: Exception) {
            val after = lockout.recordFailure(email)
            if (after.locked) {
                error("Cuenta bloqueada tras $MAX_ATTEMPTS intentos fallidos.")
            }
            val restantes = MAX_ATTEMPTS - after.attempts
            error("${e.message ?: "Credenciales inválidas."} ($restantes intentos restantes)")
        }
        roleSync.syncRole(user, tokenStore)
        lockout.clearAttempts(email)
        user
    }

    suspend fun signUp(documento: String, password: String): Result<FirebaseUser> = runCatching {
        val email = AuthCredentials.documentoToEmail(documento)
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("No se pudo crear la cuenta.")
        roleSync.syncRole(user, tokenStore)
        user
    }

    fun signOut() {
        tokenStore.clear()
        auth.signOut()
    }

    companion object {
        private const val MAX_ATTEMPTS = 5
    }
}
