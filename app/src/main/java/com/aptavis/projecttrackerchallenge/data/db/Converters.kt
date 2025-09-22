package com.aptavis.projecttrackerchallenge.data.db

import androidx.room.TypeConverter
import com.aptavis.projecttrackerchallenge.domain.model.Status

class Converters {
    @TypeConverter fun toStatus(name: String): Status = enumValueOf(name)
    @TypeConverter fun fromStatus(s: Status): String = s.name
}
