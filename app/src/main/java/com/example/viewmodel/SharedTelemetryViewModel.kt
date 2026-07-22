package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.model.DriverBehaviourRequest
import com.example.model.DriverBehaviourResponse
import com.example.model.HealthPredictionRequest
import com.example.model.HealthPredictionResponse
import com.example.model.SimulatorStatus
import com.example.model.TelemetryTick
import com.example.repository.ConnectionStatus
import com.example.repository.TelemetryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SharedTelemetryViewModel(
    private val repository: TelemetryRepository
) : ViewModel() {

    val connectionStatus = repository.connectionStatus
    
    val latestTick = repository.latestTick.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _history = MutableStateFlow<List<TelemetryTick>>(emptyList())
    val history = _history.asStateFlow()

    private val _healthPrediction = MutableStateFlow<HealthPredictionResponse?>(null)
    val healthPrediction = _healthPrediction.asStateFlow()

    private val _driverBehaviour = MutableStateFlow<DriverBehaviourResponse?>(null)
    val driverBehaviour = _driverBehaviour.asStateFlow()

    private val _simulatorStatus = MutableStateFlow<SimulatorStatus?>(null)
    val simulatorStatus = _simulatorStatus.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _inferenceError = MutableStateFlow<String?>(null)
    val inferenceError = _inferenceError.asStateFlow()

    private val _lastInferenceTimestamp = MutableStateFlow<Long?>(null)
    val lastInferenceTimestamp = _lastInferenceTimestamp.asStateFlow()

    private var telemetryJob: Job? = null
    private var lastHealthPredictionTime = 0L
    private var lastBehaviourPredictionTime = 0L

    init {
        startObserving()
        viewModelScope.launch {
            repository.latestTick.collectLatest { tick ->
                if (tick != null) {
                    val currentHistory = _history.value.toMutableList()
                    currentHistory.add(tick)
                    // Keep max 300 items for 5 mins of 1 tick/sec (assuming 1 tick/sec)
                    if (currentHistory.size > 300) {
                        currentHistory.removeAt(0)
                    }
                    _history.value = currentHistory
                    
                    // Periodically predict health (e.g. every 5 seconds)
                    val now = System.currentTimeMillis()
                    if (now - lastHealthPredictionTime > 5000) {
                        lastHealthPredictionTime = now
                        predictHealth(tick)
                    }
                    
                    // Periodically predict driver behaviour (needs 5 points)
                    if (now - lastBehaviourPredictionTime > 10000 && currentHistory.size >= 5) {
                        lastBehaviourPredictionTime = now
                        predictBehaviour(currentHistory.takeLast(10))
                    }
                }
            }
        }
        
        fetchSimulatorStatus()
        triggerInference()
    }

    fun triggerInference() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _inferenceError.value = null

            try {
                // Ensure we have history data
                var historyData = _history.value
                if (historyData.size < 5) {
                    val historyResult = repository.getHistory(10)
                    if (historyResult.isSuccess && !historyResult.getOrNull().isNullOrEmpty()) {
                        historyData = historyResult.getOrNull()!!
                        _history.value = historyData
                    }
                }

                // Get latest tick either from history or live-data REST call
                var targetTick = latestTick.value ?: historyData.lastOrNull()
                if (targetTick == null) {
                    val liveResult = repository.getLiveData()
                    if (liveResult.isSuccess) {
                        targetTick = liveResult.getOrNull()
                    }
                }

                if (targetTick != null) {
                    predictHealth(targetTick)
                }

                if (historyData.isNotEmpty()) {
                    predictBehaviour(historyData.takeLast(10))
                } else if (targetTick != null) {
                    // Fallback if history is empty: construct a list from targetTick
                    predictBehaviour(listOf(targetTick, targetTick, targetTick, targetTick, targetTick))
                }

                if (_healthPrediction.value == null && _driverBehaviour.value == null) {
                    _inferenceError.value = "Failed to obtain ML predictions. Make sure the backend server is reachable."
                } else {
                    _lastInferenceTimestamp.value = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _inferenceError.value = "Inference error: ${e.localizedMessage ?: "Network error"}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    private fun startObserving() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (isActive) {
                try {
                    repository.observeTelemetry().collectLatest { }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(3000)
            }
        }
    }

    private fun fetchSimulatorStatus() {
        viewModelScope.launch {
            val result = repository.getStatus()
            if (result.isSuccess) {
                _simulatorStatus.value = result.getOrNull()
            }
        }
    }

    fun startSimulation() {
        viewModelScope.launch {
            repository.startSimulation()
            startObserving()
            fetchSimulatorStatus()
        }
    }

    fun pingBackend() {
        viewModelScope.launch {
            repository.pingBackend()
            startObserving()
            fetchSimulatorStatus()
            triggerInference()
        }
    }

    private suspend fun predictHealth(tick: TelemetryTick) {
        val request = HealthPredictionRequest(
            rpm = tick.data.rpm,
            throttlePos = tick.data.throttlePos,
            mapKpa = tick.data.mapKpa,
            maf = tick.data.maf,
            coolantTemp = tick.data.coolantTemp,
            intakeAirTemp = tick.data.intakeAirTemp,
            ambientTemp = tick.data.ambientTemp,
            pedalD = tick.data.pedalD
        )
        val result = repository.predictHealth(request)
        if (result.isSuccess) {
            _healthPrediction.value = result.getOrNull()
        } else {
            result.exceptionOrNull()?.printStackTrace()
        }
    }
    
    private suspend fun predictBehaviour(ticks: List<TelemetryTick>) {
        val request = DriverBehaviourRequest(
            rpmValues = ticks.map { it.data.rpm },
            speedValues = ticks.map { it.data.vss },
            throttleValues = ticks.map { it.data.throttlePos }
        )
        val result = repository.predictDriverBehaviour(request)
        if (result.isSuccess) {
            _driverBehaviour.value = result.getOrNull()
        } else {
            result.exceptionOrNull()?.printStackTrace()
        }
    }

    companion object {
        fun provideFactory(repository: TelemetryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SharedTelemetryViewModel(repository) as T
                }
            }
    }
}
