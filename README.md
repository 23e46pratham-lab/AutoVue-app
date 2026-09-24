# AutoVue - Intelligent Vehicle Telemetry & Diagnostics Platform

AutoVue is a high-performance Android telemetry and diagnostic cockpit application built with Jetpack Compose and Material Design 3. It interfaces with vehicle OBD-II interfaces and predictive Machine Learning backends to provide real-time engine instrumentation, AI-powered health and driver behavior analytics, physics-based fuel modeling, trip GPS mapping, and diagnostic fault (DTC) management.

---

## 🏎️ Key Features & Modules

### 1. High-Performance Digital & Analog Cockpit
- **Dual Analog-Digital Instrument Cluster**: Custom canvas-rendered analog speedometer with dynamic sweep needle, tachometer with RPM redline threshold, coolant temperature gauge with cold/overheat zones, and fuel percentage meter.
- **Power & Torque Curves**: Instantaneous power (HP / cv) and engine torque (Nm) calculation plotted against peak power and torque RPM curves.
- **Trip Computer HUD**: Live calculation of elapsed trip time, distance (km), fuel expenditure cost (€), instant vs average fuel consumption (L/100km), instant speed, and eco-driving score (%).
- **Automotive Styling**: High-contrast UI styled with Lamborghini Urus Yellow (*Giallo Auge*) accents, Nero Noctis trims, and custom 2x2 Twill Carbon Fiber weave panels with clear-coat gloss overlays.
- **Responsive Layout Engine**: Dynamic adaptive layout with defensive text truncation and single-line constraints to ensure perfect presentation across varying phone aspect ratios and screen densities.

### 2. Live Telemetry & Diagnostic Sensor Grid
- **Real-Time OBD-II Parameter Streaming**: Continuous streaming of live PID values over WebSocket (`wss://`) with automatic REST polling fallback:
  - Vehicle Speed (VSS), Engine RPM, Throttle Position (%)
  - Engine Coolant Temperature (°C), Intake Air Temperature (IAT)
  - Calculated Engine Load (%), Barometric & Ambient Temperature
  - Manifold Absolute Pressure (MAP), Mass Air Flow (MAF)
- **Protocol Status & Telemetry Speed**: Displays active OBD-II protocol (e.g. `ISO 15765-4 CAN 11/500`) and PID throughput rate (`PID/s`).

### 3. Oscilloscope & Multi-Channel Waveform Visualizer
- **Diagnostic Strip Chart Recorder**: Real-time scrolling telemetry waveforms designed after professional automotive diagnostic oscilloscopes.
- **Configurable Waveforms**: Multi-point inspection for RPM, Speed, Intake Air Temp, Engine Load, Coolant Temp, MAP, MAF, and Accelerator Pedal position.
- **Playback & Scrubber Controls**: Waveform timeline pause/resume, zoom scaling, and peak value indicators.

### 4. AI & Machine Learning Insights
- **XGBoost Driver Behavior Profiling**: Classifies driving style into *Economical*, *Normal*, or *Aggressive* using rolling time-series telemetry windows with percentage confidence and coaching recommendations.
- **Predictive Health & Anomaly Detection**: Evaluates multidimensional sensor snapshots to identify early mechanical anomalies, highlighting specific triggered features and error contributions before critical failure occurs.
- **Physics-Based Fuel Consumption & Mileage Engine**:
  - **Tier 1 (MAF Physics)**: Direct calculation from Mass Air Flow and stoichiometric air-fuel ratios.
  - **Tier 2 (MAP Speed-Density)**: Volumetric efficiency calculation using Manifold Absolute Pressure and displacement.
  - Outputs instant consumption rate (L/100km and g/s), estimated range (km), and trip cost.

### 5. Smart Alert Engine & Voice Alerts (TTS)
- **Real-Time Alert Banner**: Prioritized visual notification system with severity tags (*CRITICAL*, *WARNING*, *INFO*) for overspeeding, engine overheating (>100°C), cold engine revving (<60°C @ >3500 RPM), and detected sensor anomalies.
- **Android Text-to-Speech (TTS)**: Intelligent vocal alerts with customizable cooldown debounce and independent toggles for speed, temperature, and anomaly announcements.

### 6. OBD-II DTC Trouble Code Scanner & Service Hub
- **SAE Standard DTC Decoder**: Scans and decodes powertrain (`P`), chassis (`C`), body (`B`), and network (`U`) Diagnostic Trouble Codes with symptom explanations and severity ratings.
- **Digital Service Ticket System**: Automatic or manual creation of maintenance tickets stamped with vehicle VIN, mileage, and fault descriptions.
- **Service Dispatch Gateway**: Integration support for dispatching tickets directly to service partners or Telegram notifications.

### 7. Trip Map & Refueling Awareness
- **GPS Route Tracking & Heatmaps**: Interactive route tracking with switchable display modes (Standard Path, Speed Heatmap, Driving Behavior Overlay, and Fuel Efficiency Trace).
- **Refueling Station Awareness**: Automatic low-fuel detection (<50 km estimated range) prompting drivers to find fuel stations or log refueling stops.
- **Refueling Diary**: Logs fuel quantity (L), fuel price per liter, total cost (€), and odometer readings.

### 8. Start-Stop Efficiency Optimizer
- Tracks engine shut-off time vs vehicle stationary time during idle/stoplights.
- Computes cumulative fuel saved (liters) and financial savings (€).

### 9. Driver Profile & Onboarding Wizard
- **Two-Page Setup Wizard**:
  - *Page 1*: Profile name, fuel supply type (gasoline, diesel, hybrid, electric, LPG), start-stop system status, displacement (cc), max power (HP), and odometer reading.
  - *Page 2*: Total curb weight (kg), manufacturer reference consumption, corrective factor, tank capacity (L), current fuel level (%), and VIN.
- **Multi-Profile Management**: Seamless switching between registered vehicle and driver profiles.

---

## 🛠️ Architecture & Tech Stack

- **Platform**: Android (Target SDK 34)
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Language**: Kotlin with Coroutines and StateFlow
- **Architecture**: Clean Architecture / MVVM (Model-View-ViewModel)
- **Networking**:
  - **WebSockets**: OkHttp WebSocket client for real-time live telemetry streaming (`/api/ws/live`)
  - **REST API**: Retrofit 2 + Moshi Converter for ML inference and backend health pings
- **Audio & Accessibility**: Android Text-to-Speech (`TextToSpeech`) Engine

---

## 🌐 Backend & ML API Integration

AutoVue integrates with remote backend telemetry and ML inference endpoints:

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/ws/live` | WebSocket | Real-time OBD-II telemetry broadcast stream |
| `/api/live-data` | GET | Single snapshot of the latest OBD-II telemetry parameters |
| `/api/history` | GET | Historical telemetry buffer for rolling window analysis |
| `/api/driver/predict` | POST | Classifies driver behavior from time-series window |
| `/api/health/predict` | POST | Evaluates vehicle health status and anomalous features |
| `/api/fuel/predict` | POST | Computes instantaneous physics-based fuel consumption |
| `/api/datasets` | GET | Lists available simulator datasets |
| `/api/datasets/change` | POST | Switches the live playback dataset in simulation mode |

---

## 🚀 Getting Started

1. Open the project in Android Studio (Jellyfish or newer recommended).
2. The default backend URL points to the hosted API service (`https://ecu-backend-95fz.onrender.com/`). To configure a custom endpoint, update `defaultBaseUrl` in `AppContainer.kt`.
3. Build and run on a physical Android device or emulator.
4. On first launch, select an existing driver profile or complete the 2-page registration wizard to calibrate vehicle displacement and fuel parameters.
5. In the top bar, use the **OBD Link** button to connect to the telemetry stream or select simulation datasets.
