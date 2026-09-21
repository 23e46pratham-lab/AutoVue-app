package com.example.repository

import com.example.model.ChangeDatasetRequest
import com.example.model.DatasetListResponse
import com.example.model.DriverBehaviourRequest
import com.example.model.DriverBehaviourResponse
import com.example.model.FuelPredictionRequest
import com.example.model.FuelPredictionResponse
import com.example.model.HealthPredictionRequest
import com.example.model.HealthPredictionResponse
import com.example.model.HistoryResponse
import com.example.model.LoopRequest
import com.example.model.MlInferencePayload
import com.example.model.SimulatorStatus
import com.example.model.SpeedRequest
import com.example.model.StartSimulationRequest
import com.example.model.TelemetryTick
import com.example.network.AutoVueApi
import com.example.network.TelemetryWebSocket
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach

class TelemetryRepository(
    private val api: AutoVueApi,
    private val webSocket: TelemetryWebSocket,
    private val baseUrl: String,
    private val moshi: Moshi
) {
    private val _latestTick = MutableStateFlow<TelemetryTick?>(null)
    val latestTick: StateFlow<TelemetryTick?> = _latestTick.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    fun observeTelemetry(): Flow<TelemetryTick> {
        _connectionStatus.value = ConnectionStatus.CONNECTING
        return webSocket.connect(baseUrl)
            .onEach { 
                _latestTick.value = it 
                _connectionStatus.value = ConnectionStatus.CONNECTED
            }
            .catch { 
                _connectionStatus.value = ConnectionStatus.ERROR 
                throw it
            }
    }

    suspend fun getStatus(): Result<SimulatorStatus> = runCatching {
        api.getStatus()
    }

    suspend fun getLiveData(): Result<TelemetryTick> = runCatching {
        val tick = api.getLiveData()
        _latestTick.value = tick
        _connectionStatus.value = ConnectionStatus.CONNECTED
        tick
    }

    suspend fun getHistory(limit: Int = 100): Result<List<TelemetryTick>> = runCatching {
        val responseBody = api.getHistory(limit).string().trim()
        if (responseBody.startsWith("[")) {
            val type = Types.newParameterizedType(List::class.java, TelemetryTick::class.java)
            val adapter: JsonAdapter<List<TelemetryTick>> = moshi.adapter(type)
            adapter.fromJson(responseBody) ?: emptyList()
        } else {
            val adapter: JsonAdapter<HistoryResponse> = moshi.adapter(HistoryResponse::class.java)
            val obj = adapter.fromJson(responseBody)
            obj?.history ?: emptyList()
        }
    }

    suspend fun getLatestMl(): Result<MlInferencePayload> = runCatching {
        api.getLatestMl()
    }

    suspend fun startSimulation(datasetId: String? = null, speed: Double? = null, loop: Boolean? = null): Result<Unit> = runCatching {
        api.startSimulation(StartSimulationRequest(datasetId, speed, loop))
        Unit
    }

    suspend fun pauseSimulation(): Result<Unit> = runCatching {
        api.pauseSimulation()
        Unit
    }

    suspend fun resumeSimulation(): Result<Unit> = runCatching {
        api.resumeSimulation()
        Unit
    }

    suspend fun stopSimulation(): Result<Unit> = runCatching {
        api.stopSimulation()
        Unit
    }

    suspend fun resetSimulation(): Result<Unit> = runCatching {
        api.resetSimulation()
        Unit
    }

    suspend fun setSpeed(speed: Double): Result<Unit> = runCatching {
        api.setSpeed(SpeedRequest(speed))
        Unit
    }

    suspend fun setLoop(loop: Boolean): Result<Unit> = runCatching {
        api.setLoop(LoopRequest(loop))
        Unit
    }

    suspend fun getDatasets(): Result<DatasetListResponse> = runCatching {
        api.getDatasets()
    }

    suspend fun changeDataset(datasetId: String): Result<Unit> = runCatching {
        api.changeDataset(ChangeDatasetRequest(datasetId))
        Unit
    }

    suspend fun uploadDataset(file: okhttp3.MultipartBody.Part): Result<Any> = runCatching {
        api.uploadDataset(file)
    }

    suspend fun predictHealth(request: HealthPredictionRequest): Result<HealthPredictionResponse> = runCatching {
        api.predictHealth(request)
    }

    suspend fun predictDriverBehaviour(request: DriverBehaviourRequest): Result<DriverBehaviourResponse> = runCatching {
        api.predictDriverBehaviour(request)
    }

    suspend fun predictFuel(request: FuelPredictionRequest): Result<FuelPredictionResponse> = runCatching {
        api.predictFuel(request)
    }

    suspend fun pingBackend(): Result<Any> = runCatching {
        api.pingHealth()
    }
}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}
