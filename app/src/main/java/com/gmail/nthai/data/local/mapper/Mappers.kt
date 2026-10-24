package com.gmail.nthai.data.local.mapper

import com.gmail.nthai.data.local.entity.CustomerEntity
import com.gmail.nthai.data.local.entity.SendingLogEntity
import com.gmail.nthai.data.local.entity.SendingRuleEntity
import com.gmail.nthai.data.local.entity.SimConfigEntity
import com.gmail.nthai.data.local.entity.SmsTemplateEntity
import com.gmail.nthai.domain.model.Customer
import com.gmail.nthai.domain.model.CustomerStatus
import com.gmail.nthai.domain.model.LogRecord
import com.gmail.nthai.domain.model.LogStatus
import com.gmail.nthai.domain.model.SendingRule
import com.gmail.nthai.domain.model.SimConfig
import com.gmail.nthai.domain.model.SmsTemplate

// Customer Mappers
fun CustomerEntity.toDomain(): Customer = Customer(
    id = id,
    name = name,
    contractId = contractId,
    amount = amount,
    dueDate = dueDate,
    phone = phone,
    status = try { CustomerStatus.valueOf(status) } catch (_: Exception) { CustomerStatus.PENDING }
)

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    id = id,
    name = name,
    contractId = contractId,
    amount = amount,
    dueDate = dueDate,
    phone = phone,
    status = status.name
)

// SmsTemplate Mappers
fun SmsTemplateEntity.toDomain(): SmsTemplate = SmsTemplate(
    id = id,
    title = title,
    content = content
)

fun SmsTemplate.toEntity(): SmsTemplateEntity = SmsTemplateEntity(
    id = id,
    title = title,
    content = content
)

// SimConfig Mappers
fun SimConfigEntity.toDomain(): SimConfig = SimConfig(
    subscriptionId = subscriptionId,
    simSlot = simSlot,
    dailyLimit = dailyLimit,
    sentCountToday = sentCountToday,
    lastResetDate = lastResetDate
)

fun SimConfig.toEntity(): SimConfigEntity = SimConfigEntity(
    simSlot = simSlot,
    subscriptionId = subscriptionId,
    dailyLimit = dailyLimit,
    sentCountToday = sentCountToday,
    lastResetDate = lastResetDate
)

// SendingRule Mappers
fun SendingRuleEntity.toDomain(): SendingRule = SendingRule(
    minDelaySec = minDelaySec,
    maxDelaySec = maxDelaySec,
    startHour = startHour,
    endHour = endHour
)

fun SendingRule.toEntity(): SendingRuleEntity = SendingRuleEntity(
    id = 1,
    minDelaySec = minDelaySec,
    maxDelaySec = maxDelaySec,
    startHour = startHour,
    endHour = endHour
)

// LogRecord Mappers
fun SendingLogEntity.toDomain(): LogRecord = LogRecord(
    id = id,
    customerId = customerId,
    customerName = customerName,
    phone = phone,
    messageContent = messageContent,
    simSlot = simSlot,
    timestamp = timestamp,
    status = try { LogStatus.valueOf(status) } catch (_: Exception) { LogStatus.PENDING },
    errorMessage = errorMessage
)

fun LogRecord.toEntity(): SendingLogEntity = SendingLogEntity(
    id = id,
    customerId = customerId,
    customerName = customerName,
    phone = phone,
    messageContent = messageContent,
    simSlot = simSlot,
    timestamp = timestamp,
    status = status.name,
    errorMessage = errorMessage
)
