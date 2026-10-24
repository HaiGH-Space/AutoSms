package com.gmail.nthai.di

import com.gmail.nthai.data.excel.ExcelExporter
import com.gmail.nthai.data.excel.ExcelParser
import com.gmail.nthai.domain.usecase.CalculateNextDelayUseCase
import com.gmail.nthai.domain.usecase.ExportReportExcelUseCase
import com.gmail.nthai.domain.usecase.ImportCustomerExcelUseCase
import com.gmail.nthai.domain.usecase.ParseSpintaxUseCase
import com.gmail.nthai.domain.usecase.ValidateSendingConstraintUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideParseSpintaxUseCase(): ParseSpintaxUseCase = ParseSpintaxUseCase()

    @Provides
    @Singleton
    fun provideValidateSendingConstraintUseCase(): ValidateSendingConstraintUseCase = ValidateSendingConstraintUseCase()

    @Provides
    @Singleton
    fun provideCalculateNextDelayUseCase(): CalculateNextDelayUseCase = CalculateNextDelayUseCase()

    @Provides
    @Singleton
    fun provideImportCustomerExcelUseCase(excelParser: ExcelParser): ImportCustomerExcelUseCase {
        return ImportCustomerExcelUseCase(excelParser)
    }

    @Provides
    @Singleton
    fun provideExportReportExcelUseCase(excelExporter: ExcelExporter): ExportReportExcelUseCase {
        return ExportReportExcelUseCase(excelExporter)
    }
}
