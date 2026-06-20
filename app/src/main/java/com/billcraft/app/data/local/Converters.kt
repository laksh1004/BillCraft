package com.billcraft.app.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toLocalDate(epochMilli: Long?): LocalDate? {
        return epochMilli?.let {
            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
        }
    }

    @TypeConverter
    fun fromEpochMilli(value: Long?): Long? = value

    @TypeConverter
    fun toEpochMilli(value: Long?): Long? = value
}
