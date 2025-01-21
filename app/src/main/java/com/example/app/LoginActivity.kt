class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var userDao: UserDao
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        userDao = AppDatabase.getDatabase(this).userDao()
        
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToRole(sessionManager.getRole()!!)
            finish()
            return
        }
        
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            
            if (!validateInput(username, password)) return@setOnClickListener
            
            lifecycleScope.launch {
                login(username, password)
            }
        }
        
        // Inisialisasi data dummy jika database kosong
        lifecycleScope.launch {
            initializeDummyData()
        }
    }
    
    private fun validateInput(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.etUsername.error = "Username tidak boleh kosong"
            return false
        }
        
        if (password.isEmpty()) {
            binding.etPassword.error = "Password tidak boleh kosong"
            return false
        }
        
        if (password.length < 6) {
            binding.etPassword.error = "Password minimal 6 karakter"
            return false
        }
        
        return true
    }
    
    private suspend fun login(username: String, password: String) {
        val user = userDao.getUser(username)
        
        if (user != null && verifyPassword(password, user.password)) {
            sessionManager.saveLoginSession(
                username = user.username,
                role = user.role,
                rememberMe = binding.cbRememberMe.isChecked
            )
            navigateToRole(user.role)
            finish()
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@LoginActivity,
                    "Username atau password salah",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private suspend fun initializeDummyData() {
        if (userDao.getUser("admin") == null) {
            userDao.insertUser(
                UserEntity(
                    username = "admin",
                    password = encryptPassword("admin123"),
                    role = UserRole.ADMIN
                )
            )
        }
        
        if (userDao.getUser("sales") == null) {
            userDao.insertUser(
                UserEntity(
                    username = "sales",
                    password = encryptPassword("sales123"),
                    role = UserRole.SALES
                )
            )
        }
    }
    
    private fun encryptPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
    
    private fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(password, hashedPassword)
    }
    
    private fun navigateToRole(role: UserRole) {
        val intent = when (role) {
            UserRole.ADMIN -> Intent(this, AdminActivity::class.java)
            UserRole.SALES -> Intent(this, SalesActivity::class.java)
        }
        startActivity(intent)
    }
} 
} 