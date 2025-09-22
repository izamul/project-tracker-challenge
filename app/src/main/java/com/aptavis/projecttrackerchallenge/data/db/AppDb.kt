package com.aptavis.projecttrackerchallenge.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aptavis.projecttrackerchallenge.data.db.dao.ProjectDao
import com.aptavis.projecttrackerchallenge.data.db.dao.TaskDao
import com.aptavis.projecttrackerchallenge.data.db.entity.ProjectEntity
import com.aptavis.projecttrackerchallenge.data.db.entity.TaskEntity

@Database(
    entities = [ProjectEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
}
