package com.example.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.network.AutoVueApi
import com.example.network.TelemetryWebSocket
import com.example.repository.TelemetryRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(private val context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("settings") }
    )
    
    val baseUrlKey = stringPreferencesKey("base_url")

    // Default to the provided Render URL if not set
    private val defaultBaseUrl = "https://ecu-backend-95fz.onrender.com/"

    val baseUrl: String = defaultBaseUrl

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: AutoVueApi = retrofit.create(AutoVueApi::class.java)
    val webSocket = TelemetryWebSocket(okHttpClient, moshi)
    
    val ttsManager = com.example.utils.TtsManager(context)
    val telemetryRepository = TelemetryRepository(api, webSocket, baseUrl)
}
