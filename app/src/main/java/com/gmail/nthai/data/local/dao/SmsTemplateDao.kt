package com.gmail.nthai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gmail.nthai.data.local.entity.SmsTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsTemplateDao {
    @Query("SELECT * FROM templates ORDER BY isSelected DESC, updatedAt DESC")
    fun getAllTemplates(): Flow<List<SmsTemplateEntity>>

    @Query("SELECT * FROM templates WHERE isSelected = 1 LIMIT 1")
    fun getSelectedTemplate(): Flow<SmsTemplateEntity?>

    @Query("SELECT * FROM templates WHERE id = :id LIMIT 1")
    fun getTemplateById(id: Long): Flow<SmsTemplateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTemplate(template: SmsTemplateEntity): Long

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)

    @Query("UPDATE templates SET isSelected = 0")
    suspend fun clearSelectedTemplates()

    @Query("UPDATE templates SET isSelected = 1 WHERE id = :id")
    suspend fun markTemplateSelected(id: Long)

    @Transaction
    suspend fun setSelectedTemplate(selectedId: Long) {
        clearSelectedTemplates()
        markTemplateSelected(selectedId)
    }
}
