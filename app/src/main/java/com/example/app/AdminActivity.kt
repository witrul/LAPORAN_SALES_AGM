class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        // Cek apakah user adalah admin
        if (sessionManager.getRole() != UserRole.ADMIN) {
            Toast.makeText(this, "Akses ditolak", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupButtons()
    }
    
    private fun setupButtons() {
        binding.apply {
            btnCreateUser.setOnClickListener {
                startActivity(Intent(this@AdminActivity, CreateUserActivity::class.java))
            }
            
            btnDailyProgress.setOnClickListener {
                startActivity(Intent(this@AdminActivity, DailyProgressActivity::class.java))
            }
            
            btnMonthlyAchievement.setOnClickListener {
                startActivity(Intent(this@AdminActivity, MonthlyAchievementActivity::class.java))
            }
            
            btnLogout.setOnClickListener {
                logout()
            }
        }
    }
    
    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        // Cek session setiap kali activity aktif
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }
    }
} 