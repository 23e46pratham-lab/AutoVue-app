package com.example.repository

import com.example.model.DriverBehaviourRequest
import com.example.model.DriverBehaviourResponse
import com.example.model.HealthPredictionRequest
import com.example.model.HealthPredictionResponse
import com.example.model.SimulatorStatus
import com.example.model.TelemetryTick
import com.example.network.AutoVueApi
import com.example.network.TelemetryWebSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach

class TelemetryRepository(
    private val api: AutoVueApi,
    private val webSocket: TelemetryWebSocket,
    private val baseUrl: String
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

    suspend fun getHistory(limit: Int = 10): Result<List<TelemetryTick>> = runCatching {
        api.getHistory(limit).history
    }

    suspend fun startSimulation(): Result<Unit> = runCatching {
        api.startSimulation()
    }

    suspend fun pauseSimulation(): Result<Unit> = runCatching {
        api.pauseSimulation()
    }

    suspend fun resumeSimulation(): Result<Unit> = runCatching {
        api.resumeSimulation()
    }

    suspend fun stopSimulation(): Result<Unit> = runCatching {
        api.stopSimulation()
    }

    suspend fun getDatasets(): Result<com.example.model.DatasetListResponse> = runCatching {
        api.getDatasets()
    }

    suspend fun changeDataset(datasetId: String): Result<Unit> = runCatching {
        api.changeDataset(com.example.model.ChangeDatasetRequest(datasetId))
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

    suspend fun pingBackend(): Result<Any> = runCatching {
        api.pingHealth()
    }
}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}
