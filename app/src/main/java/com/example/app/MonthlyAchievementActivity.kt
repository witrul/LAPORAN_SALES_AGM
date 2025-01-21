class MonthlyAchievementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMonthlyAchievementBinding
    private lateinit var adapter: MonthlyAchievementAdapter
    private lateinit var userDao: UserDao
    private lateinit var salesDataDao: SalesDataDao
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlyAchievementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        userDao = AppDatabase.getDatabase(this).userDao()
        salesDataDao = AppDatabase.getDatabase(this).salesDataDao()
        
        setupRecyclerView()
        loadData()
    }
    
    private fun setupRecyclerView() {
        adapter = MonthlyAchievementAdapter()
        binding.rvMonthlyAchievement.apply {
            layoutManager = LinearLayoutManager(this@MonthlyAchievementActivity)
            adapter = this@MonthlyAchievementActivity.adapter
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            val salesUsers = userDao.getSalesUsers()
            
            val achievements = salesUsers.map { user ->
                val totalTagihan = salesDataDao.getTotalNominalByUserAndMonth(
                    user.username,
                    currentMonth
                )
                
                val pencapaian = if (user.targetOmset != null && user.targetOmset > 0) {
                    (totalTagihan.toDouble() / user.targetOmset.toDouble()) * 100
                } else 0.0
                
                MonthlyAchievement(
                    user = user,
                    totalTagihan = totalTagihan,
                    pencapaian = pencapaian
                )
            }
            
            adapter.submitList(achievements)
        }
    }
}

data class MonthlyAchievement(
    val user: UserEntity,
    val totalTagihan: Long,
    val pencapaian: Double
)

class MonthlyAchievementAdapter : 
    ListAdapter<MonthlyAchievement, MonthlyAchievementAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<MonthlyAchievement>() {
            override fun areItemsTheSame(oldItem: MonthlyAchievement, newItem: MonthlyAchievement) = 
                oldItem.user.userId == newItem.user.userId
            override fun areContentsTheSame(oldItem: MonthlyAchievement, newItem: MonthlyAchievement) = 
                oldItem == newItem
        }
    ) {
    
    class ViewHolder(private val binding: ItemMonthlyAchievementBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MonthlyAchievement) {
            binding.apply {
                tvNamaSales.text = item.user.nama
                tvTargetOmset.text = formatRupiah(item.user.targetOmset ?: 0)
                tvTotalTagihan.text = formatRupiah(item.totalTagihan)
                tvPencapaian.text = String.format("%.1f%%", item.pencapaian)
                
                // Set warna berdasarkan pencapaian
                tvPencapaian.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        when {
                            item.pencapaian >= 100 -> android.R.color.holo_green_dark
                            item.pencapaian >= 75 -> android.R.color.holo_blue_dark
                            item.pencapaian >= 50 -> android.R.color.holo_orange_dark
                            else -> android.R.color.holo_red_dark
                        }
                    )
                )
            }
        }
        
        private fun formatRupiah(nominal: Long): String {
            return NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                .format(nominal)
                .replace("Rp", "Rp ")
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMonthlyAchievementBinding.inflate(
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