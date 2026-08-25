package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.model.DriverBehaviourRequest
import com.example.model.DriverBehaviourResponse
import com.example.model.HealthPredictionRequest
import com.example.model.HealthPredictionResponse
import com.example.model.ServiceTicket
import com.example.model.SimulatorStatus
import com.example.model.TelemetryTick
import com.example.model.TicketStatus
import com.example.model.UrgencyLevel
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SharedTelemetryViewModel(
    private val repository: TelemetryRepository,
    private val ttsManager: com.example.utils.TtsManager
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

    private val _availableDatasets = MutableStateFlow<List<com.example.model.DatasetInfo>>(emptyList())
    val availableDatasets = _availableDatasets.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _inferenceError = MutableStateFlow<String?>(null)
    val inferenceError = _inferenceError.asStateFlow()

    private val _lastInferenceTimestamp = MutableStateFlow<Long?>(null)
    val lastInferenceTimestamp = _lastInferenceTimestamp.asStateFlow()

    private val _tickets = MutableStateFlow<List<ServiceTicket>>(emptyList())
    val tickets = _tickets.asStateFlow()

    private val _isTransmittingTicket = MutableStateFlow(false)
    val isTransmittingTicket = _isTransmittingTicket.asStateFlow()

    private val _telegramGatewayEnabled = MutableStateFlow(false)
    val telegramGatewayEnabled = _telegramGatewayEnabled.asStateFlow()

    private val _voiceAlertsEnabled = MutableStateFlow(true)
    val voiceAlertsEnabled = _voiceAlertsEnabled.asStateFlow()

    private val _userName = MutableStateFlow("John Doe")
    val userName = _userName.asStateFlow()

    private val _vehicleModel = MutableStateFlow("2024 Apex GT")
    val vehicleModel = _vehicleModel.asStateFlow()

    private var telemetryJob: Job? = null
    private var pollingJob: Job? = null
    private var lastTickTimestamp = 0L
    private var lastHealthPredictionTime = 0L
    private var lastBehaviourPredictionTime = 0L
    private var lastVoiceAlertTime = 0L

    init {
        startObserving()
        startPollingFallback()
        viewModelScope.launch {
            repository.latestTick.collectLatest { tick ->
                if (tick != null) {
                    lastTickTimestamp = System.currentTimeMillis()
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

                    checkAndSpeakAlerts(tick)
                }
            }
        }
        
        fetchSimulatorStatus()
        fetchDatasets()
        triggerInference()
    }

    private fun checkAndSpeakAlerts(tick: TelemetryTick) {
        if (!_voiceAlertsEnabled.value) return
        
        val now = System.currentTimeMillis()
        if (now - lastVoiceAlertTime < 15000) return // Debounce alerts

        val d = tick.data
        var alertMsg: String? = null
        
        if (d.vss > 120) {
            alertMsg = "Warning: Speed limit exceeded. Current speed is ${d.vss.toInt()} kilometers per hour."
        } else if (d.rpm > 6000) {
            alertMsg = "Warning: High engine RPM detected. RPM is at ${d.rpm.toInt()}."
        } else if (d.coolantTemp > 100) {
            alertMsg = "Critical Warning: Engine coolant temperature is too high. Please pull over safely."
        }
        
        if (alertMsg != null) {
            ttsManager.speak(alertMsg)
            lastVoiceAlertTime = now
        }
    }

    fun toggleVoiceAlerts(enabled: Boolean) {
        _voiceAlertsEnabled.value = enabled
    }

    fun updateUserName(name: String) {
        _userName.value = name
    }

    fun updateVehicleModel(model: String) {
        _vehicleModel.value = model
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

    private fun startPollingFallback() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                // If we haven't received a tick in the last 1200ms, fetch via REST polling
                if (now - lastTickTimestamp > 1200) {
                    val result = repository.getLiveData()
                    if (result.isSuccess && result.getOrNull() != null) {
                        lastTickTimestamp = System.currentTimeMillis()
                    } else {
                        // Backend might be sleeping or simulator paused; attempt start
                        repository.startSimulation()
                    }
                }
                delay(1000)
            }
        }
    }

    fun fetchDatasets() {
        viewModelScope.launch {
            val result = repository.getDatasets()
            if (result.isSuccess) {
                _availableDatasets.value = result.getOrNull()?.datasets ?: emptyList()
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

    fun pauseSimulation() {
        viewModelScope.launch {
            repository.pauseSimulation()
            fetchSimulatorStatus()
        }
    }

    fun resumeSimulation() {
        viewModelScope.launch {
            repository.resumeSimulation()
            fetchSimulatorStatus()
        }
    }

    fun stopSimulation() {
        viewModelScope.launch {
            repository.stopSimulation()
            fetchSimulatorStatus()
        }
    }

    fun changeDataset(datasetId: String) {
        viewModelScope.launch {
            repository.changeDataset(datasetId)
            fetchSimulatorStatus()
        }
    }

    fun uploadDataset(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                var fileName = "uploaded_dataset.csv"
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex)
                        }
                    }
                }

                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch
                
                val mediaType = "multipart/form-data".toMediaTypeOrNull()
                val requestBody = bytes.toRequestBody(mediaType)
                val part = okhttp3.MultipartBody.Part.createFormData("file", fileName, requestBody)
                
                val result = repository.uploadDataset(part)
                if (result.isSuccess) {
                    fetchDatasets()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pingBackend() {
        viewModelScope.launch {
            repository.pingBackend()
            startObserving()
            fetchSimulatorStatus()
            fetchDatasets()
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

    fun submitServiceTicket(
        faultCode: String,
        description: String,
        urgency: UrgencyLevel,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isTransmittingTicket.value = true
            delay(1000) // Simulate transmission delay over secure ECU channel
            val timeString = SimpleDateFormat("HH:mm:ss - MMM dd", Locale.getDefault()).format(Date())
            val randomId = "TCK-${(1000..9999).random()}"
            val newTicket = ServiceTicket(
                id = randomId,
                timestamp = timeString,
                faultCode = if (faultCode.isBlank()) "No DTC (Custom Issue)" else faultCode,
                description = description,
                urgency = urgency,
                servicePartner = "Apex Auto Services",
                status = TicketStatus.TRANSMITTED
            )
            _tickets.value = listOf(newTicket) + _tickets.value
            _isTransmittingTicket.value = false
            onSuccess()
        }
    }

    fun toggleTelegramGateway(enabled: Boolean) {
        _telegramGatewayEnabled.value = enabled
    }

    fun resolveTicket(ticketId: String) {
        _tickets.value = _tickets.value.map {
            if (it.id == ticketId) it.copy(status = TicketStatus.RESOLVED) else it
        }
    }

    fun deleteTicket(ticketId: String) {
        _tickets.value = _tickets.value.filterNot { it.id == ticketId }
    }

    companion object {
        fun provideFactory(repository: TelemetryRepository, ttsManager: com.example.utils.TtsManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SharedTelemetryViewModel(repository, ttsManager) as T
                }
            }
    }
}
