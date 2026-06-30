package pe.cmacica.clientes.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureTokenStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveJwt(token: String) {
        prefs.edit().putString(KEY_JWT, token).putLong(KEY_SAVED_AT, System.currentTimeMillis()).apply()
    }

    fun getJwt(): String? = prefs.getString(KEY_JWT, null)

    fun saveRole(role: String) {
        prefs.edit().putString(KEY_ROLE, role).apply()
    }

    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "cmacica_secure_auth"
        private const val KEY_JWT = "jwt_id_token"
        private const val KEY_ROLE = "user_role"
        private const val KEY_SAVED_AT = "saved_at"
    }
}
