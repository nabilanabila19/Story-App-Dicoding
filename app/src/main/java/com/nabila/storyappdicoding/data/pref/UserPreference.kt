package com.nabila.storyappdicoding.data.pref

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import androidx.core.content.edit as editSP

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference private constructor(private val context: Context) {

    private val sharedPreferences by lazy {
        context.getSharedPreferences("session", MODE_PRIVATE)
    }

    suspend fun saveSession(user: UserModel) {
        withContext(Dispatchers.IO) {
            sharedPreferences.editSP {
                putString(EMAIL_KEY, user.email)
                putString(TOKEN_KEY, user.token)
                putBoolean(IS_LOGIN_KEY, true)
            }
        }
        Log.d("UserPreference", "Session saved: $user")
    }

    fun getSession(): Flow<UserModel> {
        return flow {
            val userModel = UserModel(
                sharedPreferences.getString(EMAIL_KEY, "") ?: "",
                sharedPreferences.getString(TOKEN_KEY, "") ?: "",
                sharedPreferences.getBoolean(IS_LOGIN_KEY, false)
            )
            Log.d("UserPreference", "Session retrieved: $userModel")
            emit(userModel)
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            sharedPreferences.editSP { clear() }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private const val EMAIL_KEY = "email"
        private const val TOKEN_KEY = "token"
        private const val IS_LOGIN_KEY = "isLogin"

        fun getInstance(context: Context): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(context)
                INSTANCE = instance
                instance
            }
        }
    }
}