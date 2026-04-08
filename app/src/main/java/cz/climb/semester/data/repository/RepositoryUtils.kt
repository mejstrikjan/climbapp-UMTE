package cz.climb.semester.data.repository

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

internal fun generateEntityId(): String = UUID.randomUUID().toString()

internal fun nowIsoString(): String = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

internal fun todayStoredDate(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
