@Entity(tableName = "sales_data")
data class SalesData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val namaToko: String,
    val alamatToko: String,
    val latitude: Double,
    val longitude: Double,
    val nominal: Long,
    val salesUsername: String
)

@Dao
interface SalesDataDao {
    @Insert
    suspend fun insert(salesData: SalesData): Long

    @Query("SELECT * FROM sales_data WHERE salesUsername = :username ORDER BY timestamp DESC")
    suspend fun getSalesDataByUsername(username: String): List<SalesData>
} 