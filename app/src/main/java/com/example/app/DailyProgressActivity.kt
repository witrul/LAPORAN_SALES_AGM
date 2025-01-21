class DailyProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDailyProgressBinding
    private lateinit var adapter: DailyProgressAdapter
    private lateinit var userDao: UserDao
    private lateinit var salesDataDao: SalesDataDao
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        userDao = AppDatabase.getDatabase(this).userDao()
        salesDataDao = AppDatabase.getDatabase(this).salesDataDao()
        
        setupRecyclerView()
        loadData()
    }
    
    private fun setupRecyclerView() {
        adapter = DailyProgressAdapter()
        binding.rvDailyProgress.apply {
            layoutManager = LinearLayoutManager(this@DailyProgressActivity)
            adapter = this@DailyProgressActivity.adapter
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            val salesDataWithUsers = salesDataDao.getAllSalesDataWithUsers()
            adapter.submitList(salesDataWithUsers)
            
            // Hitung total nominal
            val totalNominal = salesDataWithUsers.sumOf { it.salesData.nominal }
            binding.tvTotalNominal.text = "Total Nominal: ${formatRupiah(totalNominal)}"
        }
    }
    
    private fun formatRupiah(nominal: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            .format(nominal)
            .replace("Rp", "Rp ")
    }
}

class DailyProgressAdapter : ListAdapter<SalesDataWithUser, DailyProgressAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<SalesDataWithUser>() {
        override fun areItemsTheSame(oldItem: SalesDataWithUser, newItem: SalesDataWithUser) = 
            oldItem.salesData.id == newItem.salesData.id
        override fun areContentsTheSame(oldItem: SalesDataWithUser, newItem: SalesDataWithUser) = 
            oldItem == newItem
    }
) {
    class ViewHolder(private val binding: ItemDailyProgressBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SalesDataWithUser) {
            binding.apply {
                tvTanggal.text = formatDate(item.salesData.timestamp)
                tvNamaSales.text = item.user.nama
                tvNamaToko.text = item.salesData.namaToko
                tvAlamat.text = item.salesData.alamatToko
                tvNominal.text = formatRupiah(item.salesData.nominal)
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id")).format(Date(timestamp))
        }
        
        private fun formatRupiah(nominal: Long): String {
            return NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                .format(nominal)
                .replace("Rp", "Rp ")
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDailyProgressBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
} 