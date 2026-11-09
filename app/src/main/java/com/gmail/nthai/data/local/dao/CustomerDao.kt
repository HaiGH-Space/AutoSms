package com.gmail.nthai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gmail.nthai.data.local.entity.CustomerDashboardStatsEntity
import com.gmail.nthai.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query(
        """
        SELECT COUNT(*) AS totalCustomers,
            COALESCE(SUM(CASE WHEN isSelected = 1 AND sendSimSlot = 1 THEN 1 ELSE 0 END), 0) AS sim1AssignedCount,
            COALESCE(SUM(CASE WHEN isSelected = 1 AND sendSimSlot = 1 AND status = 'PENDING' THEN 1 ELSE 0 END), 0) AS sim1ReadyCount,
            COALESCE(SUM(CASE WHEN isSelected = 1 AND sendSimSlot = 2 THEN 1 ELSE 0 END), 0) AS sim2AssignedCount,
            COALESCE(SUM(CASE WHEN isSelected = 1 AND sendSimSlot = 2 AND status = 'PENDING' THEN 1 ELSE 0 END), 0) AS sim2ReadyCount
        FROM customers
        """
    )
    fun observeDashboardStats(): Flow<CustomerDashboardStatsEntity>

    @Query("SELECT * FROM customers ORDER BY id ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE status IN ('PENDING', 'FAILED') AND isSelected = 1 ORDER BY id ASC")
    fun getSelectedCustomersForSending(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET status = :status WHERE id = :id")
    suspend fun updateCustomerStatus(id: Long, status: String)

    @Query("UPDATE customers SET isSelected = :isSelected, sendSimSlot = CASE WHEN :isSelected = 1 THEN sendSimSlot ELSE NULL END WHERE id = :id")
    suspend fun updateCustomerSelection(id: Long, isSelected: Boolean)

    @Query("UPDATE customers SET isSelected = :isSelected, sendSimSlot = CASE WHEN :isSelected = 1 THEN :simSlot ELSE NULL END WHERE id = :id")
    suspend fun updateCustomerSelectionForSim(id: Long, simSlot: Int, isSelected: Boolean)

    @Query("UPDATE customers SET isSelected = :isSelected, sendSimSlot = CASE WHEN :isSelected = 1 THEN sendSimSlot ELSE NULL END")
    suspend fun updateAllSelection(isSelected: Boolean)

    @Transaction
    suspend fun replaceSelection(selectedIds: List<Long>) {
        updateAllSelection(false)
        selectedIds.chunked(900).forEach { updateSelectedCustomers(it) }
    }

    @Transaction
    suspend fun updateSelectionForCustomers(customerIds: List<Long>, isSelected: Boolean) {
        customerIds.chunked(900).forEach { updateCustomerSelectionBatch(it, isSelected) }
    }

    @Transaction
    suspend fun updateSelectionForSim(customerIds: List<Long>, simSlot: Int, isSelected: Boolean) {
        customerIds.chunked(900).forEach { updateCustomerSelectionBatchForSim(it, simSlot, isSelected) }
    }

    @Query("UPDATE customers SET isSelected = 1, sendSimSlot = 1 WHERE id IN (:customerIds)")
    suspend fun updateSelectedCustomers(customerIds: List<Long>)

    @Query("UPDATE customers SET isSelected = :isSelected WHERE id IN (:customerIds)")
    suspend fun updateCustomerSelectionBatch(customerIds: List<Long>, isSelected: Boolean)

    @Query("UPDATE customers SET isSelected = :isSelected, sendSimSlot = CASE WHEN :isSelected = 1 THEN :simSlot ELSE NULL END WHERE id IN (:customerIds)")
    suspend fun updateCustomerSelectionBatchForSim(customerIds: List<Long>, simSlot: Int, isSelected: Boolean)

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()

    @Transaction
    suspend fun replaceAllCustomers(customers: List<CustomerEntity>) {
        deleteAllCustomers()
        insertCustomers(customers)
    }

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET status = 'PENDING'")
    suspend fun resetAllCustomerStatus()

    @Query("UPDATE customers SET status = 'PENDING' WHERE status = :targetStatus")
    suspend fun resetCustomerStatusByStatus(targetStatus: String)

    @Query("UPDATE customers SET status = 'PENDING' WHERE id IN (:customerIds)")
    suspend fun resetCustomerStatusByIdsBatch(customerIds: List<Long>)

    @Transaction
    suspend fun resetCustomerStatusByIds(customerIds: List<Long>) {
        customerIds.chunked(900).forEach { resetCustomerStatusByIdsBatch(it) }
    }

    @Query("SELECT phone FROM customers")
    suspend fun getAllCustomerPhones(): List<String>
}
