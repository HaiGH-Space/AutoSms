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
@Suppress("unused")
object DatabaseModule {

    // Note: Migrations prior to v4 (v2->v3, v3->v4) are deliberately omitted as versions < v0.1.0
    // were unreleased internal prototypes with breaking schema shifts; fallbackToDestructiveMigration handles them.
    internal val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE customers ADD COLUMN customFieldsJson TEXT NOT NULL DEFAULT '{}'")
        }
    }

    internal val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE customers ADD COLUMN sendSimSlot INTEGER DEFAULT 1")
            db.execSQL("UPDATE customers SET sendSimSlot = NULL WHERE isSelected = 0")
        }
    }

    internal val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_status` ON `customers` (`status`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_isSelected` ON `customers` (`isSelected`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_sendSimSlot` ON `customers` (`sendSimSlot`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_isSelected_sendSimSlot` ON `customers` (`isSelected`, `sendSimSlot`)")
        }
    }

    internal val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE templates ADD COLUMN isSelected INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE templates ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE templates SET isSelected = 1 WHERE id = 1")
        }
    }

    internal val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE sim_configs ADD COLUMN carrierName TEXT NOT NULL DEFAULT ''")
        }
    }

    internal val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE sending_rules ADD COLUMN startMinute INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE sending_rules ADD COLUMN endMinute INTEGER NOT NULL DEFAULT 0")
        }
    }

    internal val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_sending_logs_customerId` ON `sending_logs` (`customerId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_sending_logs_timestamp` ON `sending_logs` (`timestamp`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_sending_logs_status` ON `sending_logs` (`status`)")
        }
    }

    internal val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `zalo_recipients` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `phone` TEXT NOT NULL,
                    `isZaloFriend` INTEGER NOT NULL DEFAULT 0,
                    `status` TEXT NOT NULL DEFAULT 'PENDING',
                    `note` TEXT NOT NULL DEFAULT '',
                    `lastSentTime` INTEGER NOT NULL DEFAULT 0,
                    `customFieldsJson` TEXT NOT NULL DEFAULT '{}'
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_zalo_recipients_status` ON `zalo_recipients` (`status`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_zalo_recipients_phone` ON `zalo_recipients` (`phone`)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `zalo_sending_logs` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `recipientId` INTEGER NOT NULL,
                    `recipientName` TEXT NOT NULL,
                    `phone` TEXT NOT NULL,
                    `messageContent` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `status` TEXT NOT NULL,
                    `errorMessage` TEXT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_zalo_sending_logs_recipientId` ON `zalo_sending_logs` (`recipientId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_zalo_sending_logs_timestamp` ON `zalo_sending_logs` (`timestamp`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_zalo_sending_logs_status` ON `zalo_sending_logs` (`status`)")
        }
    }

    internal val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE sim_configs ADD COLUMN minDelaySec INTEGER NOT NULL DEFAULT 40")
            db.execSQL("ALTER TABLE sim_configs ADD COLUMN maxDelaySec INTEGER NOT NULL DEFAULT 50")
        }
    }

    internal val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `blacklist` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `phone` TEXT NOT NULL,
                    `rawPhone` TEXT NOT NULL,
                    `reason` TEXT NOT NULL DEFAULT '',
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_blacklist_phone` ON `blacklist` (`phone`)")
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13
    )

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "auto_sms_database"
        )
            .addMigrations(*ALL_MIGRATIONS)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()

    @Provides
    fun provideSmsTemplateDao(db: AppDatabase): SmsTemplateDao = db.smsTemplateDao()

    @Provides
    fun provideSimConfigDao(db: AppDatabase): SimConfigDao = db.simConfigDao()

    @Provides
    fun provideSendingLogDao(db: AppDatabase): SendingLogDao = db.sendingLogDao()

    @Provides
    fun provideZaloRecipientDao(db: AppDatabase): com.gmail.nthai.data.local.dao.ZaloRecipientDao =
        db.zaloRecipientDao()

    @Provides
    fun provideZaloSendingLogDao(db: AppDatabase): com.gmail.nthai.data.local.dao.ZaloSendingLogDao =
        db.zaloSendingLogDao()

    @Provides
    fun provideBlacklistDao(db: AppDatabase): com.gmail.nthai.data.local.dao.BlacklistDao =
        db.blacklistDao()
}
