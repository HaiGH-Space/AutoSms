package com.gmail.nthai.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gmail.nthai.data.local.AppDatabase
import com.gmail.nthai.data.local.dao.CustomerDao
import com.gmail.nthai.data.local.dao.SendingLogDao
import com.gmail.nthai.data.local.dao.SimConfigDao
import com.gmail.nthai.data.local.dao.SmsTemplateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE customers ADD COLUMN customFieldsJson TEXT NOT NULL DEFAULT '{}'")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE customers ADD COLUMN sendSimSlot INTEGER DEFAULT 1")
            db.execSQL("UPDATE customers SET sendSimSlot = NULL WHERE isSelected = 0")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_status` ON `customers` (`status`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_isSelected` ON `customers` (`isSelected`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_sendSimSlot` ON `customers` (`sendSimSlot`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_isSelected_sendSimSlot` ON `customers` (`isSelected`, `sendSimSlot`)")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "auto_sms_database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_4_5, MIGRATION_5_6).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()

    @Provides
    fun provideSmsTemplateDao(db: AppDatabase): SmsTemplateDao = db.smsTemplateDao()

    @Provides
    fun provideSimConfigDao(db: AppDatabase): SimConfigDao = db.simConfigDao()

    @Provides
    fun provideSendingLogDao(db: AppDatabase): SendingLogDao = db.sendingLogDao()
}
