@Dao
interface SalesDataDao {
    @Insert
    suspend fun insert(salesData: SalesData): Long
    
    @Query("SELECT * FROM sales_data WHERE salesUsername = :username ORDER BY timestamp DESC")
    suspend fun getSalesDataByUsername(username: String): List<SalesData>
    
    @Transaction
    @Query("SELECT * FROM sales_data ORDER BY timestamp DESC")
    suspend fun getAllSalesDataWithUsers(): List<SalesDataWithUser>
    
    @Query("""
        SELECT COALESCE(SUM(nominal), 0) 
        FROM sales_data 
        WHERE salesUsername = :username 
        AND strftime('%m', datetime(timestamp/1000, 'unixepoch')) = :month
    """)
    suspend fun getTotalNominalByUserAndMonth(username: String, month: Int): Long
}

data class SalesDataWithUser(
    @Embedded val salesData: SalesData,
    @Relation(
        parentColumn = "salesUsername",
        entityColumn = "username"
    )
    val user: UserEntity
) 