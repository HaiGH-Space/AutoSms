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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "auto_sms_database"
        ).addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration().build()
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
