# AutoVue - Vehicle Telemetry & Analytics Platform

AutoVue is an advanced Android application built to provide real-time vehicle telemetry monitoring and predictive insights using Machine Learning. It interfaces with an external backend to stream live OBD-II data and perform complex analysis for driver behavior and vehicle health.

## Key Features

- **Live Dashboard**: Real-time streaming of OBD-II telemetry parameters. Features a custom Analog Instrument Cluster displaying Speed, RPM, and Coolant Temperature alongside digital metrics.
- **Engine Data Visualisation (Oscilloscope Graphs)**: Dedicated real-time data visualization tab styled after professional automotive diagnostic oscilloscopes. Features live scrolling multi-point line graphs for RPM, Speed, Intake Air Temp, Calculated Load, Coolant Temp, MAP, MAF, Ambient Temp, and Accelerator Pedals, along with the cluster overview and top-bar playback controls.
- **AI Insights**:
  - **Driver Behavior Analysis**: Utilizes a rolling window of telemetry data and a pre-trained KMeans clustering model to classify driving patterns.
  - **Vehicle Health Prediction**: Leverages a Random Forest model on single telemetry snapshots to evaluate overall vehicle health and output probability confidences.
- **Maintenance Portal**: A dedicated module for tracking vehicle maintenance needs. Features ECU Trouble Code decoding, manual/automatic fault ticket registration, and integration placeholders for dispatching tickets to service partners or Telegram gateways.
- **Voice Alerts (TTS)**: Integrated Android Text-to-Speech (TTS) engine to vocalize critical warnings (e.g., speeding, high RPM, or engine overheating) directly to the driver, configurable via the Settings tab.
- **User & System Configuration**: Manage driver profiles, vehicle models, TTS preferences, and ping the remote Render-hosted backend instance for cold-start wakeups.

## Architecture & Tech Stack

- **Framework**: Android, Jetpack Compose (Material Design 3)
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles
- **Asynchronous Operations**: Kotlin Coroutines and Flow
- **Networking**:
  - **WebSockets**: OkHttp for real-time telemetry streaming (`wss://.../api/ws/live`)
  - **REST API**: Retrofit for ML inference and backend health checks
- **Data Serialization**: Moshi

## ML Endpoints Integration

AutoVue integrates with specialized ML endpoints hosted on a remote server:

- `POST /api/driver/predict`: Classifies driver behavior based on time-series arrays of RPM, Speed, and Throttle data.
- `POST /api/health/predict`: Classifies the health status (e.g., Normal vs Warning) from a snapshot of 8 key telemetry metrics.

## Getting Started

1. Clone the repository and open the project in Android Studio.
2. The default backend URL is configured to `https://ecu-backend-95fz.onrender.com/`. If deploying a custom backend, update the `defaultBaseUrl` in `AppContainer.kt`.
3. Build and run on an Android device or emulator.
4. Navigate to the **User** tab to wake up the backend if it is currently asleep (using the "Ping Backend Server" button).
