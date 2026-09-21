package com.example.network

import com.example.model.ChangeDatasetRequest
import com.example.model.DatasetListResponse
import com.example.model.DriverBehaviourRequest
import com.example.model.DriverBehaviourResponse
import com.example.model.FuelPredictionRequest
import com.example.model.FuelPredictionResponse
import com.example.model.HealthPredictionRequest
import com.example.model.HealthPredictionResponse
import com.example.model.LoopRequest
import com.example.model.MlInferencePayload
import com.example.model.SimulatorStatus
import com.example.model.SpeedRequest
import com.example.model.StartSimulationRequest
import com.example.model.TelemetryTick
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface AutoVueApi {
    @GET("api/status")
    suspend fun getStatus(): SimulatorStatus

    @GET("api/live-data")
    suspend fun getLiveData(): TelemetryTick

    @GET("api/history")
    suspend fun getHistory(@Query("limit") limit: Int = 100): ResponseBody

    @GET("api/ml/latest")
    suspend fun getLatestMl(): MlInferencePayload

    @POST("api/health/predict")
    suspend fun predictHealth(@Body request: HealthPredictionRequest): HealthPredictionResponse

    @POST("api/driver/predict")
    suspend fun predictDriverBehaviour(@Body request: DriverBehaviourRequest): DriverBehaviourResponse

    @POST("api/fuel/predict")
    suspend fun predictFuel(@Body request: FuelPredictionRequest): FuelPredictionResponse
    
    @POST("api/start")
    suspend fun startSimulation(@Body request: StartSimulationRequest = StartSimulationRequest()): Any
    
    @POST("api/pause")
    suspend fun pauseSimulation(): Any

    @POST("api/resume")
    suspend fun resumeSimulation(): Any

    @POST("api/stop")
    suspend fun stopSimulation(): Any

    @POST("api/reset")
    suspend fun resetSimulation(): Any

    @POST("api/speed")
    suspend fun setSpeed(@Body request: SpeedRequest): Any

    @POST("api/loop")
    suspend fun setLoop(@Body request: LoopRequest): Any

    @GET("api/datasets")
    suspend fun getDatasets(): DatasetListResponse

    @POST("api/change-dataset")
    suspend fun changeDataset(@Body request: ChangeDatasetRequest): Any

    @Multipart
    @POST("api/upload")
    suspend fun uploadDataset(@Part file: MultipartBody.Part): Any

    @GET("health")
    suspend fun pingHealth(): Any
}
