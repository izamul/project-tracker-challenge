package com.aptavis.projecttrackerchallenge.di

import android.content.Context
import androidx.room.Room
import com.aptavis.projecttrackerchallenge.data.db.AppDb
import com.aptavis.projecttrackerchallenge.data.db.dao.ProjectDao
import com.aptavis.projecttrackerchallenge.data.db.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDb =
        Room.databaseBuilder(ctx, AppDb::class.java, "project_tracker.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideProjectDao(db: AppDb): ProjectDao = db.projectDao()
    @Provides fun provideTaskDao(db: AppDb): TaskDao = db.taskDao()
}
