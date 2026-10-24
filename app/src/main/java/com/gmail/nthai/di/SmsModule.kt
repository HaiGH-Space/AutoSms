package com.gmail.nthai.di

import android.content.Context
import com.gmail.nthai.data.excel.ExcelExporter
import com.gmail.nthai.data.excel.ExcelParser
import com.gmail.nthai.data.sms.SimManagerWrapper
import com.gmail.nthai.data.sms.SmsSenderEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SmsModule {

    @Provides
    @Singleton
    fun provideSimManagerWrapper(@ApplicationContext context: Context): SimManagerWrapper {
        return SimManagerWrapper(context)
    }

    @Provides
    @Singleton
    fun provideSmsSenderEngine(@ApplicationContext context: Context): SmsSenderEngine {
        return SmsSenderEngine(context)
    }

    @Provides
    @Singleton
    fun provideExcelParser(): ExcelParser = ExcelParser()

    @Provides
    @Singleton
    fun provideExcelExporter(): ExcelExporter = ExcelExporter()
}
