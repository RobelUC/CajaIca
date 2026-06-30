package pe.cmacica.clientes.security

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class RoleSyncService(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("us-central1")
) {
    suspend fun syncRole(user: FirebaseUser, tokenStore: SecureTokenStore): UserRole {
        val tokenResult = user.getIdToken(true).await()
        tokenStore.saveJwt(tokenResult.token.orEmpty())
        val role = runCatching {
            val result = functions.getHttpsCallable("syncUserRole").call(emptyMap<String, Any>()).await()
            @Suppress("UNCHECKED_CAST")
            (result.getData() as Map<String, Any?>)["role"] as? String
        }.getOrNull()
        val resolved = UserRole.from(role ?: UserRole.inferFromEmail(user.email).value)
        tokenStore.saveRole(resolved.value)
        user.getIdToken(true).await()
        return resolved
    }
}
