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
import com.example.model.TelemetryData
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

    // ====== Complete Live Fuel & Refueling Management State ======
    private val _currentFuelLiters = MutableStateFlow(10.8) // 24% of 45L = 10.8L initial
    val currentFuelLiters = _currentFuelLiters.asStateFlow()

    private val _currentFuelPercent = MutableStateFlow(24.0)
    val currentFuelPercent = _currentFuelPercent.asStateFlow()

    private val _fuelConsumedTripLiters = MutableStateFlow(0.0)
    val fuelConsumedTripLiters = _fuelConsumedTripLiters.asStateFlow()

    private val _instantFuelEconomyKmL = MutableStateFlow(14.2)
    val instantFuelEconomyKmL = _instantFuelEconomyKmL.asStateFlow()

    private val _instantFuelEconomyL100km = MutableStateFlow(7.0)
    val instantFuelEconomyL100km = _instantFuelEconomyL100km.asStateFlow()

    private val _averageFuelEconomyKmL = MutableStateFlow(14.0)
    val averageFuelEconomyKmL = _averageFuelEconomyKmL.asStateFlow()

    private val _averageFuelEconomyL100km = MutableStateFlow(7.1)
    val averageFuelEconomyL100km = _averageFuelEconomyL100km.asStateFlow()

    private val _estimatedRangeKm = MutableStateFlow(152.0)
    val estimatedRangeKm = _estimatedRangeKm.asStateFlow()

    private val _fuelPricePerLiter = MutableStateFlow(96.72)
    val fuelPricePerLiter = _fuelPricePerLiter.asStateFlow()

    private val _fuelCurrency = MutableStateFlow("₹")
    val fuelCurrency = _fuelCurrency.asStateFlow()

    val regionalFuelPresets = listOf(
        com.example.model.RegionalFuelPreset("preset-in-petrol", "India (Karnataka / National)", "Petrol", 96.72, "₹", isAutoDetected = true),
        com.example.model.RegionalFuelPreset("preset-in-diesel", "India (Karnataka / National)", "Diesel", 89.62, "₹"),
        com.example.model.RegionalFuelPreset("preset-eu-petrol", "Europe (Central)", "Eurosuper 95", 1.78, "€"),
        com.example.model.RegionalFuelPreset("preset-eu-diesel", "Europe (Central)", "Diesel", 1.65, "€"),
        com.example.model.RegionalFuelPreset("preset-us-regular", "United States (National Avg)", "Regular Unleaded 87", 0.92, "$"),
        com.example.model.RegionalFuelPreset("preset-uk-petrol", "United Kingdom", "Premium Unleaded 95", 1.45, "£")
    )

    private val _selectedFuelPreset = MutableStateFlow(regionalFuelPresets[0])
    val selectedFuelPreset = _selectedFuelPreset.asStateFlow()

    fun saveCurrentTripSnapshot(onSaved: (String) -> Unit = {}) {
        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("dd/MM/yy", Locale.US).format(Date(now))
        val timeStr = SimpleDateFormat("HH:mm", Locale.US).format(Date(now))
        val dist = _tripDistanceKm.value
        val cost = _tripCostEuro.value
        val duration = _tripElapsedTimeMinutes.value
        val ticks = _tripTickBuffer.value
        val avgSpd = if (ticks.isNotEmpty()) ticks.map { it.vss }.average() else 35.0
        val maxSpd = if (ticks.isNotEmpty()) ticks.maxOfOrNull { it.vss } ?: 70.0 else 70.0
        val avgThrot = if (ticks.isNotEmpty()) ticks.map { it.throttlePos }.average() else 25.0
        val maxRpmVal = if (ticks.isNotEmpty()) ticks.maxOfOrNull { it.rpm }?.toInt() ?: 3200 else 3200
        val lastPoint = ticks.lastOrNull()
        val coordsStr = if (lastPoint != null) "${String.format(Locale.US, "%.5f", lastPoint.lat)}, ${String.format(Locale.US, "%.5f", lastPoint.lon)}" else "12.91345°, 74.88922°"

        val item = TripHistoryItem(
            id = "trip_$now",
            date = dateStr,
            time = timeStr,
            departure = "Start Location",
            destination = "Current Position",
            durationMinutes = duration,
            distanceKm = dist,
            driveScorePercent = 88.5,
            fuelCostEuro = cost,
            consumptionKmL = _averageFuelEconomyKmL.value,
            avgSpeedKmh = avgSpd,
            maxSpeedKmh = maxSpd,
            fuelUsedLiters = _fuelConsumedTripLiters.value,
            maxRpm = maxRpmVal,
            currency = _fuelCurrency.value,
            lastCoords = coordsStr,
            tickSnapshot = ticks,
            specificEvents = listOf(
                "Trip saved successfully @ $timeStr",
                "Total Distance: ${String.format(Locale.US, "%.2f", dist)} km",
                "Total Cost: ${_fuelCurrency.value}${String.format(Locale.US, "%.2f", cost)}"
            )
        )
        _tripHistory.value = listOf(item) + _tripHistory.value
        ttsManager.speak("Trip successfully saved to history. Distance ${String.format(Locale.US, "%.1f", dist)} kilometers.")
        onSaved(item.id)
    }

    private val _isCustomFuelPrice = MutableStateFlow(false)
    val isCustomFuelPrice = _isCustomFuelPrice.asStateFlow()

    private val _costPerKm = MutableStateFlow(0.126)
    val costPerKm = _costPerKm.asStateFlow()

    private var lastFuelBurnTimestamp = 0L
    private var lastRangeVoiceAnnouncementTime = 0L

    private val _refuelingEntries = MutableStateFlow<List<RefuelingEntry>>(
        listOf(
            RefuelingEntry("ref-1", "pratham", "09/09/26 18:42", 52.50, 1.75, 30.0, 59477.0, 100.0)
        )
    )
    val refuelingEntries = _refuelingEntries.asStateFlow()

    private val _tripHistory = MutableStateFlow<List<TripHistoryItem>>(emptyList())
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

    // GPS & Route State
    private val _routeData = MutableStateFlow<com.example.model.RouteResponse?>(null)
    val routeData = _routeData.asStateFlow()

    private val _tripSummary = MutableStateFlow<com.example.model.TripSummaryResponse?>(null)
    val tripSummary = _tripSummary.asStateFlow()

    private val _liveGpsTrail = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val liveGpsTrail = _liveGpsTrail.asStateFlow()

    // Trip Map Enhancement States (Leaflet Map, Modes, Fuel Alert)
    private val _tripTickBuffer = MutableStateFlow<List<com.example.model.TripTickItem>>(emptyList())
    val tripTickBuffer = _tripTickBuffer.asStateFlow()

    private val _currentMapMode = MutableStateFlow(com.example.model.TripMapMode.ROUTE)
    val currentMapMode = _currentMapMode.asStateFlow()

    private val _lowFuelAlert = MutableStateFlow<com.example.model.LowFuelAlertState?>(null)
    val lowFuelAlert = _lowFuelAlert.asStateFlow()

    private var fuelAlertShown = false
    private var cumulativeTripFuelUsedLitres = 0.0
    private var lastRecordedGpsPoint: Pair<Double, Double>? = null
    private var fuelAlertDismissJob: Job? = null

    fun setTripMapMode(mode: com.example.model.TripMapMode) {
        _currentMapMode.value = mode
    }

    fun refreshMapTrail() {
        _liveGpsTrail.value = emptyList()
        _tripTickBuffer.value = emptyList()
        fetchGpsRouteAndSummary()
    }

    fun dismissFuelAlert() {
        _lowFuelAlert.value = _lowFuelAlert.value?.copy(isVisible = false)
    }

    fun triggerTestFuelAlert() {
        val remaining = 3.8
        val avgFcr = 8.2
        val rangeKm = (remaining / avgFcr) * 100.0
        showFuelAlert(remaining, rangeKm, avgFcr)
    }

    private fun showFuelAlert(remainingLiters: Double, rangeKm: Double, avgFcr: Double) {
        val notif = _notificationSettings.value
        _lowFuelAlert.value = com.example.model.LowFuelAlertState(
            remainingFuelLiters = remainingLiters,
            estimatedRangeKm = rangeKm,
            avgConsumptionL100km = avgFcr,
            isVisible = true
        )
        fuelAlertDismissJob?.cancel()
        fuelAlertDismissJob = viewModelScope.launch {
            delay(8000)
            _lowFuelAlert.value = _lowFuelAlert.value?.copy(isVisible = false)
        }

        if (_voiceAlertsEnabled.value && (notif.lackOfAutonomyVocalAlert || notif.vehicleRangeVocalAlert)) {
            val tank = _vehicleProfile.value.tankCapacityLiters.toInt()
            val burnRate = String.format(Locale.US, "%.1f", latestTick.value?.ml?.fuel?.fuelFlowLitersPerHour ?: 1.4)
            ttsManager.speak("Warning: Low fuel autonomy alert. Estimated range is ${rangeKm.toInt()} kilometers with ${String.format(Locale.US, "%.1f", remainingLiters)} liters remaining in your $tank liter tank. Active burn rate is $burnRate liters per hour. Please refuel soon.")
        }
        if (_visualAlertsEnabled.value && notif.generateLackOfAutonomyAlert) {
            showAlertWithTimeout(
                com.example.model.ActiveAlert(
                    id = "alert-fuel-autonomy",
                    title = "LOW FUEL AUTONOMY",
                    message = "Fuel range below 50 km (${rangeKm.toInt()} km remaining, ${String.format(Locale.US, "%.1f", remainingLiters)}L in tank).",
                    actionAdvice = "Find nearest refueling station to prevent fuel starvation.",
                    severity = com.example.model.AlertSeverity.WARNING
                )
            )
        }
    }

    fun resetTripSession() {
        _tripTickBuffer.value = emptyList()
        _lowFuelAlert.value = null
        fuelAlertShown = false
        cumulativeTripFuelUsedLitres = 0.0
        _fuelConsumedTripLiters.value = 0.0
        _tripCostEuro.value = 0.0
        lastRecordedGpsPoint = null
        tripStartedAnnounced = false
        tripEndedAnnounced = false
        engineWarmedUpAnnounced = false
        tripStationaryStartMs = 0L
        updateEstimatedRange()
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return 6371.0 * c
    }

    fun updateVehicleProfile(profile: VehicleProfile) {
        _vehicleProfile.value = profile
        val tank = profile.tankCapacityLiters.coerceAtLeast(1.0)
        val targetLiters = (profile.fuelLeftPercent / 100.0) * tank
        _currentFuelLiters.value = targetLiters
        _currentFuelPercent.value = profile.fuelLeftPercent
        updateEstimatedRange()
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
        performRefueling(entry, addedLiters = entry.fuelLiters, afterPercent = entry.afterRefuelingPercent)
    }

    fun performRefueling(entry: RefuelingEntry, addedLiters: Double = 0.0, afterPercent: Double? = null) {
        val tank = _vehicleProfile.value.tankCapacityLiters.coerceAtLeast(1.0)
        val calculatedPercent = when {
            afterPercent != null && afterPercent > 0.0 -> afterPercent.coerceIn(0.0, 100.0)
            addedLiters > 0.0 -> {
                val newLiters = (_currentFuelLiters.value + addedLiters).coerceAtMost(tank)
                (newLiters / tank) * 100.0
            }
            entry.fuelLiters > 0.0 -> {
                val newLiters = (_currentFuelLiters.value + entry.fuelLiters).coerceAtMost(tank)
                (newLiters / tank) * 100.0
            }
            entry.afterRefuelingPercent > 0.0 -> entry.afterRefuelingPercent.coerceIn(0.0, 100.0)
            else -> 100.0
        }
        val newFuelLiters = (calculatedPercent / 100.0) * tank
        _currentFuelLiters.value = newFuelLiters
        _currentFuelPercent.value = calculatedPercent
        val finalEntry = entry.copy(
            afterRefuelingPercent = calculatedPercent,
            fuelLiters = if (entry.fuelLiters > 0.0) entry.fuelLiters else addedLiters
        )
        _refuelingEntries.value = listOf(finalEntry) + _refuelingEntries.value.filterNot { it.id == finalEntry.id }
        _vehicleProfile.value = _vehicleProfile.value.copy(
            odometerKm = if (finalEntry.odometerKm > 0) finalEntry.odometerKm else _vehicleProfile.value.odometerKm,
            fuelLeftPercent = calculatedPercent
        )
        updateEstimatedRange()
        fuelAlertShown = false

        if (_voiceAlertsEnabled.value) {
            val addedStr = String.format(Locale.US, "%.1f", if (finalEntry.fuelLiters > 0) finalEntry.fuelLiters else addedLiters)
            ttsManager.speak("Refueling recorded. $addedStr liters added. Fuel level is now ${calculatedPercent.toInt()} percent. Estimated range is ${_estimatedRangeKm.value.toInt()} kilometers.")
        }
    }

    fun deleteRefuelingEntry(id: String) {
        _refuelingEntries.value = _refuelingEntries.value.filterNot { it.id == id }
    }

    fun resetFuelLevelToFull() {
        val tank = _vehicleProfile.value.tankCapacityLiters.coerceAtLeast(1.0)
        _currentFuelLiters.value = tank
        _currentFuelPercent.value = 100.0
        _vehicleProfile.value = _vehicleProfile.value.copy(fuelLeftPercent = 100.0)
        updateEstimatedRange()
        fuelAlertShown = false
        if (_voiceAlertsEnabled.value) {
            ttsManager.speak("Fuel tank level reset to full. 100 percent capacity available. Estimated range is ${_estimatedRangeKm.value.toInt()} kilometers.")
        }
    }

    fun selectRegionalPricePreset(preset: com.example.model.RegionalFuelPreset) {
        _selectedFuelPreset.value = preset
        _isCustomFuelPrice.value = false
        _fuelPricePerLiter.value = preset.pricePerLiter
        _fuelCurrency.value = preset.currency
        updateEstimatedRange()
    }

    fun setCustomFuelPriceOverride(price: Double, currency: String = "€") {
        if (price > 0.0) {
            _fuelPricePerLiter.value = price
            _fuelCurrency.value = currency
            _isCustomFuelPrice.value = true
            updateEstimatedRange()
        }
    }

    fun updateFuelPrice(price: Double, currency: String = "€") {
        setCustomFuelPriceOverride(price, currency)
    }

    fun setFuelRangeAutonomyThresholds(rangeKm: Int, percent: Int) {
        _notificationSettings.value = _notificationSettings.value.copy(
            fuelRangeAutonomyThresholdKm = rangeKm.coerceIn(15, 200),
            lowFuelWarningThresholdPercent = percent.coerceIn(5, 40)
        )
    }

    fun speakVehicleRange() {
        val range = _estimatedRangeKm.value.toInt()
        val liters = String.format(Locale.US, "%.1f", _currentFuelLiters.value)
        val percent = _currentFuelPercent.value.toInt()
        val tank = _vehicleProfile.value.tankCapacityLiters.toInt()
        val avgKmpl = String.format(Locale.US, "%.1f", _averageFuelEconomyKmL.value)
        val burnRate = String.format(
            Locale.US,
            "%.1f",
            latestTick.value?.ml?.fuel?.fuelFlowLitersPerHour ?: if (latestTick.value?.data?.maf != null && latestTick.value!!.data.maf > 0.1) {
                ((latestTick.value!!.data.maf / 14.7) / 730.0) * 3600.0
            } else 0.65
        )
        val text = "Estimated vehicle range is $range kilometers. You have $liters liters remaining out of a $tank liter tank, at $percent percent capacity. Current burn rate is $burnRate liters per hour with average fuel economy of $avgKmpl kilometers per liter."
        ttsManager.speak(text)
    }

    private fun updateEstimatedRange() {
        val avgL100km = if (_averageFuelEconomyL100km.value > 1.0) {
            _averageFuelEconomyL100km.value
        } else {
            _vehicleProfile.value.consumptionL100km.coerceAtLeast(4.0)
        }
        val range = (_currentFuelLiters.value / avgL100km) * 100.0
        _estimatedRangeKm.value = range.coerceAtLeast(0.0)

        val dist = _tripDistanceKm.value
        if (dist > 0.05) {
            _costPerKm.value = _tripCostEuro.value / dist
        } else {
            _costPerKm.value = (avgL100km / 100.0) * _fuelPricePerLiter.value
        }
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
        val currentThreshold = _healthPrediction.value?.threshold ?: 0.0025
        _healthPrediction.value = HealthPredictionResponse(
            anomalyScore = 0.00035,
            threshold = currentThreshold,
            isAnomaly = false,
            status = "Normal",
            triggeredFeatures = emptyList(),
            featureErrors = emptyMap()
        )
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
        val notif = _notificationSettings.value
        if (_voiceAlertsEnabled.value && notif.anomalyVocalAlert) {
            ttsManager.speak("Warning: Vehicle anomaly detected in ${triggered.joinToString(", ")}.")
        }
        if (_visualAlertsEnabled.value && notif.generateAnomalyAlert) {
            showAlertWithTimeout(
                com.example.model.ActiveAlert(
                    id = "alert-anomaly",
                    title = "HEALTH ANOMALY DETECTED",
                    message = "Abnormal reading in ${triggered.joinToString(", ")}.",
                    actionAdvice = "Check sensor readings or run OBD-II diagnostic scan.",
                    severity = com.example.model.AlertSeverity.WARNING
                )
            )
        }
    }

    fun triggerTestAlert() {
        showAlertWithTimeout(
            com.example.model.ActiveAlert(
                id = "test-${System.currentTimeMillis()}",
                title = "SPEED LIMIT WARNING",
                message = "Current vehicle speed is 128 km/h.",
                actionAdvice = "Maximum recommended speed is 120 km/h. Ease throttle.",
                severity = com.example.model.AlertSeverity.WARNING
            )
        )
    }

    fun scanDtcs() {
        viewModelScope.launch {
            _isScanningDtcs.value = true
            delay(600) // Scan delay across ECU bus
            val result = repository.getDtcs()
            result.onSuccess { codes ->
                _dtcCodes.value = codes.map { com.example.utils.DtcDecoder.decode(it) }
            }.onFailure {
                // If backend call fails or no codes, preserve or check
            }
            _isScanningDtcs.value = false
        }
    }

    fun simulateTestDtc(code: String = "P0171") {
        viewModelScope.launch {
            val result = repository.setDtcs(listOf(code))
            result.onSuccess { codes ->
                _dtcCodes.value = codes.map { com.example.utils.DtcDecoder.decode(it) }
            }.onFailure {
                _dtcCodes.value = listOf(com.example.utils.DtcDecoder.decode(code))
            }
        }
    }

    fun clearDtcs() {
        viewModelScope.launch {
            repository.clearDtcs()
            _dtcCodes.value = emptyList()
        }
    }

    fun fetchDtcsFromBackend() {
        viewModelScope.launch {
            repository.getDtcs().onSuccess { codes ->
                _dtcCodes.value = codes.map { com.example.utils.DtcDecoder.decode(it) }
            }
        }
    }

    fun fetchGpsRouteAndSummary() {
        viewModelScope.launch {
            repository.getRoute().onSuccess { route ->
                _routeData.value = route
            }
            repository.getTripSummary().onSuccess { summary ->
                _tripSummary.value = summary
            }
        }
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

    // Lifecycle, Debounce & Benchmark state variables
    private var alertDismissJob: Job? = null
    private var tripStartedAnnounced = false
    private var tripEndedAnnounced = false
    private var engineWarmedUpAnnounced = false
    private var tripStationaryStartMs = 0L
    private var lastDrivingDurationAlertTime = 0L
    private var lastConsumptionAlertTime = 0L
    private var lastColdEngineAlertTime = 0L
    private var lastOverheatAlertTime = 0L
    private var lastHighRpmAlertTime = 0L
    private var lastMaxPowerAlertTime = 0L
    private var lastMaxTorqueAlertTime = 0L

    // Performance benchmark tracking
    private var accel0StartMs = 0L
    private var pickup40StartMs = 0L
    private var pickup60StartMs = 0L
    private var pickup80StartMs = 0L
    private var pickup90StartMs = 0L
    private var brake100StartMs = 0L
    private var brake50StartMs = 0L

    init {
        startObserving()
        startPollingFallback()
        fetchGpsRouteAndSummary()
        fetchDtcsFromBackend()
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

                    // Process DTCs pushed directly over WebSocket / live-data tick
                    tick.dtcs?.let { codes ->
                        _dtcCodes.value = codes.map { com.example.utils.DtcDecoder.decode(it) }
                    }

                    val now = System.currentTimeMillis()

                    // ====== Universal Dynamic Fuel Consumption & Burn Rate ======
                    val dtSeconds = if (lastFuelBurnTimestamp > 0L) {
                        ((now - lastFuelBurnTimestamp).coerceIn(100L, 3000L)) / 1000.0
                    } else {
                        1.0
                    }
                    lastFuelBurnTimestamp = now

                    // Flow rate in Litres per Hour
                    val flowLph = when {
                        tick.ml?.fuel?.fuelFlowLitersPerHour != null && tick.ml.fuel.fuelFlowLitersPerHour > 0.0 ->
                            tick.ml.fuel.fuelFlowLitersPerHour
                        tick.ml?.fuel?.fcrGs != null && tick.ml.fuel.fcrGs > 0.0 ->
                            (tick.ml.fuel.fcrGs / 730.0) * 3600.0
                        tick.data.maf > 0.1 ->
                            (tick.data.maf / 14.7 / 730.0) * 3600.0
                        tick.data.rpm > 100 ->
                            (tick.data.rpm / 2800.0) * (0.65 + (tick.data.throttlePos / 100.0) * 3.8)
                        else -> 0.65 // standard 1.6L warm engine idle
                    }

                    val deltaFuelBurnLiters = (flowLph / 3600.0) * dtSeconds
                    val tankCapacity = _vehicleProfile.value.tankCapacityLiters.coerceAtLeast(1.0)

                    // Decrement live tracked fuel level
                    val newFuelLiters = (_currentFuelLiters.value - deltaFuelBurnLiters).coerceAtLeast(0.0)
                    _currentFuelLiters.value = newFuelLiters
                    val newFuelPercent = ((newFuelLiters / tankCapacity) * 100.0).coerceIn(0.0, 100.0)
                    _currentFuelPercent.value = newFuelPercent
                    _vehicleProfile.value = _vehicleProfile.value.copy(fuelLeftPercent = newFuelPercent)

                    // Accumulate session/trip fuel consumed
                    val newTotalConsumed = _fuelConsumedTripLiters.value + deltaFuelBurnLiters
                    _fuelConsumedTripLiters.value = newTotalConsumed
                    cumulativeTripFuelUsedLitres = newTotalConsumed
                    _tripCostEuro.value = newTotalConsumed * _fuelPricePerLiter.value

                    // Distance step from speed (km)
                    val deltaSpeedDistKm = (tick.data.vss / 3600.0) * dtSeconds
                    if (deltaSpeedDistKm > 0.0) {
                        _tripDistanceKm.value += deltaSpeedDistKm
                    }

                    // Instantaneous Fuel Economy
                    val instantKmL = if (tick.data.vss > 2.0 && flowLph > 0.05) {
                        (tick.data.vss / flowLph).coerceIn(0.5, 50.0)
                    } else {
                        0.0
                    }
                    val instantL100km = if (instantKmL > 0.1) {
                        (100.0 / instantKmL).coerceIn(1.0, 45.0)
                    } else {
                        0.0
                    }
                    _instantFuelEconomyKmL.value = instantKmL
                    _instantFuelEconomyL100km.value = instantL100km

                    // Average Fuel Economy (km/L and L/100km)
                    val totalDist = _tripDistanceKm.value
                    val (avgKmL, avgL100km) = if (totalDist > 0.05 && newTotalConsumed > 0.005) {
                        val kmpl = (totalDist / newTotalConsumed).coerceIn(2.0, 35.0)
                        val l100 = (100.0 / kmpl).coerceIn(2.8, 30.0)
                        Pair(kmpl, l100)
                    } else {
                        val profileL100 = _vehicleProfile.value.consumptionL100km.coerceAtLeast(3.0)
                        Pair(100.0 / profileL100, profileL100)
                    }
                    _averageFuelEconomyKmL.value = avgKmL
                    _averageFuelEconomyL100km.value = avgL100km

                    // Estimated Range based on tank capacity, current fuel level and burn rate
                    val estRange = (newFuelLiters / avgL100km) * 100.0
                    _estimatedRangeKm.value = estRange.coerceAtLeast(0.0)

                    // Cost per km
                    if (totalDist > 0.05) {
                        _costPerKm.value = _tripCostEuro.value / totalDist
                    } else {
                        _costPerKm.value = (avgL100km / 100.0) * _fuelPricePerLiter.value
                    }

                    // Low Fuel & Range Alert check
                    val lowFuelPctThresh = _notificationSettings.value.lowFuelWarningThresholdPercent.toDouble()
                    val lowRangeKmThresh = _notificationSettings.value.fuelRangeAutonomyThresholdKm.toDouble()
                    if (!fuelAlertShown && (newFuelPercent < lowFuelPctThresh || estRange < lowRangeKmThresh)) {
                        fuelAlertShown = true
                        showFuelAlert(newFuelLiters, estRange, avgL100km)
                    }

                    // Process Live GPS coordinates & Trip Tick Buffer
                    if (tick.data.hasGps) {
                        val lat = tick.data.lat
                        val lon = tick.data.lon
                        if (lat != null && lon != null) {
                            val currentGpsList = _liveGpsTrail.value.toMutableList()
                            val firstPoint = currentGpsList.firstOrNull()
                            val lastPoint = currentGpsList.lastOrNull()
                            if (firstPoint != null && lastPoint != null) {
                                val distFromStart = haversineKm(firstPoint.first, firstPoint.second, lat, lon)
                                val lastDistFromStart = haversineKm(firstPoint.first, firstPoint.second, lastPoint.first, lastPoint.second)
                                val jumpKm = haversineKm(lastPoint.first, lastPoint.second, lat, lon)
                                if (jumpKm > 3.0 || (currentGpsList.size > 15 && lastDistFromStart > 0.5 && distFromStart < 0.08)) {
                                    currentGpsList.clear()
                                    _tripTickBuffer.value = emptyList()
                                }
                            } else if (lastPoint != null) {
                                val jumpKm = haversineKm(lastPoint.first, lastPoint.second, lat, lon)
                                if (jumpKm > 3.0) {
                                    currentGpsList.clear()
                                    _tripTickBuffer.value = emptyList()
                                }
                            }

                            if (lastPoint == null || Math.abs(lastPoint.first - lat) > 0.00001 || Math.abs(lastPoint.second - lon) > 0.00001) {
                                currentGpsList.add(Pair(lat, lon))
                                if (currentGpsList.size > 2000) currentGpsList.removeAt(0)
                                _liveGpsTrail.value = currentGpsList
                            }

                            lastRecordedGpsPoint = Pair(lat, lon)

                            val drivingProfile = tick.drivingProfile 
                                ?: tick.data.drivingProfile 
                                ?: tick.ml?.driverBehaviour?.label 
                                ?: "NORMAL"

                            val anomalyScore = tick.anomalyScore 
                                ?: tick.data.anomalyScore 
                                ?: tick.ml?.health?.anomalyScore 
                                ?: 0.0

                            val tripItem = com.example.model.TripTickItem(
                                lat = lat,
                                lon = lon,
                                vss = tick.data.vss,
                                rpm = tick.data.rpm,
                                throttlePos = tick.data.throttlePos,
                                maf = tick.data.maf,
                                instantConsumption = if (instantL100km > 0.0) instantL100km else 6.5,
                                drivingProfile = drivingProfile,
                                anomalyScore = anomalyScore,
                                gpsBearing = tick.data.gpsBearing ?: 0.0,
                                elevationM = tick.data.elevationM
                            )

                            val currentTripBuffer = _tripTickBuffer.value.toMutableList()
                            val lastTripPoint = currentTripBuffer.lastOrNull()
                            if (lastTripPoint != null && lastTripPoint.lat != null && lastTripPoint.lon != null) {
                                val jumpKm = haversineKm(lastTripPoint.lat, lastTripPoint.lon, lat, lon)
                                if (jumpKm > 3.0) {
                                    currentTripBuffer.clear()
                                }
                            }
                            currentTripBuffer.add(tripItem)
                            if (currentTripBuffer.size > 2500) {
                                currentTripBuffer.removeAt(0)
                            }
                            _tripTickBuffer.value = currentTripBuffer
                        }
                    }

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
                            // STRICT REQUIREMENT: Only play voice warning when behaviour is aggressive
                            // Never speak "great driving", eco, or normal messages
                            val isAggressive = db.label.contains("aggressive", ignoreCase = true) ||
                                    db.behaviourClass.contains("aggressive", ignoreCase = true)
                            if (isAggressive) {
                                handleAggressiveDrivingAlert(db.ttsMessage)
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
        lastAnomalyAlertTime = now

        val notif = _notificationSettings.value
        val triggeredDesc = if (!health.triggeredFeatures.isNullOrEmpty()) {
            health.triggeredFeatures.joinToString(", ")
        } else {
            "critical engine sensors"
        }
        val alertMsg = "Warning: Vehicle anomaly detected in $triggeredDesc."
        if (_voiceAlertsEnabled.value && notif.anomalyVocalAlert) {
            ttsManager.speak(alertMsg)
        }
        if (_visualAlertsEnabled.value && notif.generateAnomalyAlert) {
            showAlertWithTimeout(
                com.example.model.ActiveAlert(
                    id = "alert-anomaly",
                    title = "HEALTH ANOMALY DETECTED",
                    message = "Abnormal reading in $triggeredDesc.",
                    actionAdvice = "Check sensor readings or run OBD-II diagnostic scan.",
                    severity = com.example.model.AlertSeverity.WARNING
                )
            )
        }
    }

    private fun handleAggressiveDrivingAlert(customMsg: String?) {
        val notif = _notificationSettings.value
        val now = System.currentTimeMillis()
        if (now - lastBehaviourTtsTime < 25000L) return
        lastBehaviourTtsTime = now

        val vocalText = if (!customMsg.isNullOrBlank() && (customMsg.contains("aggressive", ignoreCase = true) || customMsg.contains("harsh", ignoreCase = true) || customMsg.contains("throttle", ignoreCase = true))) {
            customMsg
        } else {
            "Warning: Aggressive driving behaviour detected. Smooth out pedal input."
        }

        if (_voiceAlertsEnabled.value && notif.styleDrivingVocalMessage) {
            ttsManager.speak(vocalText)
        }

        if (_visualAlertsEnabled.value && notif.generateStyleDrivingMessage) {
            showAlertWithTimeout(
                com.example.model.ActiveAlert(
                    id = "alert-aggressive-driving",
                    title = "AGGRESSIVE DRIVING DETECTED",
                    message = "High throttle acceleration and dynamic load detected. Ease pedal.",
                    actionAdvice = "Smooth pedal transitions conserve fuel and protect powertrain.",
                    severity = com.example.model.AlertSeverity.WARNING
                )
            )
        }
    }

    fun showAlertWithTimeout(alert: com.example.model.ActiveAlert) {
        _activeAlert.value = alert
        _visualAlertEvent.tryEmit(alert.message)

        alertDismissJob?.cancel()
        if (alert.severity != com.example.model.AlertSeverity.CRITICAL) {
            val timeoutSec = _notificationSettings.value.timeOutDisplayMessageSec.coerceIn(1, 15)
            alertDismissJob = viewModelScope.launch {
                delay(timeoutSec * 1000L)
                if (_activeAlert.value?.id == alert.id) {
                    _activeAlert.value = null
                }
            }
        }
    }

    private fun checkAndSpeakAlerts(tick: TelemetryTick) {
        val d = tick.data
        val notif = _notificationSettings.value
        val now = System.currentTimeMillis()

        // Auto clear speed/rpm alerts when back in safe envelope
        val speedLimit = notif.overspeedingMessageOverKmh.toDouble()
        if (d.vss <= speedLimit && d.rpm <= 6000 && d.coolantTemp <= 100) {
            val current = _activeAlert.value
            if (current != null && (current.id == "alert-speed" || current.id == "alert-rpm" || current.id == "alert-coolant")) {
                _activeAlert.value = null
            }
        }

        // 1. Trip Start / End Alert & Voice
        if (d.vss > 5.0 && !tripStartedAnnounced) {
            tripStartedAnnounced = true
            tripEndedAnnounced = false
            if (_voiceAlertsEnabled.value && notif.startTripVocalMessage) {
                ttsManager.speak("Trip started. Drive safely.")
            }
            if (_visualAlertsEnabled.value && notif.generateStartTripMessage) {
                showAlertWithTimeout(
                    com.example.model.ActiveAlert(
                        id = "alert-trip-start",
                        title = "TRIP STARTED",
                        message = "Trip telemetry recording initiated. Drive safely.",
                        actionAdvice = "Vehicle speed and engine parameters are monitored.",
                        severity = com.example.model.AlertSeverity.INFO
                    )
                )
            }
        } else if (tripStartedAnnounced && !tripEndedAnnounced && d.vss < 1.0 && _tripDistanceKm.value > 0.2) {
            if (tripStationaryStartMs == 0L) {
                tripStationaryStartMs = now
            } else if (now - tripStationaryStartMs > 15000L) {
                tripEndedAnnounced = true
                if (_voiceAlertsEnabled.value && notif.endTripVocalMessage) {
                    ttsManager.speak("Trip completed. Drive safely.")
                }
                if (_visualAlertsEnabled.value && notif.generateEndTripMessage) {
                    showAlertWithTimeout(
                        com.example.model.ActiveAlert(
                            id = "alert-trip-end",
                            title = "TRIP COMPLETED",
                            message = "Trip concluded. Total distance: ${String.format(Locale.US, "%.1f", _tripDistanceKm.value)} km.",
                            actionAdvice = "Review trip statistics and fuel economy summary.",
                            severity = com.example.model.AlertSeverity.INFO
                        )
                    )
                }
            }
        } else if (d.vss >= 1.0) {
            tripStationaryStartMs = 0L
        }

        // 2. Driving Duration Reminder Alert
        if (notif.drivingDurationMessageMin > 0 && _tripElapsedTimeMinutes.value >= notif.drivingDurationMessageMin) {
            if (now - lastDrivingDurationAlertTime > 1800000L) {
                lastDrivingDurationAlertTime = now
                if (_voiceAlertsEnabled.value && notif.drivingDurationVocalAlert) {
                    ttsManager.speak("You have been driving for ${_tripElapsedTimeMinutes.value} minutes. Please take a rest break.")
                }
                if (_visualAlertsEnabled.value && notif.generateDrivingDurationAlert) {
                    showAlertWithTimeout(
                        com.example.model.ActiveAlert(
                            id = "alert-driving-duration",
                            title = "DRIVER FATIGUE REMINDER",
                            message = "You have been driving for ${_tripElapsedTimeMinutes.value} minutes.",
                            actionAdvice = "Consider taking a short rest break to stay alert.",
                            severity = com.example.model.AlertSeverity.INFO
                        )
                    )
                }
            }
        }

        // 3. High Instantaneous Fuel Consumption Alert
        val speedKmh = d.vss
        val instantFcr = if (speedKmh > 2.0 && d.maf > 0.1) {
            ((d.maf / 14.7) / 730.0) * 3600.0 / speedKmh * 100.0
        } else 0.0

        if (instantFcr > 16.0 && d.throttlePos > 65.0 && now - lastConsumptionAlertTime > 45000L) {
            lastConsumptionAlertTime = now
            if (_voiceAlertsEnabled.value && notif.consumptionVocalMessage) {
                ttsManager.speak("High fuel consumption detected.")
            }
            if (_visualAlertsEnabled.value && notif.generateConsumptionMessage) {
                showAlertWithTimeout(
                    com.example.model.ActiveAlert(
                        id = "alert-high-consumption",
                        title = "HIGH FUEL CONSUMPTION",
                        message = "Current fuel rate is ${String.format(Locale.US, "%.1f", instantFcr)} L/100km.",
                        actionAdvice = "Ease throttle pedal and shift to higher gear to conserve fuel.",
                        severity = com.example.model.AlertSeverity.WARNING
                    )
                )
            }
        }

        // 4. Engine Coolant Temperature Alerts
        if (d.coolantTemp > 100.0 && now - lastOverheatAlertTime > 25000L) {
            lastOverheatAlertTime = now
            if (_voiceAlertsEnabled.value && notif.coolantOverheatVocalAlert) {
                ttsManager.speak("Critical Warning: Engine coolant temperature is too high. Please pull over safely.")
            }
            if (_visualAlertsEnabled.value && notif.generateCoolantOverheatAlert) {
                showAlertWithTimeout(
                    com.example.model.ActiveAlert(
                        id = "alert-coolant",
                        title = "COOLANT OVERHEAT",
                        message = "Coolant temperature is ${d.coolantTemp.toInt()}°C (Normal: 85-92°C).",
                        actionAdvice = "Pull over safely and shut off engine to prevent overheating.",
                        severity = com.example.model.AlertSeverity.CRITICAL
                    )
                )
            }
        } else if (d.coolantTemp in 85.0..92.0 && !engineWarmedUpAnnounced) {
            engineWarmedUpAnnounced = true
            if (_voiceAlertsEnabled.value && notif.engineOperatingTempVocalMessage) {
                ttsManager.speak(notif.warningTextEngineOperatingTemp)
            }
            if (_visualAlertsEnabled.value && notif.generateEngineOperatingTempMessage) {
                showAlertWithTimeout(
                    com.example.model.ActiveAlert(
                        id = "alert-operating-temp",
                        title = "ENGINE AT OPERATING TEMPERATURE",
                        message = notif.warningTextEngineOperatingTemp,
                        actionAdvice = "Engine is fully warmed up and at peak thermodynamic efficiency.",
                        severity = com.example.model.AlertSeverity.INFO
                    )
                )
            }
        } else if (d.coolantTemp < 60.0 && d.rpm > 3500.0 && now - lastColdEngineAlertTime > 40000L) {
            lastColdEngineAlertTime = now
            if (_voiceAlertsEnabled.value && notif.lowEngineTempVocalAlert) {
                ttsManager.speak(notif.warningTextLowEngineTemp)
            }
            if (_visualAlertsEnabled.value && notif.generateLowEngineTempMessage) {
                showAlertWithTimeout(
                    com.example.model.ActiveAlert(
                        id = "alert-low-temp",
                        title = "COLD ENGINE WARNING",
                        message = notif.warningTextLowEngineTemp,
                        actionAdvice = "Keep RPM below 3000 until engine reaches operating temperature.",
                        severity = com.example.model.AlertSeverity.WARNING
                    )
                )
            }
        }

        // 5. Engine RPM High Alert
        if (d.rpm > 6000.0 && now - lastHighRpmAlertTime > 20000L) {
            lastHighRpmAlertTime = now
            if (_voiceAlertsEnabled.value && notif.highRpmVocalAlert) {
                ttsManager.speak("Warning: High engine RPM detected. Shift to higher gear.")
            }
            if (_visualAlertsEnabled.value && notif.generateHighRpmAlert) {
                showAlertWithTimeout(
                    com.example.model.ActiveAlert(
                        id = "alert-rpm",
                        title = "HIGH ENGINE RPM",
                        message = "Engine speed reached ${d.rpm.toInt()} RPM near redline.",
                        actionAdvice = "Shift to higher gear to protect engine.",
                        severity = com.example.model.AlertSeverity.WARNING
                    )
                )
            }
        }

        // 6. Overspeeding Alert
        val overspeedDebounceMs = (notif.overspeedingAlertTimeOutSec * 1000L).coerceAtLeast(8000L)
        if (d.vss > speedLimit && now - lastVoiceAlertTime > overspeedDebounceMs) {
            lastVoiceAlertTime = now
            val alertMsg = notif.overspeedingAlertText.ifEmpty {
                "Slow down. Current speed is ${d.vss.toInt()} km/h."
            }
            if (_voiceAlertsEnabled.value && notif.overspeedingVocalAlert) {
                ttsManager.speak(alertMsg)
            }
            if (_visualAlertsEnabled.value && notif.generateOverspeedingAlert) {
                showAlertWithTimeout(
                    com.example.model.ActiveAlert(
                        id = "alert-speed",
                        title = "OVERSPEEDING ALERT",
                        message = "${alertMsg} (${d.vss.toInt()} km/h > ${speedLimit.toInt()} km/h limit).",
                        actionAdvice = "Ease throttle pedal to maintain safe stopping distance.",
                        severity = com.example.model.AlertSeverity.WARNING
                    )
                )
            }
        }

        // 7. Performance & Dynamics Benchmarks
        checkPerformanceBenchmarks(d, notif, now)
    }

    private fun checkPerformanceBenchmarks(d: TelemetryData, notif: com.example.model.NotificationSettings, now: Long) {
        val speed = d.vss

        // Track 0 to 100 km/h acceleration
        if (speed < 5.0 && d.throttlePos > 30.0) {
            accel0StartMs = now
        } else if (speed >= 100.0 && accel0StartMs > 0L) {
            val elapsedSec = (now - accel0StartMs) / 1000.0
            accel0StartMs = 0L
            if (elapsedSec in 2.5..35.0) {
                if (_voiceAlertsEnabled.value && notif.acceleration0To100VocalMessage) {
                    ttsManager.speak("Zero to one hundred kilometers per hour achieved in ${String.format(Locale.US, "%.1f", elapsedSec)} seconds.")
                }
                if (_visualAlertsEnabled.value && notif.generateAccel0To100Message) {
                    showAlertWithTimeout(
                        com.example.model.ActiveAlert(
                            id = "alert-accel-100",
                            title = "ACCELERATION 0-100 KM/H",
                            message = "0 to 100 km/h completed in ${String.format(Locale.US, "%.1f", elapsedSec)} s.",
                            actionAdvice = "Benchmark recorded in performance log.",
                            severity = com.example.model.AlertSeverity.INFO
                        )
                    )
                }
            }
        }

        // Track 0 to 50 km/h acceleration
        if (speed >= 50.0 && accel0StartMs > 0L && speed < 60.0) {
            val elapsedSec = (now - accel0StartMs) / 1000.0
            if (elapsedSec in 1.2..15.0 && (notif.generateAccel0To50Message || notif.accel0To50VocalMessage)) {
                if (_voiceAlertsEnabled.value && notif.accel0To50VocalMessage) {
                    ttsManager.speak("Zero to fifty kilometers per hour achieved in ${String.format(Locale.US, "%.1f", elapsedSec)} seconds.")
                }
                if (_visualAlertsEnabled.value && notif.generateAccel0To50Message) {
                    showAlertWithTimeout(
                        com.example.model.ActiveAlert(
                            id = "alert-accel-50",
                            title = "ACCELERATION 0-50 KM/H",
                            message = "0 to 50 km/h completed in ${String.format(Locale.US, "%.1f", elapsedSec)} s.",
                            actionAdvice = "Launch benchmark recorded.",
                            severity = com.example.model.AlertSeverity.INFO
                        )
                    )
                }
            }
        }

        // Pickup 40 to 70 km/h
        if (speed in 38.0..42.0 && d.throttlePos > 35.0) {
            pickup40StartMs = now
        } else if (speed >= 70.0 && pickup40StartMs > 0L) {
            val elapsed = (now - pickup40StartMs) / 1000.0
            pickup40StartMs = 0L
            if (elapsed in 1.0..20.0) {
                if (_voiceAlertsEnabled.value && notif.pickup40To70VocalMessage) {
                    ttsManager.speak("Pickup 40 to 70 completed in ${String.format(Locale.US, "%.1f", elapsed)} seconds.")
                }
                if (_visualAlertsEnabled.value && notif.generatePickup40To70Message) {
                    showAlertWithTimeout(
                        com.example.model.ActiveAlert(
                            id = "alert-pickup-40-70",
                            title = "PICKUP 40-70 KM/H",
                            message = "Pickup 40-70 km/h achieved in ${String.format(Locale.US, "%.1f", elapsed)} s.",
                            actionAdvice = "In-gear acceleration benchmark logged.",
                            severity = com.example.model.AlertSeverity.INFO
                        )
                    )
                }
            }
        }

        // Pickup 60 to 100 km/h
        if (speed in 58.0..62.0 && d.throttlePos > 35.0) {
            pickup60StartMs = now
        } else if (speed >= 100.0 && pickup60StartMs > 0L) {
            val elapsed = (now - pickup60StartMs) / 1000.0
            pickup60StartMs = 0L
            if (elapsed in 1.5..25.0) {
                if (_voiceAlertsEnabled.value && notif.pickup60To100VocalMessage) {
                    ttsManager.speak("Pickup 60 to 100 completed in ${String.format(Locale.US, "%.1f", elapsed)} seconds.")
                }
                if (_visualAlertsEnabled.value && notif.generatePickup60To100Message) {
                    showAlertWithTimeout(
                        com.example.model.ActiveAlert(
                            id = "alert-pickup-60-100",
                            title = "PICKUP 60-100 KM/H",
                            message = "Pickup 60-100 km/h achieved in ${String.format(Locale.US, "%.1f", elapsed)} s.",
                            actionAdvice = "Overtaking performance logged.",
                            severity = com.example.model.AlertSeverity.INFO
                        )
                    )
                }
            }
        }

        // Pickup 80 to 120 km/h
        if (speed in 78.0..82.0 && d.throttlePos > 35.0) {
            pickup80StartMs = now
        } else if (speed >= 120.0 && pickup80StartMs > 0L) {
            val elapsed = (now - pickup80StartMs) / 1000.0
            pickup80StartMs = 0L
            if (elapsed in 2.0..30.0) {
                if (_voiceAlertsEnabled.value && notif.pickup80To120VocalMessage) {
                    ttsManager.speak("Pickup 80 to 120 completed in ${String.format(Locale.US, "%.1f", elapsed)} seconds.")
                }
                if (_visualAlertsEnabled.value && notif.generatePickup80To120Message) {
                    showAlertWithTimeout(
                        com.example.model.ActiveAlert(
                            id = "alert-pickup-80-120",
                            title = "PICKUP 80-120 KM/H",
                            message = "Pickup 80-120 km/h achieved in ${String.format(Locale.US, "%.1f", elapsed)} s.",
                            actionAdvice = "Highway passing test logged.",
                            severity = com.example.model.AlertSeverity.INFO
                        )
                    )
                }
            }
        }

        // Pickup 90 to 130 km/h
        if (speed in 88.0..92.0 && d.throttlePos > 35.0) {
            pickup90StartMs = now
        } else if (speed >= 130.0 && pickup90StartMs > 0L) {
            val elapsed = (now - pickup90StartMs) / 1000.0
            pickup90StartMs = 0L
            if (elapsed in 2.0..35.0) {
                if (_voiceAlertsEnabled.value && notif.pickup90To130VocalMessage) {
                    ttsManager.speak("Pickup 90 to 130 completed in ${String.format(Locale.US, "%.1f", elapsed)} seconds.")
                }
                if (_visualAlertsEnabled.value && notif.generatePickup90To130Message) {
                    showAlertWithTimeout(
                        com.example.model.ActiveAlert(
                            id = "alert-pickup-90-130",
                            title = "PICKUP 90-130 KM/H",
                            message = "Pickup 90-130 km/h achieved in ${String.format(Locale.US, "%.1f", elapsed)} s.",
                            actionAdvice = "High-speed passing test logged.",
                            severity = com.example.model.AlertSeverity.INFO
                        )
                    )
                }
            }
        }

        // Braking 100 to 0 km/h
        if (speed > 98.0) {
            brake100StartMs = now
        } else if (speed < 3.0 && brake100StartMs > 0L) {
            val elapsed = (now - brake100StartMs) / 1000.0
            brake100StartMs = 0L
            if (elapsed in 1.5..15.0) {
                if (_voiceAlertsEnabled.value && notif.brakingVocalAlert) {
                    ttsManager.speak("Braking 100 to zero completed in ${String.format(Locale.US, "%.1f", elapsed)} seconds.")
                }
                if (_visualAlertsEnabled.value && notif.generateBraking100To0Message) {
                    showAlertWithTimeout(
                        com.example.model.ActiveAlert(
                            id = "alert-brake-100",
                            title = "BRAKING 100-0 KM/H",
                            message = "Deceleration 100-0 km/h achieved in ${String.format(Locale.US, "%.1f", elapsed)} s.",
                            actionAdvice = "Emergency brake benchmark logged.",
                            severity = com.example.model.AlertSeverity.INFO
                        )
                    )
                }
            }
        }

        // Braking 50 to 0 km/h
        if (speed in 48.0..54.0 && brake100StartMs == 0L) {
            brake50StartMs = now
        } else if (speed < 3.0 && brake50StartMs > 0L) {
            val elapsed = (now - brake50StartMs) / 1000.0
            brake50StartMs = 0L
            if (elapsed in 1.0..10.0) {
                if (_visualAlertsEnabled.value && notif.generateBraking50To0Message) {
                    showAlertWithTimeout(
                        com.example.model.ActiveAlert(
                            id = "alert-brake-50",
                            title = "BRAKING 50-0 KM/H",
                            message = "Braking 50-0 km/h achieved in ${String.format(Locale.US, "%.1f", elapsed)} s.",
                            actionAdvice = "City stop benchmark logged.",
                            severity = com.example.model.AlertSeverity.INFO
                        )
                    )
                }
            }
        }

        // Max Power Benchmark
        if (d.rpm > 5200.0 && d.throttlePos > 85.0 && now - lastMaxPowerAlertTime > 45000L) {
            lastMaxPowerAlertTime = now
            if (_voiceAlertsEnabled.value && notif.maxPowerVocalMessage) {
                ttsManager.speak("Peak engine power reached.")
            }
            if (_visualAlertsEnabled.value && notif.generateMaxPowerMessage) {
                showAlertWithTimeout(
                    com.example.model.ActiveAlert(
                        id = "alert-max-power",
                        title = "PEAK POWER REACHED",
                        message = "Maximum engine power envelope accessed at ${d.rpm.toInt()} RPM.",
                        actionAdvice = "Engine operating near peak horsepower output.",
                        severity = com.example.model.AlertSeverity.INFO
                    )
                )
            }
        }

        // Max Torque Benchmark
        if (d.rpm in 2200.0..3600.0 && d.throttlePos > 80.0 && now - lastMaxTorqueAlertTime > 45000L) {
            lastMaxTorqueAlertTime = now
            if (_visualAlertsEnabled.value && notif.generateMaxTorqueMessage) {
                showAlertWithTimeout(
                    com.example.model.ActiveAlert(
                        id = "alert-max-torque",
                        title = "PEAK TORQUE REACHED",
                        message = "Peak engine torque sweet-spot achieved (${d.rpm.toInt()} RPM).",
                        actionAdvice = "Maximum pulling power available.",
                        severity = com.example.model.AlertSeverity.INFO
                    )
                )
            }
        }
    }

    fun updateNotificationSettings(transform: (com.example.model.NotificationSettings) -> com.example.model.NotificationSettings) {
        _notificationSettings.value = transform(_notificationSettings.value)
    }

    fun setNotificationSettings(settings: com.example.model.NotificationSettings) {
        _notificationSettings.value = settings
    }

    fun activateAllVisualAlerts() {
        _visualAlertsEnabled.value = true
        _notificationSettings.value = _notificationSettings.value.copy(
            generateStartTripMessage = true,
            generateEndTripMessage = true,
            generateConsumptionMessage = true,
            generateStyleDrivingMessage = true,
            generateLackOfAutonomyAlert = true,
            generateDrivingDurationAlert = true,
            generateHighRpmAlert = true,
            generateCoolantOverheatAlert = true,
            generateAnomalyAlert = true,
            generateEngineOperatingTempMessage = true,
            generateLowEngineTempMessage = true,
            generateOverspeedingAlert = true,
            generateMaxPowerMessage = true,
            generateMaxTorqueMessage = true,
            generateAccel0To50Message = true,
            generateAccel0To100Message = true,
            generatePickup40To70Message = true,
            generatePickup60To100Message = true,
            generatePickup80To120Message = true,
            generatePickup90To130Message = true,
            generateBraking100To0Message = true,
            generateBraking50To0Message = true,
            generateVehicleRangeAlert = true
        )
    }

    fun deactivateAllVisualAlerts() {
        _visualAlertsEnabled.value = false
        _notificationSettings.value = _notificationSettings.value.copy(
            generateStartTripMessage = false,
            generateEndTripMessage = false,
            generateConsumptionMessage = false,
            generateStyleDrivingMessage = false,
            generateLackOfAutonomyAlert = false,
            generateDrivingDurationAlert = false,
            generateHighRpmAlert = false,
            generateCoolantOverheatAlert = false,
            generateAnomalyAlert = false,
            generateEngineOperatingTempMessage = false,
            generateLowEngineTempMessage = false,
            generateOverspeedingAlert = false,
            generateMaxPowerMessage = false,
            generateMaxTorqueMessage = false,
            generateAccel0To50Message = false,
            generateAccel0To100Message = false,
            generatePickup40To70Message = false,
            generatePickup60To100Message = false,
            generatePickup80To120Message = false,
            generatePickup90To130Message = false,
            generateBraking100To0Message = false,
            generateBraking50To0Message = false,
            generateVehicleRangeAlert = false
        )
        _activeAlert.value = null
    }

    fun activateAllVocalAlerts() {
        _voiceAlertsEnabled.value = true
        _notificationSettings.value = _notificationSettings.value.copy(
            startTripVocalMessage = true,
            endTripVocalMessage = true,
            consumptionVocalMessage = true,
            styleDrivingVocalMessage = true,
            lackOfAutonomyVocalAlert = true,
            vehicleRangeVocalAlert = true,
            drivingDurationVocalAlert = true,
            engineOperatingTempVocalMessage = true,
            lowEngineTempVocalAlert = true,
            overspeedingVocalAlert = true,
            highRpmVocalAlert = true,
            coolantOverheatVocalAlert = true,
            anomalyVocalAlert = true,
            acceleration0To100VocalMessage = true,
            accel0To50VocalMessage = true,
            pickup40To70VocalMessage = true,
            pickup60To100VocalMessage = true,
            pickup80To120VocalMessage = true,
            pickup90To130VocalMessage = true,
            brakingVocalAlert = true,
            maxPowerVocalMessage = true
        )
    }

    fun deactivateAllVocalAlerts() {
        _voiceAlertsEnabled.value = false
        _notificationSettings.value = _notificationSettings.value.copy(
            startTripVocalMessage = false,
            endTripVocalMessage = false,
            consumptionVocalMessage = false,
            styleDrivingVocalMessage = false,
            lackOfAutonomyVocalAlert = false,
            drivingDurationVocalAlert = false,
            engineOperatingTempVocalMessage = false,
            lowEngineTempVocalAlert = false,
            overspeedingVocalAlert = false,
            highRpmVocalAlert = false,
            coolantOverheatVocalAlert = false,
            anomalyVocalAlert = false,
            acceleration0To100VocalMessage = false,
            accel0To50VocalMessage = false,
            pickup40To70VocalMessage = false,
            pickup60To100VocalMessage = false,
            pickup80To120VocalMessage = false,
            pickup90To130VocalMessage = false,
            brakingVocalAlert = false,
            maxPowerVocalMessage = false
        )
    }

    fun testVoiceAlert(sampleText: String = "Warning: Aggressive driving behaviour detected. Ease throttle pedal.") {
        if (_voiceAlertsEnabled.value) {
            ttsManager.speak(sampleText)
        }
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
            var counter = 0
            while (isActive) {
                val now = System.currentTimeMillis()
                // If we haven't received a tick in the last 1500ms, fetch via REST polling
                if (now - lastTickTimestamp > 1500) {
                    val result = repository.getLiveData()
                    if (result.isSuccess && result.getOrNull() != null) {
                        lastTickTimestamp = System.currentTimeMillis()
                    }
                }
                counter++
                if (counter % 4 == 0) {
                    fetchDtcsFromBackend()
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
            resetTripSession()
            repository.changeDataset(datasetId)
            fetchSimulatorStatus()
            fetchGpsRouteAndSummary()
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
            val isAggressive = response?.label?.contains("aggressive", ignoreCase = true) == true ||
                    response?.behaviourClass?.contains("aggressive", ignoreCase = true) == true
            if (isAggressive) {
                handleAggressiveDrivingAlert(response?.ttsMessage)
            }
        } else {
            // Local Classifier Fallback
            val fallbackResponse = generateFallbackBehaviour(fallbackTick)
            _driverBehaviour.value = fallbackResponse
            val isAggressive = fallbackResponse.label.contains("aggressive", ignoreCase = true)
            if (isAggressive) {
                handleAggressiveDrivingAlert(fallbackResponse.ttsMessage)
            }
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
