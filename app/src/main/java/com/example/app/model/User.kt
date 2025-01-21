data class User(
    val username: String,
    val password: String,
    val role: UserRole
)

enum class UserRole {
    ADMIN,
    SALES
} 