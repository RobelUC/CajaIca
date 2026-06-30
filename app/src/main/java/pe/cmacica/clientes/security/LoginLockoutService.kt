package pe.cmacica.clientes.security

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

data class LockStatus(
    val locked: Boolean,
    val attempts: Int = 0,
    val lockedUntil: Long? = null
)

class LoginLockoutService(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("us-central1")
) {
    suspend fun checkLock(identifier: String): LockStatus {
        val result = functions.getHttpsCallable("checkLoginLock")
            .call(mapOf("identifier" to identifier.lowercase().trim()))
            .await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as Map<String, Any?>
        return LockStatus(
            locked = data["locked"] as? Boolean ?: false,
            attempts = (data["attempts"] as? Number)?.toInt() ?: 0,
            lockedUntil = (data["lockedUntil"] as? Number)?.toLong()
        )
    }

    suspend fun recordFailure(identifier: String): LockStatus {
        val result = functions.getHttpsCallable("recordFailedLogin")
            .call(mapOf("identifier" to identifier.lowercase().trim()))
            .await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as Map<String, Any?>
        return LockStatus(
            locked = data["locked"] as? Boolean ?: false,
            attempts = (data["attempts"] as? Number)?.toInt() ?: 0,
            lockedUntil = (data["lockedUntil"] as? Number)?.toLong()
        )
    }

    suspend fun clearAttempts(identifier: String) {
        runCatching {
            functions.getHttpsCallable("clearLoginAttempts")
                .call(mapOf("identifier" to identifier.lowercase().trim()))
                .await()
        }
    }
}
