package com.nabila.storyappdicoding.ui.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nabila.storyappdicoding.di.Injection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SaveSessionWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Log.d("SaveSessionWorker", "doWork() started")
        val userRepository = Injection.provideRepository(applicationContext)
        val session = runBlocking { userRepository.getSession().first() }
        runBlocking {
            userRepository.saveSession(session)
        }
        Log.d("SaveSessionWorker", "Session data saved to DataStore: $session")
        Log.d("SaveSessionWorker", "doWork() finished")

        return Result.success()
    }
}