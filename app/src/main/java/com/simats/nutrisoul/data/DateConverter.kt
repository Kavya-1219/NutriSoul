package com.simats.nutrisoul.data

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromLongToFirebaseTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(Date(it)) }
    }

    @TypeConverter
    fun firebaseTimestampToLong(timestamp: Timestamp?): Long? {
        return timestamp?.toDate()?.time
    }
}
