package com.gmail.nthai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gmail.nthai.data.local.entity.SmsTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsTemplateDao {
    @Query("SELECT * FROM templates WHERE id = 1 LIMIT 1")
    fun getTemplate(): Flow<SmsTemplateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTemplate(template: SmsTemplateEntity)
}
