package com.example.ui.dialogs

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitGreen
import com.example.ui.theme.CockpitRed
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private const val PREFS_NAME = "supabase_api_credentials"
private const val KEY_PROJECT_URL = "supabase_project_url"
private const val KEY_ANON_KEY = "supabase_anon_key"
private const val KEY_CUSTOM_HEADER = "supabase_custom_schema"

@Composable
fun ApiKeyInitializationDialog(
    onDismiss: () -> Unit,
    onKeysUpdated: ((url: String, anonKey: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var projectUrl by remember {
        mutableStateOf(prefs.getString(KEY_PROJECT_URL, "") ?: "")
    }
    var anonKey by remember {
        mutableStateOf(prefs.getString(KEY_ANON_KEY, "") ?: "")
    }
    var customSchema by remember {
        mutableStateOf(prefs.getString(KEY_CUSTOM_HEADER, "public") ?: "public")
    }

    var isKeyObscured by remember { mutableStateOf(true) }
    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var isTestSuccess by remember { mutableStateOf<Boolean?>(null) }
    var showHelpGuide by remember { mutableStateOf(false) }

    val isInitialized = projectUrl.isNotBlank() && anonKey.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(16.dp)),
            color = CockpitCard,
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CockpitSteel.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = CockpitSteel,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Initialize API Keys",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Supabase Database & Cloud Endpoints",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                // Status Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isInitialized) CockpitGreen.copy(alpha = 0.12f) else CockpitAmber.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isInitialized) CockpitGreen.copy(alpha = 0.4f) else CockpitAmber.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isInitialized) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isInitialized) CockpitGreen else CockpitAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isInitialized) "Supabase Configured" else "Keys Not Initialized",
                                color = if (isInitialized) CockpitGreen else CockpitAmber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isInitialized)
                                    "Project URL & API Key saved locally. Ready for cloud sync."
                                else
                                    "Enter your Supabase URL & anon key to enable database persistence.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Project URL Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "SUPABASE PROJECT URL",
                        color = CockpitSteel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = projectUrl,
                        onValueChange = {
                            projectUrl = it.trim()
                            testStatusMessage = null
                            isTestSuccess = null
                        },
                        placeholder = { Text("https://[ref].supabase.co", color = TextMuted, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CockpitCardElevated,
                            unfocusedContainerColor = CockpitCardElevated
                        ),
                        trailingIcon = {
                            if (projectUrl.isNotBlank()) {
                                IconButton(onClick = { projectUrl = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    )
                }

                // Supabase Anon / Public API Key
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "SUPABASE ANON / PUBLIC API KEY",
                        color = CockpitSteel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = anonKey,
                        onValueChange = {
                            anonKey = it.trim()
                            testStatusMessage = null
                            isTestSuccess = null
                        },
                        placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", color = TextMuted, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (isKeyObscured) PasswordVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CockpitCardElevated,
                            unfocusedContainerColor = CockpitCardElevated
                        ),
                        trailingIcon = {
                            IconButton(onClick = { isKeyObscured = !isKeyObscured }) {
                                Icon(
                                    imageVector = if (isKeyObscured) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isKeyObscured) "Show key" else "Hide key",
                                    tint = CockpitSteel,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }

                // Schema Name (Optional / Advanced)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "DATABASE SCHEMA (DEFAULT: public)",
                        color = CockpitSteel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = customSchema,
                        onValueChange = { customSchema = it.trim() },
                        placeholder = { Text("public", color = TextMuted, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CockpitCardElevated,
                            unfocusedContainerColor = CockpitCardElevated
                        )
                    )
                }

                // Test Connection Status Message
                if (testStatusMessage != null) {
                    val statusColor = when (isTestSuccess) {
                        true -> CockpitGreen
                        false -> CockpitRed
                        else -> CockpitSteel
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CockpitSteel,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isTestSuccess == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = testStatusMessage.orEmpty(),
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Action Buttons: Test Connection & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (projectUrl.isBlank() || anonKey.isBlank()) {
                                testStatusMessage = "Please enter both Project URL and Anon Key first."
                                isTestSuccess = false
                                return@OutlinedButton
                            }
                            isTestingConnection = true
                            testStatusMessage = "Connecting to Supabase REST endpoint..."
                            isTestSuccess = null

                            coroutineScope.launch {
                                val cleanUrl = projectUrl.trim().removeSuffix("/")
                                val result = withContext(Dispatchers.IO) {
                                    try {
                                        val client = OkHttpClient.Builder()
                                            .connectTimeout(5, TimeUnit.SECONDS)
                                            .readTimeout(5, TimeUnit.SECONDS)
                                            .build()

                                        val request = Request.Builder()
                                            .url("$cleanUrl/rest/v1/")
                                            .addHeader("apikey", anonKey.trim())
                                            .addHeader("Authorization", "Bearer ${anonKey.trim()}")
                                            .build()

                                        val response = client.newCall(request).execute()
                                        val code = response.code
                                        response.close()

                                        if (code in 200..299 || code == 404 || code == 401 || code == 400) {
                                            if (code == 401) {
                                                Result.failure(Exception("HTTP 401: Invalid Anon Key for this Supabase project"))
                                            } else {
                                                Result.success("Endpoint reachable! HTTP $code (Supabase API active)")
                                            }
                                        } else {
                                            Result.failure(Exception("HTTP $code returned from server"))
                                        }
                                    } catch (e: Exception) {
                                        Result.failure(e)
                                    }
                                }

                                isTestingConnection = false
                                result.fold(
                                    onSuccess = { msg ->
                                        testStatusMessage = msg
                                        isTestSuccess = true
                                    },
                                    onFailure = { err ->
                                        testStatusMessage = "Connection failed: ${err.message}"
                                        isTestSuccess = false
                                    }
                                )
                            }
                        },
                        enabled = !isTestingConnection && projectUrl.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, CockpitSurfaceBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = CockpitSteel,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TEST PING", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            prefs.edit()
                                .putString(KEY_PROJECT_URL, projectUrl.trim())
                                .putString(KEY_ANON_KEY, anonKey.trim())
                                .putString(KEY_CUSTOM_HEADER, customSchema.trim())
                                .apply()

                            onKeysUpdated?.invoke(projectUrl.trim(), anonKey.trim())
                            Toast.makeText(context, "API Keys Saved Successfully", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CockpitSteel)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = CockpitCard,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE KEYS", color = CockpitCard, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Help Guide Accordion / Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHelpGuide = !showHelpGuide },
                    colors = CardDefaults.cardColors(containerColor = CockpitCardElevated),
                    border = BorderStroke(1.dp, CockpitSurfaceBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = CockpitSteel,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Where to get your Supabase keys?",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (showHelpGuide) "Hide" else "Show",
                                color = CockpitSteel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (showHelpGuide) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "1. Open your Supabase Dashboard at supabase.com\n" +
                                       "2. Click on your project and navigate to Project Settings (gear icon)\n" +
                                       "3. Click on 'API' in the configuration menu\n" +
                                       "4. Copy the 'Project URL' (e.g. https://xyz.supabase.co)\n" +
                                       "5. Under Project API Keys, copy the 'anon' / 'public' key\n" +
                                       "6. Paste both values here and tap 'SAVE KEYS'",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Reset / Clear option
                if (isInitialized) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Clear Saved API Keys",
                            color = CockpitRed.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable {
                                    prefs.edit().clear().apply()
                                    projectUrl = ""
                                    anonKey = ""
                                    testStatusMessage = "Saved keys cleared"
                                    isTestSuccess = null
                                    Toast.makeText(context, "API Keys Cleared", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
