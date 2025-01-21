class CreateUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateUserBinding
    private lateinit var userDao: UserDao
    private lateinit var sessionManager: SessionManager
    private var selectedRole: UserRole = UserRole.SALES
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Cek apakah user adalah admin
        sessionManager = SessionManager(this)
        if (sessionManager.getRole() != UserRole.ADMIN) {
            Toast.makeText(this, "Akses ditolak", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        userDao = AppDatabase.getDatabase(this).userDao()
        
        setupUserId()
        setupRoleSpinner()
        setupTargetOmset()
        setupSaveButton()
    }
    
    private fun setupUserId() {
        val userId = UUID.randomUUID().toString()
        binding.tvUserId.text = "ID User: $userId"
    }
    
    private fun setupRoleSpinner() {
        val roles = arrayOf("Sales", "Admin")
        val adapter = ArrayAdapter(this, R.layout.list_item, roles)
        binding.spinnerRole.setAdapter(adapter)
        
        binding.spinnerRole.setOnItemClickListener { _, _, position, _ ->
            selectedRole = if (position == 0) UserRole.SALES else UserRole.ADMIN
            binding.tilTargetOmset.visibility = 
                if (selectedRole == UserRole.SALES) View.VISIBLE else View.GONE
        }
        
        // Set default value
        binding.spinnerRole.setText("Sales", false)
    }
    
    private fun setupTargetOmset() {
        binding.etTargetOmset.addTextChangedListener(object : TextWatcher {
            private var current = ""
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    binding.etTargetOmset.removeTextChangedListener(this)
                    
                    val cleanString = s.toString().replace(Regex("[Rp,.]"), "")
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0
                    
                    val formatted = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        .format(parsed)
                        .replace("Rp", "Rp ")
                    
                    current = formatted
                    binding.etTargetOmset.setText(formatted)
                    binding.etTargetOmset.setSelection(formatted.length)
                    
                    binding.etTargetOmset.addTextChangedListener(this)
                }
            }
        })
    }
    
    private fun setupSaveButton() {
        binding.btnSimpan.setOnClickListener {
            if (validateInput()) {
                lifecycleScope.launch {
                    saveUser()
                }
            }
        }
    }
    
    private fun validateInput(): Boolean {
        if (binding.etNama.text.toString().isEmpty()) {
            binding.etNama.error = "Nama harus diisi"
            return false
        }
        
        if (binding.etUsername.text.toString().isEmpty()) {
            binding.etUsername.error = "Username harus diisi"
            return false
        }
        
        if (binding.etPassword.text.toString().isEmpty()) {
            binding.etPassword.error = "Password harus diisi"
            return false
        }
        
        if (binding.etPassword.text.toString().length < 6) {
            binding.etPassword.error = "Password minimal 6 karakter"
            return false
        }
        
        if (selectedRole == UserRole.SALES && binding.etTargetOmset.text.toString().isEmpty()) {
            binding.etTargetOmset.error = "Target omset harus diisi"
            return false
        }
        
        return true
    }
    
    private suspend fun saveUser() {
        val username = binding.etUsername.text.toString()
        
        // Cek username sudah ada atau belum
        if (userDao.isUsernameExists(username)) {
            withContext(Dispatchers.Main) {
                binding.etUsername.error = "Username sudah digunakan"
            }
            return
        }
        
        val targetOmset = if (selectedRole == UserRole.SALES) {
            binding.etTargetOmset.text.toString()
                .replace(Regex("[Rp,.]"), "")
                .toLong()
        } else null
        
        val user = UserEntity(
            userId = binding.tvUserId.text.toString().replace("ID User: ", ""),
            nama = binding.etNama.text.toString(),
            username = username,
            password = encryptPassword(binding.etPassword.text.toString()),
            role = selectedRole,
            targetOmset = targetOmset
        )
        
        userDao.insertUser(user)
        
        withContext(Dispatchers.Main) {
            Toast.makeText(this@CreateUserActivity, "User berhasil dibuat", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun encryptPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
} 