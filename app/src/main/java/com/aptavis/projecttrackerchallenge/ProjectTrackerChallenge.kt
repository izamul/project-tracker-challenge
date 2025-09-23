// app/src/main/java/com/aptavis/projecttrackerchallenge/ProjectTrackerChallenge.kt
package com.aptavis.projecttrackerchallenge

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ProjectTrackerChallenge : Application() {

    override fun onCreate() {
        super.onCreate()
    }


}