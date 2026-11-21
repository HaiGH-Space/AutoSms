package com.gmail.nthai.di

import com.gmail.nthai.data.local.dao.CallLogDao
import com.gmail.nthai.data.local.dao.CallRecipientDao
import com.gmail.nthai.data.repository.AutoCallRepositoryImpl
import com.gmail.nthai.data.repository.CallRecipientRepositoryImpl
import com.gmail.nthai.domain.repository.AutoCallRepository
import com.gmail.nthai.domain.repository.CallRecipientRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AutoCallModule {

    @Provides
    @Singleton
    fun provideAutoCallRepository(callLogDao: CallLogDao): AutoCallRepository {
        return AutoCallRepositoryImpl(callLogDao)
    }

    @Provides
    @Singleton
    fun provideCallRecipientRepository(callRecipientDao: CallRecipientDao): CallRecipientRepository {
        return CallRecipientRepositoryImpl(callRecipientDao)
    }
}

