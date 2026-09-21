package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CockpitOverflowMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelectQuickGuide: () -> Unit,
    onSelectRefueling: () -> Unit,
    onSelectSettings: () -> Unit,
    onSelectDtc: () -> Unit,
    onSelectLiveData: () -> Unit,
    onSelectHistory: () -> Unit,
    onSelectSwitchUser: () -> Unit = {},
    onSelectConnectObd: () -> Unit = {},
    onSelectInitializeApiKeys: () -> Unit = {}
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .background(CockpitCard)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
    ) {
        DropdownMenuItem(
            text = { Text("Initialize API Keys", color = TextPrimary, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismissRequest()
                onSelectInitializeApiKeys()
            }
        )
        DropdownMenuItem(
            text = { Text("Connect OBD Adapter", color = TextPrimary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Bluetooth, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismissRequest()
                onSelectConnectObd()
            }
        )
        DropdownMenuItem(
            text = { Text("Switch Driver / User", color = TextPrimary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismissRequest()
                onSelectSwitchUser()
            }
        )
        DropdownMenuItem(
            text = { Text("Graphic Theme: Cockpit Dark", color = TextPrimary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
            onClick = { onDismissRequest() }
        )
        DropdownMenuItem(
            text = { Text("Quick Guide", color = TextPrimary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.HelpOutline, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismissRequest()
                onSelectQuickGuide()
            }
        )
        DropdownMenuItem(
            text = { Text("Live data", color = TextPrimary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.ShowChart, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismissRequest()
                onSelectLiveData()
            }
        )
        DropdownMenuItem(
            text = { Text("DTCs", color = TextPrimary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismissRequest()
                onSelectDtc()
            }
        )
        DropdownMenuItem(
            text = { Text("Refueling", color = TextPrimary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismissRequest()
                onSelectRefueling()
            }
        )
        DropdownMenuItem(
            text = { Text("Last data", color = TextPrimary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismissRequest()
                onSelectHistory()
            }
        )
        DropdownMenuItem(
            text = { Text("Settings", color = TextPrimary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
            onClick = {
                onDismissRequest()
                onSelectSettings()
            }
        )
    }
}
