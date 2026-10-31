package com.gmail.nthai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gmail.nthai.data.local.dao.CustomerDao
import com.gmail.nthai.data.local.dao.SendingLogDao
import com.gmail.nthai.data.local.dao.SimConfigDao
import com.gmail.nthai.data.local.dao.SmsTemplateDao
import com.gmail.nthai.data.local.entity.CustomerEntity
import com.gmail.nthai.data.local.entity.SendingLogEntity
import com.gmail.nthai.data.local.entity.SendingRuleEntity
import com.gmail.nthai.data.local.entity.SimConfigEntity
import com.gmail.nthai.data.local.entity.SmsTemplateEntity

@Database(
    entities = [
        CustomerEntity::class,
        SmsTemplateEntity::class,
        SimConfigEntity::class,
        SendingRuleEntity::class,
        SendingLogEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun smsTemplateDao(): SmsTemplateDao
    abstract fun simConfigDao(): SimConfigDao
    abstract fun sendingLogDao(): SendingLogDao
}
