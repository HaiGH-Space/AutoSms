package com.gmail.nthai.di

import com.gmail.nthai.data.local.dao.ZaloRecipientDao
import com.gmail.nthai.data.local.dao.ZaloSendingLogDao
import com.gmail.nthai.data.repository.ZaloRepositoryImpl
import com.gmail.nthai.domain.repository.ZaloRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ZaloModule {

    @Provides
    @Singleton
    fun provideZaloRepository(
        zaloRecipientDao: ZaloRecipientDao,
        zaloSendingLogDao: ZaloSendingLogDao
    ): ZaloRepository {
        return ZaloRepositoryImpl(zaloRecipientDao, zaloSendingLogDao)
    }
}
