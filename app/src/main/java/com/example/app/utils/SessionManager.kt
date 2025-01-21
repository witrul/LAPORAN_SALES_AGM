class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "LoginPrefs",
        Context.MODE_PRIVATE
    )
    
    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        "SecurePrefs",
        getMasterKey(context),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveLoginSession(username: String, role: UserRole, rememberMe: Boolean) {
        with(sharedPreferences.edit()) {
            putString("USERNAME", username)
            putString("ROLE", role.name)
            putBoolean("REMEMBER_ME", rememberMe)
            apply()
        }
    }
    
    fun isLoggedIn(): Boolean = sharedPreferences.getString("USERNAME", null) != null
    
    fun getRole(): UserRole? {
        val roleStr = sharedPreferences.getString("ROLE", null)
        return roleStr?.let { UserRole.valueOf(it) }
    }
    
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
    
    private fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
} 