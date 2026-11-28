package com.gmail.nthai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gmail.nthai.data.local.entity.BlacklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlacklistDao {
    @Query("SELECT * FROM blacklist ORDER BY id DESC")
    fun getAllBlacklist(): Flow<List<BlacklistEntity>>

    @Query("SELECT phone FROM blacklist")
    fun getAllPhonesFlow(): Flow<List<String>>

    @Query("SELECT phone FROM blacklist")
    suspend fun getAllNormalizedPhones(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM blacklist WHERE phone = :normalizedPhone LIMIT 1)")
    suspend fun isBlacklisted(normalizedPhone: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlacklistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BlacklistEntity>)

    @Update
    suspend fun update(entity: BlacklistEntity)

    @Query("DELETE FROM blacklist WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM blacklist WHERE phone = :normalizedPhone")
    suspend fun deleteByPhone(normalizedPhone: String)

    @Query("DELETE FROM blacklist")
    suspend fun deleteAll()
}
