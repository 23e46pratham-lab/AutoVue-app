package com.example.network

import com.example.model.DriverBehaviourRequest
import com.example.model.DriverBehaviourResponse
import com.example.model.HealthPredictionRequest
import com.example.model.HealthPredictionResponse
import com.example.model.HistoryResponse
import com.example.model.SimulatorStatus
import com.example.model.TelemetryTick
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AutoVueApi {
    @GET("api/status")
    suspend fun getStatus(): SimulatorStatus

    @GET("api/live-data")
    suspend fun getLiveData(): TelemetryTick

    @GET("api/history")
    suspend fun getHistory(@Query("limit") limit: Int = 10): HistoryResponse

    @POST("api/health/predict")
    suspend fun predictHealth(@Body request: HealthPredictionRequest): HealthPredictionResponse

    @POST("api/driver/predict")
    suspend fun predictDriverBehaviour(@Body request: DriverBehaviourRequest): DriverBehaviourResponse
    
    @POST("api/start")
    suspend fun startSimulation()
    
    @POST("api/pause")
    suspend fun pauseSimulation()

    @POST("api/resume")
    suspend fun resumeSimulation()

    @POST("api/stop")
    suspend fun stopSimulation()

    @GET("api/datasets")
    suspend fun getDatasets(): com.example.model.DatasetListResponse

    @POST("api/change-dataset")
    suspend fun changeDataset(@Body request: com.example.model.ChangeDatasetRequest)

    @retrofit2.http.Multipart
    @POST("api/upload")
    suspend fun uploadDataset(@retrofit2.http.Part file: okhttp3.MultipartBody.Part): Any

    @GET("health")
    suspend fun pingHealth(): Any
}
