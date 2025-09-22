package com.aptavis.projecttrackerchallenge.di

import com.aptavis.projecttrackerchallenge.data.db.AppDb
import com.aptavis.projecttrackerchallenge.data.repository.ProjectRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun provideProjectRepository(db: AppDb): ProjectRepository =
        ProjectRepository(db)
}
