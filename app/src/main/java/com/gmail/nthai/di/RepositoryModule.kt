package com.gmail.nthai.di

import com.gmail.nthai.data.local.dao.CustomerDao
import com.gmail.nthai.data.local.dao.SendingLogDao
import com.gmail.nthai.data.local.dao.SimConfigDao
import com.gmail.nthai.data.local.dao.SmsTemplateDao
import com.gmail.nthai.data.repository.CustomerRepositoryImpl
import com.gmail.nthai.data.repository.SendingLogRepositoryImpl
import com.gmail.nthai.data.repository.SimConfigRepositoryImpl
import com.gmail.nthai.data.repository.TemplateRepositoryImpl
import com.gmail.nthai.domain.repository.CustomerRepository
import com.gmail.nthai.domain.repository.SendingLogRepository
import com.gmail.nthai.domain.repository.SimConfigRepository
import com.gmail.nthai.domain.repository.TemplateRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCustomerRepository(customerDao: CustomerDao): CustomerRepository {
        return CustomerRepositoryImpl(customerDao)
    }

    @Provides
    @Singleton
    fun provideTemplateRepository(smsTemplateDao: SmsTemplateDao): TemplateRepository {
        return TemplateRepositoryImpl(smsTemplateDao)
    }

    @Provides
    @Singleton
    fun provideSimConfigRepository(simConfigDao: SimConfigDao): SimConfigRepository {
        return SimConfigRepositoryImpl(simConfigDao)
    }

    @Provides
    @Singleton
    fun provideSendingLogRepository(sendingLogDao: SendingLogDao): SendingLogRepository {
        return SendingLogRepositoryImpl(sendingLogDao)
    }
}
