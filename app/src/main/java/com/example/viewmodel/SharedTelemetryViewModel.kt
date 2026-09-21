package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.model.DriverBehaviourRequest
import com.example.model.DriverBehaviourResponse
import com.example.model.FuelPredictionRequest
import com.example.model.FuelPredictionResponse
import com.example.model.FuelSensorTick
import com.example.model.HealthPredictionRequest
import com.example.model.HealthPredictionResponse
import com.example.model.HealthSensorTick
import com.example.model.ServiceTicket
import com.example.model.SimulatorStatus
import com.example.model.TelemetryTick
import com.example.model.TicketStatus
import com.example.model.UrgencyLevel
import com.example.repository.ConnectionStatus
import com.example.repository.TelemetryRepository
import com.example.model.VehicleProfile
import com.example.model.RefuelingEntry
import com.example.model.TripHistoryItem
import com.example.model.VehicleEventLog
import com.example.model.DtcCode
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _fuelPrediction = MutableStateFlow<FuelPredictionResponse?>(null)
    val fuelPrediction = _fuelPrediction.asStateFlow()

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

    private val _visualAlertsEnabled = MutableStateFlow(true)
    val visualAlertsEnabled = _visualAlertsEnabled.asStateFlow()

    private val _notificationSettings = MutableStateFlow(com.example.model.NotificationSettings())
    val notificationSettings = _notificationSettings.asStateFlow()

    private val _activeAlert = MutableStateFlow<com.example.model.ActiveAlert?>(null)
    val activeAlert = _activeAlert.asStateFlow()

    private val _detectedAnomalies = MutableStateFlow<List<com.example.model.AnomalyRecord>>(emptyList())
    val detectedAnomalies = _detectedAnomalies.asStateFlow()

    private val _visualAlertEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>(extraBufferCapacity = 1)
    val visualAlertEvent = _visualAlertEvent.asSharedFlow()

    private val _userName = MutableStateFlow("Pratham")
    val userName = _userName.asStateFlow()

    private val _vehicleModel = MutableStateFlow("Seat Leon 1.6 FSI")
    val vehicleModel = _vehicleModel.asStateFlow()

    private val _userProfiles = MutableStateFlow<List<com.example.model.UserProfileItem>>(
        listOf(
            com.example.model.UserProfileItem("u1", "Pratham", "Seat Leon 1.6 FSI", isDefault = true),
            com.example.model.UserProfileItem("u2", "Alex", "Volkswagen Golf 2.0 TDI", isDefault = false)
        )
    )
    val userProfiles = _userProfiles.asStateFlow()

    private val _selectedUserId = MutableStateFlow("u1")
    val selectedUserId = _selectedUserId.asStateFlow()

    // SmartControl Cockpit state flows
    private val _vehicleProfile = MutableStateFlow(VehicleProfile())
    val vehicleProfile = _vehicleProfile.asStateFlow()

    private val _startStopActive = MutableStateFlow(false)
    val startStopActive = _startStopActive.asStateFlow()

    private val _engineStopTimeSeconds = MutableStateFlow(1L)
    val engineStopTimeSeconds = _engineStopTimeSeconds.asStateFlow()

    private val _carStopTimeSeconds = MutableStateFlow(507L)
    val carStopTimeSeconds = _carStopTimeSeconds.asStateFlow()

    private val _fuelSavedLiters = MutableStateFlow(0.000)
    val fuelSavedLiters = _fuelSavedLiters.asStateFlow()

    private val _moneySavedEuro = MutableStateFlow(0.00)
    val moneySavedEuro = _moneySavedEuro.asStateFlow()

    private val _tripElapsedTimeMinutes = MutableStateFlow(8)
    val tripElapsedTimeMinutes = _tripElapsedTimeMinutes.asStateFlow()

    private val _tripDistanceKm = MutableStateFlow(0.0)
    val tripDistanceKm = _tripDistanceKm.asStateFlow()

    private val _tripCostEuro = MutableStateFlow(0.0)
    val tripCostEuro = _tripCostEuro.asStateFlow()

    private val _refuelingEntries = MutableStateFlow<List<RefuelingEntry>>(
        listOf(
            RefuelingEntry("ref-1", "pratham", "09/09/26 18:42", 52.50, 1.75, 30.0, 59477.0, 100.0)
        )
    )
    val refuelingEntries = _refuelingEntries.asStateFlow()

    private val _tripHistory = MutableStateFlow<List<TripHistoryItem>>(
        listOf(
            TripHistoryItem(
                id = "tr-1",
                date = "09/09/26",
                time = "18:28",
                departure = "Home Residence",
                destination = "Office Tech Park",
                durationMinutes = 18,
                distanceKm = 12.4,
                driveScorePercent = 94.2,
                fuelCostEuro = 2.15,
                consumptionKmL = 17.5,
                avgSpeedKmh = 48.0,
                maxSpeedKmh = 86.0,
                fuelUsedLiters = 0.71,
                maxRpm = 3250,
                hardBrakes = 0,
                rapidAccels = 1,
                specificEvents = listOf(
                    "Cold start completed normally (ambient 19°C)",
                    "Cruised at 86 km/h on Sector 4 arterial",
                    "Regenerative engine braking: 1.4 km",
                    "0 Diagnostic or warning flags recorded"
                ),
                waypoints = listOf("Home", "Central Blvd", "North Ring Road", "Tech Park"),
                routeType = 0
            ),
            TripHistoryItem(
                id = "tr-2",
                date = "08/09/26",
                time = "19:15",
                departure = "Office Tech Park",
                destination = "Supermarket Plaza",
                durationMinutes = 12,
                distanceKm = 6.2,
                driveScorePercent = 88.0,
                fuelCostEuro = 1.10,
                consumptionKmL = 16.8,
                avgSpeedKmh = 34.0,
                maxSpeedKmh = 58.0,
                fuelUsedLiters = 0.37,
                maxRpm = 2900,
                hardBrakes = 0,
                rapidAccels = 0,
                specificEvents = listOf(
                    "Urban evening transit with 4 traffic lights",
                    "Start-Stop idle cut-off active for 2.1 min",
                    "Smooth deceleration into parking bay",
                    "Average coolant temperature 88°C"
                ),
                waypoints = listOf("Tech Park", "Market Avenue", "Commercial Plaza"),
                routeType = 1
            ),
            TripHistoryItem(
                id = "tr-3",
                date = "07/09/26",
                time = "08:30",
                departure = "Home Residence",
                destination = "Sports Complex",
                durationMinutes = 10,
                distanceKm = 4.8,
                driveScorePercent = 91.5,
                fuelCostEuro = 0.85,
                consumptionKmL = 18.2,
                avgSpeedKmh = 38.0,
                maxSpeedKmh = 65.0,
                fuelUsedLiters = 0.26,
                maxRpm = 3100,
                hardBrakes = 0,
                rapidAccels = 1,
                specificEvents = listOf(
                    "Suburban morning commute",
                    "Quick engine warm-up (reached 82°C in 3 min)",
                    "Steady throttle control throughout journey",
                    "Fuel efficiency target exceeded by +6%"
                ),
                waypoints = listOf("Home", "Greenway Path", "Sports Complex"),
                routeType = 2
            ),
            TripHistoryItem(
                id = "tr-4",
                date = "05/09/26",
                time = "14:10",
                departure = "Downtown Metro",
                destination = "International Airport",
                durationMinutes = 26,
                distanceKm = 24.8,
                driveScorePercent = 96.5,
                fuelCostEuro = 3.65,
                consumptionKmL = 19.4,
                avgSpeedKmh = 68.0,
                maxSpeedKmh = 104.0,
                fuelUsedLiters = 1.28,
                maxRpm = 3450,
                hardBrakes = 0,
                rapidAccels = 0,
                specificEvents = listOf(
                    "High-speed highway transit on Motorway A1",
                    "Cruise control engaged for 18 continuous minutes",
                    "Optimal gear 5/6 utilization at 2200 RPM",
                    "Peak eco efficiency: 96.5% driver score"
                ),
                waypoints = listOf("Downtown", "Interchange 7", "Motorway A1", "Terminal 2"),
                routeType = 3
            )
        )
    )
    val tripHistory = _tripHistory.asStateFlow()

    private val _eventLogs = MutableStateFlow<List<VehicleEventLog>>(
        listOf(
            VehicleEventLog("log-1", "09/09/26", "18:36", "Engine operating temperature: It has been reached", "pratham"),
            VehicleEventLog("log-2", "09/09/26", "18:28", "Start driving: with pratham profile. SmartControl active", "pratham"),
            VehicleEventLog("log-3", "09/09/26", "18:25", "OBD-II Bluetooth adapter connected successfully", "pratham")
        )
    )
    val eventLogs = _eventLogs.asStateFlow()

    private val _dtcCodes = MutableStateFlow<List<DtcCode>>(emptyList())
    val dtcCodes = _dtcCodes.asStateFlow()

    private val _isScanningDtcs = MutableStateFlow(false)
    val isScanningDtcs = _isScanningDtcs.asStateFlow()

    fun updateVehicleProfile(profile: VehicleProfile) {
        _vehicleProfile.value = profile
    }

    fun toggleStartStop() {
        val newState = !_startStopActive.value
        _startStopActive.value = newState
        if (newState) {
            _eventLogs.value = listOf(
                VehicleEventLog(
                    id = "log-${System.currentTimeMillis()}",
                    date = SimpleDateFormat("dd/MM/yy", Locale.US).format(Date()),
                    time = SimpleDateFormat("HH:mm", Locale.US).format(Date()),
                    description = "Start/Stop engine management system activated",
                    profile = _vehicleProfile.value.profileName
                )
            ) + _eventLogs.value
        }
    }

    fun addRefuelingEntry(entry: RefuelingEntry) {
        _refuelingEntries.value = listOf(entry) + _refuelingEntries.value
        _vehicleProfile.value = _vehicleProfile.value.copy(
            odometerKm = entry.odometerKm,
            fuelLeftPercent = entry.afterRefuelingPercent
        )
    }

    fun toggleVoiceAlerts() {
        _voiceAlertsEnabled.value = !_voiceAlertsEnabled.value
    }

    fun toggleVisualAlerts() {
        _visualAlertsEnabled.value = !_visualAlertsEnabled.value
    }

    fun dismissAlert() {
        _activeAlert.value = null
    }

    fun clearAnomalies() {
        _detectedAnomalies.value = emptyList()
        if (_activeAlert.value?.id == "alert-anomaly") {
            _activeAlert.value = null
        }
    }

    fun simulateTestAnomaly() {
        val d = latestTick.value?.data
        val speed = if (d != null && d.vss > 0) d.vss else 88.0
        val rpm = if (d != null && d.rpm > 0) d.rpm else 3450.0
        val coolant = 103.5
        val throttle = 89.0
        val triggered = listOf("Coolant Temp (${coolant.toInt()}°C)", "Throttle (${throttle.toInt()}%)")
        val errors = mapOf(
            "coolant_temp" to 0.0086,
            "throttle_pos" to 0.0054,
            "engine_rpm" to 0.00028,
            "air_flow_maf" to 0.00032
        )
        val simHealth = HealthPredictionResponse(
            isAnomaly = true,
            status = "Anomaly",
            anomalyScore = 0.0068,
            threshold = 0.0025,
            triggeredFeatures = triggered,
            featureErrors = errors
        )
        _healthPrediction.value = simHealth
        val record = com.example.model.AnomalyRecord(
            anomalyScore = simHealth.anomalyScore,
            threshold = simHealth.threshold,
            status = "Anomaly",
            triggeredFeatures = triggered,
            featureErrors = errors,
            speedKmh = speed,
            rpm = rpm,
            coolantTemp = coolant,
            throttlePos = throttle
        )
        _detectedAnomalies.value = (listOf(record) + _detectedAnomalies.value).take(50)
        _activeAlert.value = com.example.model.ActiveAlert(
            id = "alert-anomaly",
            title = "HEALTH ANOMALY DETECTED",
            message = "Abnormal reading in ${triggered.joinToString(", ")}.",
            actionAdvice = "Check sensor readings or run OBD-II diagnostic scan.",
            severity = com.example.model.AlertSeverity.WARNING
        )
        if (_voiceAlertsEnabled.value) {
            ttsManager.speak("Warning: Vehicle anomaly detected in ${triggered.joinToString(", ")}.")
        }
    }

    fun triggerTestAlert() {
        _activeAlert.value = com.example.model.ActiveAlert(
            id = "test-${System.currentTimeMillis()}",
            title = "SPEED LIMIT WARNING",
            message = "Current vehicle speed is 128 km/h.",
            actionAdvice = "Maximum recommended speed is 120 km/h. Ease throttle.",
            severity = com.example.model.AlertSeverity.WARNING
        )
    }

    fun scanDtcs() {
        viewModelScope.launch {
            _isScanningDtcs.value = true
            delay(1200) // Scan delay across ECU bus
            _dtcCodes.value = emptyList() // Clean ECU read with mode 03/07
            _isScanningDtcs.value = false
        }
    }

    fun simulateTestDtc() {
        _dtcCodes.value = listOf(
            DtcCode(
                code = "P0171",
                system = "Powertrain / Fuel Trim",
                description = "Fuel System Too Lean (Bank 1)",
                mode = "Mode 03",
                severity = "Attention",
                plainMeaning = "Engine receives slightly more air than fuel ratio target.",
                plainAction = "Inspect mass air flow sensor and vacuum hoses for leaks."
            )
        )
    }

    fun clearDtcs() {
        _dtcCodes.value = emptyList()
    }

    private var telemetryJob: Job? = null
    private var pollingJob: Job? = null
    private var lastTickTimestamp = 0L
    private var lastHealthPredictionTime = 0L
    private var lastBehaviourPredictionTime = 0L
    private var lastFuelPredictionTime = 0L
    private var lastVoiceAlertTime = 0L
    private var lastBehaviourTtsTime = 0L
    private var lastAnomalyAlertTime = 0L
    private var lastAnomalyRecordTime = 0L
    private var windowCounter = 0

    init {
        startObserving()
        startPollingFallback()
        viewModelScope.launch {
            repository.latestTick.collectLatest { tick ->
                if (tick != null) {
                    lastTickTimestamp = System.currentTimeMillis()
                    val currentHistory = _history.value.toMutableList()
                    currentHistory.add(tick)
                    // Keep max 300 items for ring buffer
                    if (currentHistory.size > 300) {
                        currentHistory.removeAt(0)
                    }
                    _history.value = currentHistory

                    // Extract real-time ML results pushed directly over WebSocket / live-data!
                    tick.ml?.let { ml ->
                        var receivedMl = false
                        ml.health?.let { hp ->
                            _healthPrediction.value = hp
                            checkAnomalyAlert(hp)
                            receivedMl = true
                        }
                        ml.driverBehaviour?.let { db ->
                            _driverBehaviour.value = db
                            receivedMl = true
                            db.ttsMessage?.let { msg ->
                                val now = System.currentTimeMillis()
                                if (_voiceAlertsEnabled.value && now - lastBehaviourTtsTime > 25000) {
                                    ttsManager.speak(msg)
                                    lastBehaviourTtsTime = now
                                }
                            }
                        }
                        ml.fuel?.let { fp ->
                            _fuelPrediction.value = fp
                            receivedMl = true
                        }
                        if (receivedMl) {
                            _lastInferenceTimestamp.value = System.currentTimeMillis()
                            _inferenceError.value = null
                        }
                    }
                    
                    val now = System.currentTimeMillis()
                    // Fallback periodic polling if WebSocket tick.ml is still warming up (~30 ticks)
                    if (tick.ml?.health == null && now - lastHealthPredictionTime > 6000) {
                        lastHealthPredictionTime = now
                        predictHealth(currentHistory, tick)
                    }
                    
                    if (tick.ml?.driverBehaviour == null && now - lastBehaviourPredictionTime > 10000 && currentHistory.size >= 5) {
                        lastBehaviourPredictionTime = now
                        predictBehaviour(currentHistory, tick)
                    }

                    if (tick.ml?.fuel == null && now - lastFuelPredictionTime > 6000) {
                        lastFuelPredictionTime = now
                        predictFuel(currentHistory, tick)
                    }

                    checkAndSpeakAlerts(tick)
                }
            }
        }
        
        fetchSimulatorStatus()
        fetchDatasets()
        triggerInference()
    }

    private fun checkAnomalyAlert(health: HealthPredictionResponse) {
        if (!health.isAnomaly) {
            if (_activeAlert.value?.id == "alert-anomaly") {
                _activeAlert.value = null
            }
            return
        }
        val now = System.currentTimeMillis()

        // Log anomaly to detectedAnomalies history (debounced by 4s to avoid identical tick spam)
        if (now - lastAnomalyRecordTime > 4000) {
            lastAnomalyRecordTime = now
            val tick = latestTick.value
            val d = tick?.data
            val record = com.example.model.AnomalyRecord(
                anomalyScore = health.anomalyScore,
                threshold = health.threshold,
                status = health.status,
                triggeredFeatures = health.triggeredFeatures ?: emptyList(),
                featureErrors = health.featureErrors ?: emptyMap(),
                speedKmh = d?.vss ?: 0.0,
                rpm = d?.rpm ?: 0.0,
                coolantTemp = d?.coolantTemp ?: 0.0,
                throttlePos = d?.throttlePos ?: 0.0
            )
            _detectedAnomalies.value = (listOf(record) + _detectedAnomalies.value).take(50)
        }

        if (now - lastAnomalyAlertTime < 30000) return

        val triggeredDesc = if (!health.triggeredFeatures.isNullOrEmpty()) {
            health.triggeredFeatures.joinToString(", ")
        } else {
            "critical engine sensors"
        }
        val alertMsg = "Warning: Vehicle anomaly detected in $triggeredDesc."
        if (_voiceAlertsEnabled.value) {
            ttsManager.speak(alertMsg)
        }
        if (_visualAlertsEnabled.value) {
            _visualAlertEvent.tryEmit(alertMsg)
            _activeAlert.value = com.example.model.ActiveAlert(
                id = "alert-anomaly",
                title = "HEALTH ANOMALY DETECTED",
                message = "Abnormal reading in $triggeredDesc.",
                actionAdvice = "Check sensor readings or run OBD-II diagnostic scan.",
                severity = com.example.model.AlertSeverity.WARNING
            )
        }
        lastAnomalyAlertTime = now
    }

    private fun checkAndSpeakAlerts(tick: TelemetryTick) {
        val d = tick.data
        val notif = _notificationSettings.value
        val speedLimit = notif.overspeedingMessageOverKmh.toDouble()

        if (d.vss <= speedLimit && d.rpm <= 6000 && d.coolantTemp <= 100) {
            val current = _activeAlert.value
            if (current != null && (current.id == "alert-speed" || current.id == "alert-rpm" || current.id == "alert-coolant")) {
                _activeAlert.value = null
            }
        }

        if (!_voiceAlertsEnabled.value && !_visualAlertsEnabled.value) return
        
        val now = System.currentTimeMillis()
        val debounceMs = (notif.overspeedingAlertTimeOutSec * 1000L).coerceAtLeast(8000L)
        if (now - lastVoiceAlertTime < debounceMs) return // Debounce alerts

        var alertMsg: String? = null
        var alertObj: com.example.model.ActiveAlert? = null
        var isVoiceAllowed = true
        
        if (d.vss > speedLimit && notif.generateOverspeedingAlert) {
            alertMsg = notif.overspeedingAlertText.ifEmpty {
                "Warning: Speed limit exceeded. Current speed is ${d.vss.toInt()} km/h."
            }
            alertObj = com.example.model.ActiveAlert(
                id = "alert-speed",
                title = "OVERSPEEDING ALERT",
                message = "${alertMsg} (${d.vss.toInt()} km/h > ${speedLimit.toInt()} km/h limit).",
                actionAdvice = "Ease throttle pedal to maintain safe stopping distance.",
                severity = com.example.model.AlertSeverity.WARNING
            )
            isVoiceAllowed = notif.overspeedingVocalAlert
        } else if (d.coolantTemp > 100) {
            alertMsg = "Critical Warning: Engine coolant temperature is too high (${d.coolantTemp.toInt()}°C). Please pull over safely."
            alertObj = com.example.model.ActiveAlert(
                id = "alert-coolant",
                title = "COOLANT OVERHEAT",
                message = "Coolant temperature is ${d.coolantTemp.toInt()}°C (Normal: 85-92°C).",
                actionAdvice = "Pull over safely and shut off engine to prevent overheating.",
                severity = com.example.model.AlertSeverity.CRITICAL
            )
            isVoiceAllowed = true
        } else if (d.rpm > 6000) {
            alertMsg = "Warning: High engine RPM detected (${d.rpm.toInt()} RPM)."
            alertObj = com.example.model.ActiveAlert(
                id = "alert-rpm",
                title = "HIGH ENGINE RPM",
                message = "Engine speed reached ${d.rpm.toInt()} RPM near redline.",
                actionAdvice = "Shift to higher gear to protect engine.",
                severity = com.example.model.AlertSeverity.WARNING
            )
            isVoiceAllowed = true
        } else if (d.coolantTemp in 85.0..92.0 && notif.generateEngineOperatingTempMessage && (tick.rowIndex % 200 == 0)) {
            alertMsg = notif.warningTextEngineOperatingTemp
            alertObj = com.example.model.ActiveAlert(
                id = "alert-operating-temp",
                title = "ENGINE AT OPERATING TEMPERATURE",
                message = notif.warningTextEngineOperatingTemp,
                actionAdvice = "Engine is fully warmed up and at peak thermodynamic efficiency.",
                severity = com.example.model.AlertSeverity.INFO
            )
            isVoiceAllowed = notif.engineOperatingTempVocalMessage
        } else if (d.coolantTemp < 60.0 && d.rpm > 3500 && notif.generateLowEngineTempMessage) {
            alertMsg = notif.warningTextLowEngineTemp
            alertObj = com.example.model.ActiveAlert(
                id = "alert-low-temp",
                title = "COLD ENGINE WARNING",
                message = notif.warningTextLowEngineTemp,
                actionAdvice = "Keep RPM below 3000 until engine reaches operating temperature.",
                severity = com.example.model.AlertSeverity.WARNING
            )
            isVoiceAllowed = notif.lowEngineTempVocalAlert
        }
        
        if (alertMsg != null) {
            if (_voiceAlertsEnabled.value && isVoiceAllowed) {
                ttsManager.speak(alertMsg)
            }
            if (_visualAlertsEnabled.value) {
                _visualAlertEvent.tryEmit(alertMsg)
                if (alertObj != null) {
                    _activeAlert.value = alertObj
                }
            }
            lastVoiceAlertTime = now
        }
    }

    fun updateNotificationSettings(transform: (com.example.model.NotificationSettings) -> com.example.model.NotificationSettings) {
        _notificationSettings.value = transform(_notificationSettings.value)
    }

    fun setNotificationSettings(settings: com.example.model.NotificationSettings) {
        _notificationSettings.value = settings
    }

    fun toggleVoiceAlerts(enabled: Boolean) {
        _voiceAlertsEnabled.value = enabled
    }

    fun toggleVisualAlerts(enabled: Boolean) {
        _visualAlertsEnabled.value = enabled
    }

    fun updateUserName(name: String) {
        _userName.value = name
    }

    fun updateVehicleModel(model: String) {
        _vehicleModel.value = model
    }

    fun selectUser(user: com.example.model.UserProfileItem) {
        _selectedUserId.value = user.id
        _userName.value = user.name
        _vehicleModel.value = user.vehicleModel
        _vehicleProfile.value = _vehicleProfile.value.copy(
            profileName = user.name.lowercase()
        )
    }

    fun selectUserById(userId: String) {
        val user = _userProfiles.value.find { it.id == userId } ?: return
        selectUser(user)
    }

    fun addNewUser(name: String, vehicleModel: String = "Seat Leon 1.6 FSI") {
        val trimmedName = name.trim().ifEmpty { "Driver" }
        val trimmedModel = vehicleModel.trim().ifEmpty { "Seat Leon 1.6 FSI" }
        val newUser = com.example.model.UserProfileItem(
            id = "u_${System.currentTimeMillis()}",
            name = trimmedName,
            vehicleModel = trimmedModel,
            isDefault = false
        )
        _userProfiles.value = _userProfiles.value + newUser
        selectUser(newUser)
    }

    fun registerNewUserWithProfile(profile: VehicleProfile) {
        val trimmedName = profile.profileName.trim().ifEmpty { "Driver" }
        val newUser = com.example.model.UserProfileItem(
            id = "u_${System.currentTimeMillis()}",
            name = trimmedName,
            vehicleModel = "${profile.fuelSupply.replaceFirstChar { it.uppercase() }} ${profile.displacementCc}cc (${profile.maxPowerHp.toInt()} HP)",
            isDefault = false
        )
        _userProfiles.value = _userProfiles.value + newUser
        _selectedUserId.value = newUser.id
        _userName.value = trimmedName
        _vehicleModel.value = newUser.vehicleModel
        _vehicleProfile.value = profile
        
        _eventLogs.value = listOf(
            VehicleEventLog(
                id = "log-${System.currentTimeMillis()}",
                date = SimpleDateFormat("dd/MM/yy", Locale.US).format(Date()),
                time = SimpleDateFormat("HH:mm", Locale.US).format(Date()),
                description = "New profile registered: ${profile.profileName} (${profile.displacementCc}cc, ${profile.fuelSupply})",
                profile = profile.profileName
            )
        ) + _eventLogs.value
    }

    fun connectObd() {
        startObserving()
        startPollingFallback()
    }

    fun triggerInference() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _inferenceError.value = null

            try {
                // Ensure we have history data
                var historyData = _history.value
                if (historyData.size < 5) {
                    val historyResult = repository.getHistory(30)
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

                // Check if latest ML is available directly from backend
                val latestMlResult = repository.getLatestMl()
                if (latestMlResult.isSuccess) {
                    val ml = latestMlResult.getOrNull()
                    if (ml?.health != null) _healthPrediction.value = ml.health
                    if (ml?.driverBehaviour != null) _driverBehaviour.value = ml.driverBehaviour
                    if (ml?.fuel != null) _fuelPrediction.value = ml.fuel
                }

                // If targetTick has embedded ML inference results, use them as well
                targetTick?.ml?.let { ml ->
                    if (ml.health != null && _healthPrediction.value == null) _healthPrediction.value = ml.health
                    if (ml.driverBehaviour != null && _driverBehaviour.value == null) _driverBehaviour.value = ml.driverBehaviour
                    if (ml.fuel != null && _fuelPrediction.value == null) _fuelPrediction.value = ml.fuel
                }

                // If any prediction is still missing, trigger dedicated endpoints
                if (targetTick != null) {
                    val tasks = mutableListOf<kotlinx.coroutines.Deferred<Unit>>()
                    if (_healthPrediction.value == null) {
                        tasks.add(async { predictHealth(historyData, targetTick) })
                    }
                    if (_driverBehaviour.value == null) {
                        tasks.add(async { predictBehaviour(historyData, targetTick) })
                    }
                    if (_fuelPrediction.value == null) {
                        tasks.add(async { predictFuel(historyData, targetTick) })
                    }
                    if (tasks.isNotEmpty()) {
                        tasks.forEach { it.await() }
                    }
                }

                if (_healthPrediction.value == null && _driverBehaviour.value == null && _fuelPrediction.value == null) {
                    _inferenceError.value = "Failed to obtain ML predictions. Make sure backend is running."
                } else {
                    _lastInferenceTimestamp.value = System.currentTimeMillis()
                    _inferenceError.value = null
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
                // If we haven't received a tick in the last 1500ms, fetch via REST polling
                if (now - lastTickTimestamp > 1500) {
                    val result = repository.getLiveData()
                    if (result.isSuccess && result.getOrNull() != null) {
                        lastTickTimestamp = System.currentTimeMillis()
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

    fun fetchSimulatorStatus() {
        viewModelScope.launch {
            val result = repository.getStatus()
            if (result.isSuccess) {
                _simulatorStatus.value = result.getOrNull()
            }
        }
    }

    fun startSimulation(datasetId: String? = null, speed: Double? = null, loop: Boolean? = null) {
        viewModelScope.launch {
            repository.startSimulation(datasetId, speed, loop)
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

    fun resetSimulation() {
        viewModelScope.launch {
            repository.resetSimulation()
            fetchSimulatorStatus()
        }
    }

    fun setPlaybackSpeed(speed: Double) {
        viewModelScope.launch {
            repository.setSpeed(speed)
            fetchSimulatorStatus()
        }
    }

    fun toggleLoop(loop: Boolean) {
        viewModelScope.launch {
            repository.setLoop(loop)
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

    private suspend fun predictHealth(historyTicks: List<TelemetryTick>, fallbackTick: TelemetryTick) {
        // LSTM Autoencoder requires >= 24 ticks
        val sourceTicks = if (historyTicks.size >= 24) {
            historyTicks.takeLast(24)
        } else if (historyTicks.isNotEmpty()) {
            val padCount = 24 - historyTicks.size
            List(padCount) { historyTicks.first() } + historyTicks
        } else {
            List(24) { fallbackTick }
        }

        val request = HealthPredictionRequest(
            ticks = sourceTicks.map {
                HealthSensorTick(
                    rpm = it.data.rpm,
                    vss = it.data.vss,
                    maf = it.data.maf,
                    throttlePos = it.data.throttlePos,
                    mapKpa = it.data.mapKpa,
                    coolantTemp = it.data.coolantTemp,
                    intakeAirTemp = it.data.intakeAirTemp,
                    ambientTemp = it.data.ambientTemp,
                    pedalD = it.data.pedalD,
                    pedalE = it.data.pedalE
                )
            }
        )
        val result = repository.predictHealth(request)
        if (result.isSuccess && result.getOrNull() != null) {
            val response = result.getOrNull()
            _healthPrediction.value = response
            response?.let { checkAnomalyAlert(it) }
        } else {
            // Local Physics / Anomaly Heuristic Fallback
            val fallbackResponse = generateFallbackHealth(fallbackTick)
            _healthPrediction.value = fallbackResponse
            checkAnomalyAlert(fallbackResponse)
        }
    }
    
    private suspend fun predictBehaviour(historyTicks: List<TelemetryTick>, fallbackTick: TelemetryTick) {
        // XGBoost model requires >= 5 ticks
        val sourceTicks = if (historyTicks.size >= 5) {
            historyTicks.takeLast(30)
        } else if (historyTicks.isNotEmpty()) {
            val padCount = 5 - historyTicks.size
            List(padCount) { historyTicks.first() } + historyTicks
        } else {
            List(5) { fallbackTick }
        }

        val request = DriverBehaviourRequest(
            rpmValues = sourceTicks.map { it.data.rpm },
            speedValues = sourceTicks.map { it.data.vss },
            throttleValues = sourceTicks.map { it.data.throttlePos },
            windowIndex = windowCounter++
        )
        val result = repository.predictDriverBehaviour(request)
        if (result.isSuccess && result.getOrNull() != null) {
            val response = result.getOrNull()
            _driverBehaviour.value = response
            response?.ttsMessage?.let { msg ->
                val now = System.currentTimeMillis()
                if (_voiceAlertsEnabled.value && now - lastBehaviourTtsTime > 25000) {
                    ttsManager.speak(msg)
                    lastBehaviourTtsTime = now
                }
            }
        } else {
            // Local Classifier Fallback
            val fallbackResponse = generateFallbackBehaviour(fallbackTick)
            _driverBehaviour.value = fallbackResponse
        }
    }

    private suspend fun predictFuel(historyTicks: List<TelemetryTick>, fallbackTick: TelemetryTick) {
        // Physics fuel estimator requires >= 20 ticks
        val sourceTicks = if (historyTicks.size >= 20) {
            historyTicks.takeLast(20)
        } else if (historyTicks.isNotEmpty()) {
            val padCount = 20 - historyTicks.size
            List(padCount) { historyTicks.first() } + historyTicks
        } else {
            List(20) { fallbackTick }
        }

        val request = FuelPredictionRequest(
            ticks = sourceTicks.map {
                FuelSensorTick(
                    rpm = it.data.rpm,
                    vss = it.data.vss,
                    maf = it.data.maf,
                    throttlePos = it.data.throttlePos,
                    mapKpa = it.data.mapKpa,
                    pedalD = it.data.pedalD,
                    pedalE = it.data.pedalE
                )
            }
        )
        val result = repository.predictFuel(request)
        if (result.isSuccess && result.getOrNull() != null) {
            _fuelPrediction.value = result.getOrNull()
        } else {
            _fuelPrediction.value = generateFallbackFuel(fallbackTick)
        }
    }

    private fun generateFallbackHealth(fallbackTick: TelemetryTick): HealthPredictionResponse {
        val d = fallbackTick.data
        val isHot = d.coolantTemp > 98.0
        val isHighLoad = d.throttlePos > 85.0
        val isAnomaly = isHot || isHighLoad
        val triggered = mutableListOf<String>()
        if (isHot) triggered.add("Coolant Temp (${d.coolantTemp.toInt()}°C)")
        if (isHighLoad) triggered.add("Throttle (${d.throttlePos.toInt()}%)")

        val errors = mapOf(
            "coolant_temp" to if (isHot) 0.0084 else 0.00018,
            "engine_rpm" to 0.00025,
            "air_flow_maf" to 0.00031,
            "map_kpa" to 0.00021,
            "throttle_pos" to if (isHighLoad) 0.0052 else 0.00029,
            "intake_air_temp" to 0.00015
        )

        return HealthPredictionResponse(
            anomalyScore = if (isAnomaly) 0.0054 else 0.00034,
            threshold = 0.0025,
            isAnomaly = isAnomaly,
            status = if (isAnomaly) "Anomaly" else "Normal",
            triggeredFeatures = triggered,
            featureErrors = errors
        )
    }

    private fun generateFallbackBehaviour(fallbackTick: TelemetryTick): DriverBehaviourResponse {
        val d = fallbackTick.data
        val isAggressive = d.throttlePos > 55.0 || d.rpm > 4200
        val isEco = d.vss > 20.0 && d.throttlePos < 30.0 && d.rpm < 2800

        val label = when {
            isAggressive -> "Aggressive"
            isEco -> "Economical"
            else -> "Normal"
        }

        val tts = when {
            isAggressive -> "Aggressive throttle detected. Smooth out pedal input to conserve fuel."
            isEco -> "Eco-driving pattern observed. Excellent fuel preservation."
            else -> "Driving profile is balanced and within normal operational limits."
        }

        val features = mapOf(
            "avg_speed" to d.vss,
            "mean_rpm" to d.rpm,
            "mean_pedal" to d.throttlePos,
            "rpm_std" to if (isAggressive) 420.0 else 110.0,
            "vs_dev" to if (isAggressive) 14.5 else 4.2,
            "pedal_std" to if (isAggressive) 18.0 else 5.5
        )

        return DriverBehaviourResponse(
            label = label,
            confidence = if (isAggressive) 0.89 else if (isEco) 0.94 else 0.91,
            probabilities = mapOf(
                "Aggressive" to if (isAggressive) 0.89 else 0.05,
                "Normal" to if (isAggressive) 0.08 else (if (isEco) 0.15 else 0.91),
                "Economical" to if (isEco) 0.94 else 0.06
            ),
            ttsMessage = tts,
            featureValues = features
        )
    }

    private fun generateFallbackFuel(fallbackTick: TelemetryTick): FuelPredictionResponse {
        val d = fallbackTick.data
        val maf = if (d.maf > 0.1) d.maf else (d.rpm * 0.004).coerceAtLeast(2.0)
        val fcrGs = maf / 14.7
        val flowLitersPerHour = (fcrGs / 740.0) * 3600.0
        val mileage = if (d.vss > 1.0) {
            (d.vss / flowLitersPerHour).coerceIn(4.0, 32.0)
        } else null

        return FuelPredictionResponse(
            fcrGs = fcrGs,
            vssKmph = d.vss,
            mileageKmpl = mileage,
            fuelFlowLitersPerHour = flowLitersPerHour,
            tier = 1,
            method = "maf"
        )
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
