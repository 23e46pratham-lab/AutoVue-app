package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navigation.AutoVueNavGraph
import com.example.ui.theme.AutoVueTheme
import com.example.viewmodel.SharedTelemetryViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val appContainer = (application as AutoVueApplication).container
    setContent {
      AutoVueTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val viewModel: SharedTelemetryViewModel = viewModel(
            factory = SharedTelemetryViewModel.provideFactory(
                appContainer.telemetryRepository,
                appContainer.ttsManager
            )
          )
          
          val context = androidx.compose.ui.platform.LocalContext.current
          androidx.compose.runtime.LaunchedEffect(viewModel.visualAlertEvent) {
              viewModel.visualAlertEvent.collect { alertMessage ->
                  android.widget.Toast.makeText(context, alertMessage, android.widget.Toast.LENGTH_SHORT).show()
              }
          }
          
          AutoVueNavGraph(viewModel = viewModel)
        }
      }
    }
  }
}
