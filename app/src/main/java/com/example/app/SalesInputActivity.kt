class SalesInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySalesInputBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sessionManager: SessionManager
    private lateinit var salesDataDao: SalesDataDao
    
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sessionManager = SessionManager(this)
        salesDataDao = AppDatabase.getDatabase(this).salesDataDao()
        
        setupDateTime()
        setupLocationButton()
        setupNominalInput()
        setupSaveButton()
    }
    
    private fun setupDateTime() {
        val currentDateTime = SimpleDateFormat(
            "EEEE, dd MMMM yyyy HH:mm:ss",
            Locale("id", "ID")
        ).format(Date())
        binding.tvDateTime.text = currentDateTime
    }
    
    private fun setupLocationButton() {
        binding.btnLocation.setOnClickListener {
            if (checkLocationPermission()) {
                getCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }
    }
    
    private fun setupNominalInput() {
        binding.etNominal.addTextChangedListener(object : TextWatcher {
            private var current = ""
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    binding.etNominal.removeTextChangedListener(this)
                    
                    val cleanString = s.toString().replace(Regex("[Rp,.]"), "")
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0
                    
                    val formatted = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        .format(parsed)
                        .replace("Rp", "Rp ")
                    
                    current = formatted
                    binding.etNominal.setText(formatted)
                    binding.etNominal.setSelection(formatted.length)
                    
                    binding.etNominal.addTextChangedListener(this)
                }
            }
        })
    }
    
    private fun setupSaveButton() {
        binding.btnSimpan.setOnClickListener {
            if (validateInput()) {
                saveData()
            }
        }
    }
    
    private fun validateInput(): Boolean {
        if (binding.etNamaToko.text.toString().isEmpty()) {
            binding.etNamaToko.error = "Nama toko harus diisi"
            return false
        }
        
        if (binding.etAlamatToko.text.toString().isEmpty()) {
            binding.etAlamatToko.error = "Alamat toko harus diisi"
            return false
        }
        
        if (currentLatitude == 0.0 || currentLongitude == 0.0) {
            Toast.makeText(this, "Mohon ambil lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (binding.etNominal.text.toString().isEmpty()) {
            binding.etNominal.error = "Nominal harus diisi"
            return false
        }
        
        return true
    }
    
    private fun saveData() {
        lifecycleScope.launch {
            val nominal = binding.etNominal.text.toString()
                .replace(Regex("[Rp,.]"), "")
                .toLong()
            
            val salesData = SalesData(
                timestamp = System.currentTimeMillis(),
                namaToko = binding.etNamaToko.text.toString(),
                alamatToko = binding.etAlamatToko.text.toString(),
                latitude = currentLatitude,
                longitude = currentLongitude,
                nominal = nominal,
                salesUsername = sessionManager.getUsername()!!
            )
            
            salesDataDao.insert(salesData)
            
            withContext(Dispatchers.Main) {
                Toast.makeText(this@SalesInputActivity, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude
                    updateLocationText(it)
                }
            }
        }
    }
    
    private fun updateLocationText(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.let { address ->
                val addressText = """
                    Latitude: ${location.latitude}
                    Longitude: ${location.longitude}
                    Alamat: ${address.getAddressLine(0)}
                """.trimIndent()
                binding.tvLocation.text = addressText
            }
        } catch (e: Exception) {
            binding.tvLocation.text = "Lat: ${location.latitude}, Long: ${location.longitude}"
        }
    }
    
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            }
        }
    }
} 